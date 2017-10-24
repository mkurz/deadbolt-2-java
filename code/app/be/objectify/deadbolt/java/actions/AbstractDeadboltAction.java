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

import be.objectify.deadbolt.java.ConfigKeys;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.cache.BeforeAuthCheckCache;
import be.objectify.deadbolt.java.cache.HandlerCache;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Provides some convenience methods for concrete Deadbolt actions, such as getting the correct {@link DeadboltHandler},
 * etc.  Extend this if you want to save some time if you create your own action.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public abstract class AbstractDeadboltAction<T> extends Action<T>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDeadboltAction.class);

    private static final String ACTION_AUTHORISED = "deadbolt.action-authorised";

    private static final String ACTION_UNAUTHORISED = "deadbolt.action-unauthorised";

    private static final String ACTION_DEFERRED = "deadbolt.action-deferred";
    private static final String IGNORE_DEFERRED_FLAG = "deadbolt.ignore-deferred-flag";

    final HandlerCache handlerCache;

    final BeforeAuthCheckCache beforeAuthCheckCache;

    final Config config;

    public final boolean blocking;
    public final long blockingTimeout;

    protected AbstractDeadboltAction(final HandlerCache handlerCache,
                                     final BeforeAuthCheckCache beforeAuthCheckCache,
                                     final Config config)
    {
        this.handlerCache = handlerCache;
        this.beforeAuthCheckCache = beforeAuthCheckCache;
        this.config = config;

        final HashMap<String, Object> defaults = new HashMap<>();
        defaults.put(ConfigKeys.BLOCKING_DEFAULT._1,
                     ConfigKeys.BLOCKING_DEFAULT._2);
        defaults.put(ConfigKeys.DEFAULT_BLOCKING_TIMEOUT_DEFAULT._1,
                     ConfigKeys.DEFAULT_BLOCKING_TIMEOUT_DEFAULT._2);
        final Config configWithFallback = config.withFallback(ConfigFactory.parseMap(defaults));
        this.blocking = configWithFallback.getBoolean(ConfigKeys.BLOCKING_DEFAULT._1);
        this.blockingTimeout = configWithFallback.getLong(ConfigKeys.DEFAULT_BLOCKING_TIMEOUT_DEFAULT._1);
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
        return handlerKey == null || ConfigKeys.DEFAULT_HANDLER_KEY.equals(handlerKey) ? handlerCache.get()
                                                                                       : handlerCache.apply(handlerKey);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletionStage<Result> call(final Http.Context ctx)
    {
        CompletionStage<Result> result;

        Class annClass = configuration.getClass();
        try
        {
            if (isDeferred(ctx))
            {
                result = getDeferredAction(ctx).call(ctx);
            }
            else if (!ctx.args.containsKey(IGNORE_DEFERRED_FLAG)
                    && annClass.isAnnotationPresent(Deferrable.class)
                    && (Boolean) annClass.getMethod("deferred").invoke(configuration))
            {
                defer(ctx,
                      this);
                result = delegate.call(ctx);
            }
            else
            {
                result = execute(ctx);
            }
            return result;
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
     * @param ctx the request context
     * @return the result
     * @throws Exception if something bad happens
     */
    public abstract CompletionStage<Result> execute(final Http.Context ctx) throws Exception;

    /**
     * Wrapper for {@link DeadboltHandler#onAuthFailure} to ensure the access failure is logged.
     *
     * @param deadboltHandler the Deadbolt handler
     * @param content         the content type hint
     * @param ctx             th request context
     * @return the result of {@link DeadboltHandler#onAuthFailure}
     */
    protected CompletionStage<Result> onAuthFailure(final DeadboltHandler deadboltHandler,
                                                    final Optional<String> content,
                                                    final Http.Context ctx)
    {
        LOGGER.info("Deadbolt: Access failure on [{}]",
                    ctx.request().uri());

        CompletionStage<Result> result;
        try
        {
            result = deadboltHandler.onAuthFailure(ctx,
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
     * @param ctx the request context
     */
    protected void markActionAsAuthorised(final Http.Context ctx)
    {
        ctx.args.put(ACTION_AUTHORISED,
                     true);
    }

    /**
     * Marks the current action as unauthorised.  This allows method-level annotations to override controller-level annotations.
     *
     * @param ctx the request context
     */
    protected void markActionAsUnauthorised(final Http.Context ctx)
    {
        ctx.args.put(ACTION_UNAUTHORISED,
                     true);
    }

    /**
     * Checks if an action is authorised.  This allows controller-level annotations to cede control to method-level annotations.
     *
     * @param ctx the request context
     * @return true if a more-specific annotation has authorised access, otherwise false
     */
    protected boolean isActionAuthorised(final Http.Context ctx)
    {
        final Object o = ctx.args.get(ACTION_AUTHORISED);
        return o != null && (Boolean) o;
    }

    /**
     * Checks if an action is unauthorised.  This allows controller-level annotations to cede control to method-level annotations.
     *
     * @param ctx the request context
     * @return true if a more-specific annotation has blocked access, otherwise false
     */
    protected boolean isActionUnauthorised(final Http.Context ctx)
    {
        final Object o = ctx.args.get(ACTION_UNAUTHORISED);
        return o != null && (Boolean) o;
    }

    /**
     * Defer execution until a later point.
     *
     * @param ctx    the request context
     * @param action the action to defer
     */
    protected void defer(final Http.Context ctx,
                         final AbstractDeadboltAction action)
    {
        if (action != null)
        {
            LOGGER.info("Deferring action [{}]",
                        this.getClass().getName());
            ctx.args.put(ACTION_DEFERRED,
                         action);
        }
    }

    /**
     * Check if there is a deferred action in the context.
     *
     * @param ctx the request context
     * @return true iff there is a deferred action in the context
     */
    public boolean isDeferred(final Http.Context ctx)
    {
        return ctx.args.containsKey(ACTION_DEFERRED);
    }

    /**
     * Get the deferred action from the context.
     *
     * @param ctx the request context
     * @return the deferred action, or null if it doesn't exist
     */
    public AbstractDeadboltAction getDeferredAction(final Http.Context ctx)
    {
        AbstractDeadboltAction action = null;
        final Object o = ctx.args.get(ACTION_DEFERRED);
        if (o != null)
        {
            action = (AbstractDeadboltAction) o;

            ctx.args.remove(ACTION_DEFERRED);
            ctx.args.put(IGNORE_DEFERRED_FLAG,
                         true);
        }
        return action;
    }

    public CompletionStage<Optional<Result>> preAuth(final boolean forcePreAuthCheck,
                                                     final Http.Context ctx,
                                                     final DeadboltHandler deadboltHandler)
    {
        return forcePreAuthCheck ? beforeAuthCheckCache.apply(deadboltHandler, ctx)
                                 : CompletableFuture.completedFuture(Optional.empty());
    }

    /**
     * Add a flag to the context to indicate the action has passed the constraint
     * and call the delegate.
     *
     * @param context the context
     * @return the result
     */
    protected CompletionStage<Result> authorizeAndExecute(final Http.Context context)
    {
        markActionAsAuthorised(context);
        return delegate.call(context);
    }

    /**
     * Add a flag to the context to indicate the action has been blocked by the
     * constraint and call {@link DeadboltHandler#onAuthFailure(Http.Context, Optional<String>)}.
     *
     * @param context the context
     * @param handler the relevant handler
     * @param content the content type
     * @return the result
     */
    protected CompletionStage<Result> unauthorizeAndFail(final Http.Context context,
                                                         final DeadboltHandler handler,
                                                         final Optional<String> content)
    {
        markActionAsUnauthorised(context);
        return onAuthFailure(handler,
                             content,
                             context);
    }

    public static CompletionStage<Result> sneakyCall(final Action<?> action,
                                                     final Http.Context context)
    {
        try
        {
            return action.call(context);
        }
        catch (Throwable t)
        {
            throw sneakyThrow(t);
        }
    }

    CompletionStage<Result> maybeBlock(CompletionStage<Result> eventualResult) throws InterruptedException,
                                                                                      ExecutionException,
                                                                                      TimeoutException
    {
        return blocking ? CompletableFuture.completedFuture(eventualResult.toCompletableFuture().get(blockingTimeout,
                                                                                                     TimeUnit.MILLISECONDS))
                        : eventualResult;
    }

    private static RuntimeException sneakyThrow(final Throwable t)
    {
        if (t == null)
        {
            throw new NullPointerException("Can't use sneakyThrow without a throwable");
        }
        sneakyThrow0(t);
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void sneakyThrow0(final Throwable t) throws T
    {
        throw (T) t;
    }
}
