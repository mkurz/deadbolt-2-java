/*
 * Copyright 2010-2016 Steve Chaloner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.objectify.deadbolt.java.actions;

import be.objectify.deadbolt.java.Constants;
import be.objectify.deadbolt.java.ConstraintAnnotationMode;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.cache.BeforeAuthCheckCache;
import be.objectify.deadbolt.java.cache.HandlerCache;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.F;
import play.libs.typedmap.TypedKey;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

/**
 * Provides some convenience methods for concrete Deadbolt actions, such as getting the correct {@link DeadboltHandler},
 * etc.  Extend this if you want to save some time if you create your own action.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public abstract class AbstractDeadboltAction<T> extends Action<T>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDeadboltAction.class);

    static final TypedKey<Boolean> ACTION_AUTHORISED = TypedKey.create("deadbolt.action-authorised");

    static final TypedKey<AbstractDeadboltAction<?>> ACTION_DEFERRED = TypedKey.create("deadbolt.action-deferred");
    static final TypedKey<Boolean> IGNORE_DEFERRED_FLAG = TypedKey.create("deadbolt.ignore-deferred-flag");

    final HandlerCache handlerCache;

    final BeforeAuthCheckCache beforeAuthCheckCache;

    public final boolean blocking;
    public final long blockingTimeout;
    public final ConstraintAnnotationMode constraintAnnotationMode;

    private boolean authorised = false;

    protected AbstractDeadboltAction(final HandlerCache handlerCache,
                                     final BeforeAuthCheckCache beforeAuthCheckCache,
                                     final Config config)
    {
        this.handlerCache = handlerCache;
        this.beforeAuthCheckCache = beforeAuthCheckCache;
        this.blocking = config.getBoolean("deadbolt.java.blocking");
        this.blockingTimeout = config.getLong("deadbolt.java.blocking-timeout");
        this.constraintAnnotationMode = ConstraintAnnotationMode.valueOf(config.getString("deadbolt.java.constraint-mode"));
    }

    /**
     * Gets the current {@link DeadboltHandler}.  This can come from one of two places:
     * - a handler key is provided in the annotation.  A cached instance of that class will be used. This has the highest priority.
     * - the global handler defined in the application.conf by deadbolt.handler.  This has the lowest priority.
     *
     * @param handlerKey the DeadboltHandler key, if any, coming from the annotation.
     * @param <C>        the actual class of the DeadboltHandler
     * @return an option for the DeadboltHandler.
     */
    protected <C extends DeadboltHandler> DeadboltHandler getDeadboltHandler(final String handlerKey)
    {
        LOGGER.debug("Getting Deadbolt handler with key [{}]",
                     handlerKey);
        return handlerKey == null || Constants.DEFAULT_HANDLER_KEY.equals(handlerKey) ? handlerCache.get()
                                                                                       : handlerCache.apply(handlerKey);
    }

    @Override
    public CompletionStage<Result> call(final Http.Request request)
    {
        CompletionStage<Result> result;

        try
        {
            if (isDeferred(request))
            {
                final F.Tuple<AbstractDeadboltAction<?>, Http.RequestHeader> deferredAction = getDeferredAction(request);
                LOGGER.debug("Executing deferred action [{}]", deferredAction._1.getClass().getName());
                result = deferredAction._1.call((Http.Request)deferredAction._2);
            }
            else if (!request.attrs().containsKey(IGNORE_DEFERRED_FLAG)
                    && deferred())
            {
                result = delegate.call((Http.Request)defer(request,
                        this));
            }
            else
            {
                if (isAuthorised(request) && !alwaysExecute())
                {
                    result = delegate.call(request);
                }
                else
                {
                    result = maybeBlock(execute(request));
                }
            }
            return result.thenCompose(r -> {
                if(constraintAnnotationMode == ConstraintAnnotationMode.OR && !deadboltActionLeftInActionChain(this) && !this.isAuthorised() && !isAuthorised(request) && (isConstraintInActionChain(this, action -> action.precursor) || isConstraintInActionChain(this, action -> action.delegate))) {
                    // We are in OR mode and "this" was the last deadbolt-action that ran and no constraint marked the targeted action-method as authorised yet -> we finally have to fail now.
                    // If there was no "real" constraint, we don't come here, e.g. just calling @BeforeAccess or/and @DeferredDeadboltAction doesn't count as constraint
                    return onAuthFailure(getDeadboltHandler(getHandlerKey()),
                            getContent(),
                            request);
                }
                return CompletableFuture.completedFuture(r);
            });
        }
        catch (Exception e)
        {
            LOGGER.info("Something bad happened while checking authorization",
                        e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Execute the action.
     *
     * @param request the request header
     * @return the result
     * @throws Exception if something bad happens
     */
    public abstract CompletionStage<Result> execute(final Http.RequestHeader request) throws Exception;

    /**
     * If a constraint is deferrable, i.e. method-level constraints are not applied until controller-level annotations are applied.
     */
    protected abstract boolean deferred();

    /**
     * Wrapper for {@link DeadboltHandler#onAuthFailure} to ensure the access failure is logged.
     *
     * @param deadboltHandler the Deadbolt handler
     * @param content         the content type hint
     * @param request         the request
     * @return the result of {@link DeadboltHandler#onAuthFailure}
     */
    protected CompletionStage<Result> onAuthFailure(final DeadboltHandler deadboltHandler,
                                                    final Optional<String> content,
                                                    final Http.RequestHeader request)
    {
        LOGGER.info("Deadbolt: Access failure on [{}]",
                    request.uri());

        CompletionStage<Result> result;
        try
        {
            result = deadboltHandler.onAuthFailure(request,
                                                   content);
        }
        catch (Exception e)
        {
            LOGGER.warn("Deadbolt: Exception when invoking onAuthFailure",
                        e);
            result = CompletableFuture.completedFuture(Results.internalServerError());
        }
        return result;
    }

    /**
     * Marks the current action as authorised.  This allows method-level annotations to override controller-level annotations.
     *
     * @param request the request
     */
    private Http.RequestHeader markAsAuthorised(final Http.RequestHeader request)
    {
        this.authorised = true;
        return request.addAttr(ACTION_AUTHORISED,
                     true);
    }

    boolean isAuthorised() {
        return this.authorised;
    }

    /**
     * Checks if an action is authorised.  This allows controller-level annotations to cede control to method-level annotations.
     *
     * @param request the request
     * @return true if a more-specific annotation has authorised access, otherwise false
     */
    protected static boolean isAuthorised(final Http.RequestHeader request)
    {
        return request.attrs().getOptional(ACTION_AUTHORISED).orElse(false);
    }

    /**
     * Defer execution until a later point.
     *
     * @param request    the request
     * @param action the action to defer
     */
    protected Http.RequestHeader defer(final Http.RequestHeader request,
                         final AbstractDeadboltAction<T> action)
    {
        if (action != null)
        {
            LOGGER.info("Deferring action [{}]",
                        this.getClass().getName());
            return request.addAttr(ACTION_DEFERRED,
                         action);
        }
        return request;
    }

    /**
     * Check if there is a deferred action in the request.
     *
     * @param request the request
     * @return true if there is a deferred action in the request
     */
    public boolean isDeferred(final Http.RequestHeader request)
    {
        return request.attrs().containsKey(ACTION_DEFERRED);
    }

    /**
     * Get the deferred action from the request.
     *
     * @param request the request
     * @return a tuple containing the deferred action (or null if it doesn't exist) and the cleaned up request you should pass on
     */
    @SuppressWarnings("unchecked")
    public F.Tuple<AbstractDeadboltAction<?>, Http.RequestHeader> getDeferredAction(final Http.RequestHeader request)
    {
        return request.attrs().getOptional(ACTION_DEFERRED).map(action -> {
            action.delegate = this;
            return F.<AbstractDeadboltAction<?>, Http.RequestHeader>Tuple(action, request.removeAttr(ACTION_DEFERRED).addAttr(IGNORE_DEFERRED_FLAG, true));
        }).orElseGet(() -> F.Tuple(null, request));
    }

    public CompletionStage<F.Tuple<Optional<Result>, Http.RequestHeader>> preAuth(final boolean forcePreAuthCheck,
                                                                                  final Http.RequestHeader request,
                                                                                  final Optional<String> content,
                                                                                  final DeadboltHandler deadboltHandler)
    {
        return forcePreAuthCheck ? beforeAuthCheckCache.apply(deadboltHandler, request, content)
                                 : CompletableFuture.completedFuture(F.Tuple(Optional.empty(), request));
    }

    /**
     * Add a flag to the request to indicate the action has passed the constraint
     * and call the delegate.
     *
     * @param request the request
     * @return the result
     */
    protected CompletionStage<Result> authorizeAndExecute(final Http.RequestHeader request)
    {
        if(constraintAnnotationMode != ConstraintAnnotationMode.AND)
        {
            // In AND mode we don't mark an action as authorised because we want ALL (remaining) constraints to be evaluated as well!
            return delegate.call((Http.Request)markAsAuthorised(request));
        }
        return delegate.call((Http.Request)request);
    }

    /**
     * Add a flag to the request to indicate the action has been blocked by the
     * constraint and call {@link DeadboltHandler#onAuthFailure(Http.RequestHeader, Optional<String>)}.
     *
     * @param request the request
     * @param handler the relevant handler
     * @param content the content type
     * @return the result
     */
    protected CompletionStage<Result> unauthorizeAndFail(final Http.RequestHeader request,
                                                         final DeadboltHandler handler,
                                                         final Optional<String> content)
    {
        if(constraintAnnotationMode == ConstraintAnnotationMode.OR && deadboltActionLeftInActionChain(this))
        {
            // In OR mode we don't fail immediately but also check remaining constraints (it there is any left). Maybe one of these next ones authorizes...
            return delegate.call((Http.Request)request);
        }

        return onAuthFailure(handler,
                             content,
                             request);
    }

    private CompletionStage<Result> maybeBlock(CompletionStage<Result> eventualResult) throws InterruptedException,
                                                                                      ExecutionException,
                                                                                      TimeoutException
    {
        return blocking ? CompletableFuture.completedFuture(eventualResult.toCompletableFuture().get(blockingTimeout,
                                                                                                     TimeUnit.MILLISECONDS))
                        : eventualResult;
    }

    /**
     * Recursive method to determine if there is another deadbolt action further down the action chain
     */
    private static boolean deadboltActionLeftInActionChain(final Action<?> action) {
        if(action != null) {
            if(action.delegate instanceof AbstractDeadboltAction) {
                return true; // yes, there is at least one deadbolt action remaining
            }
            // action.delegate wasn't a deadbolt action, let's check the next one in the chain
            return deadboltActionLeftInActionChain(action.delegate);
        }
        return false;
    }

    /**
     * Be aware: Not every deadbolt-annotation is a constraint, but every constraint is a deadbolt-annotation ;)
     * @BeforeAccess and @DeferredDeadboltAction do NOT count as constraint, because they just pass trough (=they do NOT call markAsAuthorised() in success case)
     */
    private static boolean isConstraintInActionChain(final Action<?> action, final Function<Action<?>, Action<?>> nextAction) {
        if(action != null) {
            if(action instanceof AbstractDeadboltAction &&
                    !(action instanceof BeforeAccessAction) &&
                    !(action instanceof DeferredDeadboltAction)) {
                return true;
            }
            // that wasn't a deadbolt constraint, let's go and check the next action in the chain
            return isConstraintInActionChain(nextAction.apply(action), nextAction);
        }
        return false;
    }

    public abstract Optional<String> getContent();

    public abstract String getHandlerKey();

    public boolean alwaysExecute() {
        return false;
    }

}
