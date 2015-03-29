/*
 * Copyright 2012 Steve Chaloner
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

import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import be.objectify.deadbolt.java.DeadboltViewSupport;
import be.objectify.deadbolt.java.JavaDeadboltAnalyzer;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class PatternAction extends AbstractRestrictiveAction<Pattern>
{
    private static final JavaDeadboltAnalyzer ANALYZER = new JavaDeadboltAnalyzer();

    public PatternAction()
    {
        // no-op
    }

    public PatternAction(final Pattern configuration,
                         final Action<?> delegate)
    {
        this.configuration = configuration;
        this.delegate = delegate;
    }

    @Override
    public F.Promise<Result> applyRestriction(Http.Context ctx,
                                              DeadboltHandler deadboltHandler) throws Throwable
    {
        F.Promise<Result> result;

        switch (configuration.patternType())
        {
            case EQUALITY:
                result = equality(ctx,
                                  deadboltHandler);
                break;
            case REGEX:
                result = regex(ctx,
                               deadboltHandler);
                break;
            case CUSTOM:
                result = custom(ctx,
                                deadboltHandler);
                break;
            default:
                throw new RuntimeException("Unknown pattern type: " + configuration.patternType());
        }

        return result;
    }

    private F.Promise<Result> custom(final Http.Context ctx,
                                     final DeadboltHandler deadboltHandler) throws Throwable
    {
        final DynamicResourceHandler resourceHandler = deadboltHandler.getDynamicResourceHandler(ctx);
        F.Promise<Result> result;

        if (resourceHandler == null)
        {
            throw new RuntimeException(
                    "A custom permission type is specified but no dynamic resource handler is provided");
        }
        else
        {
            result = F.Promise.promise(new F.Function0<Boolean>()
            {
                @Override
                public Boolean apply() throws Throwable
                {
                    return resourceHandler.checkPermission(getValue(),
                                                           deadboltHandler,
                                                           ctx);
                }
            }).flatMap(new F.Function<Boolean, F.Promise<Result>>()
            {
                @Override
                public F.Promise<Result> apply(final Boolean allowed) throws Throwable
                {
                    final F.Promise<Result> innerResult;
                    if (allowed)
                    {
                        markActionAsAuthorised(ctx);
                        innerResult = delegate.call(ctx);
                    }
                    else
                    {
                        markActionAsUnauthorised(ctx);
                        innerResult = onAuthFailure(deadboltHandler,
                                                    configuration.content(),
                                                    ctx);
                    }
                    return innerResult;
                }
            });
        }
        return result;
    }

    public String getValue()
    {
        return configuration.value();
    }

    private F.Promise<Result> equality(final Http.Context ctx,
                                       final DeadboltHandler deadboltHandler) throws Throwable
    {
        final String patternValue = getValue();
        return F.Promise.promise(new F.Function0<Boolean>()
        {
            @Override
            public Boolean apply() throws Throwable
            {
                return ANALYZER.checkPatternEquality(getSubject(ctx,
                                                                deadboltHandler),
                                                     patternValue);
            }
        }).flatMap(new F.Function<Boolean, F.Promise<Result>>()
        {
            @Override
            public F.Promise<Result> apply(final Boolean equal) throws Throwable
            {
                final F.Promise<Result> result;
                if (equal)
                {
                    markActionAsAuthorised(ctx);
                    result = delegate.call(ctx);
                }
                else
                {
                    markActionAsUnauthorised(ctx);
                    result = onAuthFailure(deadboltHandler,
                                           configuration.content(),
                                           ctx);
                }

                return result;
            }
        });
    }

    /**
     * Checks access to the resource based on the regex
     *
     * @param ctx             the HTTP context
     * @param deadboltHandler the Deadbolt handler
     * @return the necessary result
     * @throws Throwable if something needs throwing
     */
    private F.Promise<Result> regex(final Http.Context ctx,
                                    final DeadboltHandler deadboltHandler) throws Throwable
    {
        final String patternValue = getValue();
        return F.Promise.promise(new F.Function0<java.util.regex.Pattern>()
        {
            @Override
            public java.util.regex.Pattern apply() throws Throwable
            {
                return DeadboltViewSupport.getPattern(patternValue);
            }
        }).map(new F.Function<java.util.regex.Pattern, Boolean>()
        {
            @Override
            public Boolean apply(final java.util.regex.Pattern pattern) throws Throwable
            {
                return ANALYZER.checkRegexPattern(getSubject(ctx,
                                                             deadboltHandler),
                                                  pattern);
            }
        }).flatMap(new F.Function<Boolean, F.Promise<Result>>()
        {
            @Override
            public F.Promise<Result> apply(final Boolean applicable) throws Throwable
            {
                final F.Promise<Result> result;
                if (applicable)
                {
                    markActionAsAuthorised(ctx);
                    result = delegate.call(ctx);
                }
                else
                {
                    markActionAsUnauthorised(ctx);
                    result = onAuthFailure(deadboltHandler,
                                           configuration.content(),
                                           ctx);
                }
                return result;
            }
        });
    }

    @Override
    public String getHandlerKey()
    {
        return configuration.handlerKey();
    }

    @Override
    public Class<? extends DeadboltHandler> getDeadboltHandlerClass()
    {
        return configuration.handler();
    }
}
