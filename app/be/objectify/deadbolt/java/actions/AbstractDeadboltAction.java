/*
 * Copyright 2010-2012 Steve Chaloner
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

import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.JavaDeadboltAnalyzer;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.utils.PluginUtils;
import be.objectify.deadbolt.java.utils.ReflectionUtils;
import be.objectify.deadbolt.java.utils.RequestUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.F;
import play.mvc.*;

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

    /**
     * Gets the current {@link DeadboltHandler}.  This can come from one of three places:
     * - a handler key is provided in the annotation.  A cached instance of that class will be used. This has the highest priority.
     * - a class name is provided in the annotation.  A new instance of that class will be created.
     * - the global handler defined in the application.conf by deadbolt.handler.  This has the lowest priority.
     *
     * @param handlerKey the DeadboltHandler key, if any, coming from the annotation. May be null.
     * @param deadboltHandlerClass the DeadboltHandler class, if any, coming from the annotation. May be null.
     * @param <C>                  the actual class of the DeadboltHandler
     * @return an instance of DeadboltHandler.
     */
    protected <C extends DeadboltHandler> DeadboltHandler getDeadboltHandler(String handlerKey,
                                                                             Class<C> deadboltHandlerClass) throws Throwable
    {
        DeadboltHandler deadboltHandler;
        if (StringUtils.isNotEmpty(handlerKey))
        {
            LOGGER.info("Getting Deadbolt handler with key [{}]",
                    handlerKey);
            deadboltHandler = PluginUtils.getDeadboltHandler(handlerKey);
            LOGGER.info("Deadbolt handler with key [{}] - found [{}]",
                    handlerKey,
                    deadboltHandler);

            if (deadboltHandler == null)
            {
                LOGGER.error("Falling back to global handler because requested handler [{}] is null",
                             handlerKey);
                deadboltHandler = PluginUtils.getDeadboltHandler();
            }
        }
        else if (deadboltHandlerClass != null
                && !deadboltHandlerClass.isInterface())
        {
            try
            {
                deadboltHandler = deadboltHandlerClass.newInstance();
            }
            catch (Exception e)
            {
                throw new RuntimeException("Error creating Deadbolt handler",
                                           e);
            }
        }
        else
        {
            deadboltHandler = PluginUtils.getDeadboltHandler();
        }
        return deadboltHandler;
    }

    /** {@inheritDoc} */
    @Override
    public F.Promise<SimpleResult> call(Http.Context ctx) throws Throwable
    {
        F.Promise<SimpleResult> result;

        Class annClass = configuration.getClass();
        if (isDeferred(ctx))
        {
            result = getDeferredAction(ctx).call(ctx);
        }
        else if (!ctx.args.containsKey(IGNORE_DEFERRED_FLAG)
                && ReflectionUtils.hasMethod(annClass, "deferred") &&
                (Boolean)annClass.getMethod("deferred").invoke(configuration))
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

    /**
     * Execute the action.
     *
     * @param ctx the request context
     * @return the result
     * @throws Throwable if something bad happens
     */
    public abstract F.Promise<SimpleResult> execute(Http.Context ctx) throws Throwable;

    /**
     * @param subject
     * @param roleNames
     * @return
     */
    protected boolean checkRole(Subject subject,
                                String[] roleNames)
    {
        return JavaDeadboltAnalyzer.checkRole(subject,
                                              roleNames);
    }

    /**
     * @param subject
     * @param roleNames
     * @return
     */
    protected boolean hasAllRoles(Subject subject,
                                  String[] roleNames)
    {
        return JavaDeadboltAnalyzer.hasAllRoles(subject,
                                                roleNames);
    }

    /**
     * Wrapper for {@link DeadboltHandler#onAuthFailure} to ensure the access failure is logged.
     *
     * @param deadboltHandler the Deadbolt handler
     * @param content         the content type hint
     * @param ctx             th request context
     * @return the result of {@link DeadboltHandler#onAuthFailure}
     */
    protected F.Promise<SimpleResult> onAuthFailure(DeadboltHandler deadboltHandler,
                                                    String content,
                                                    Http.Context ctx)
    {
        LOGGER.warn(String.format("Deadbolt: Access failure on [%s]",
                                  ctx.request().uri()));

        try
        {
            return deadboltHandler.onAuthFailure(ctx,
                                                 content);
        }
        catch (Exception e)
        {
            LOGGER.warn("Deadbolt: Exception when invoking onAuthFailure",
                        e);
            return F.Promise.promise(new F.Function0<SimpleResult>()
            {
                @Override
                public SimpleResult apply() throws Throwable {
                    return Results.internalServerError();
                }
            });
        }
    }

    /**
     * Gets the {@link be.objectify.deadbolt.core.models.Subject} from the {@link DeadboltHandler}, and logs an error if it's not present. Note that
     * at least one actions ({@link Unrestricted} does not not require a Subject to be present.
     *
     * @param ctx             the request context
     * @param deadboltHandler the Deadbolt handler
     * @return the Subject, if any
     */
    protected Subject getSubject(Http.Context ctx,
                                 DeadboltHandler deadboltHandler)
    {
        Subject subject = RequestUtils.getSubject(deadboltHandler,
                                                  ctx);
        if (subject == null)
        {
            LOGGER.error(String.format("Access to [%s] requires a subject, but no subject is present.",
                                       ctx.request().uri()));
        }

        return subject;
    }

    /**
     * Marks the current action as authorised.  This allows method-level annotations to override controller-level annotations.
     *
     * @param ctx the request context
     */
    protected void markActionAsAuthorised(Http.Context ctx)
    {
        ctx.args.put(ACTION_AUTHORISED,
                     true);
    }

    /**
     * Marks the current action as unauthorised.  This allows method-level annotations to override controller-level annotations.
     *
     * @param ctx the request context
     */
    protected void markActionAsUnauthorised(Http.Context ctx)
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
    protected boolean isActionAuthorised(Http.Context ctx)
    {
        Object o = ctx.args.get(ACTION_AUTHORISED);
        return o != null && (Boolean) o;
    }

    /**
     * Checks if an action is unauthorised.  This allows controller-level annotations to cede control to method-level annotations.
     *
     * @param ctx the request context
     * @return true if a more-specific annotation has blocked access, otherwise false
     */
    protected boolean isActionUnauthorised(Http.Context ctx)
    {
        Object o = ctx.args.get(ACTION_UNAUTHORISED);
        return o != null && (Boolean) o;
    }

    /**
     * Defer execution until a later point.
     *
     * @param ctx the request context
     * @param action the action to defer
     */
    protected void defer(Http.Context ctx,
                         AbstractDeadboltAction action)
    {
        if (action != null)
        {
            LOGGER.info(String.format("Deferring action [%s]",
                                      this.getClass().getName()));
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
    public boolean isDeferred(Http.Context ctx)
    {
        return ctx.args.containsKey(ACTION_DEFERRED);
    }

    /**
     * Get the deferred action from the context.
     *
     * @param ctx the request context
     * @return the deferred action, or null if it doesn't exist
     */
    public AbstractDeadboltAction getDeferredAction(Http.Context ctx)
    {
        AbstractDeadboltAction action = null;
        Object o = ctx.args.get(ACTION_DEFERRED);
        if (o != null)
        {
            action = (AbstractDeadboltAction)o;

            ctx.args.remove(ACTION_DEFERRED);
            ctx.args.put(IGNORE_DEFERRED_FLAG,
                         true);
        }
        return action;
    }
}
