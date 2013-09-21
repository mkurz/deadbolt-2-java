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
import play.mvc.SimpleResult;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class PatternAction extends AbstractRestrictiveAction<Pattern>
{
    public PatternAction()
    {
        // no-op
    }

    public PatternAction(Pattern configuration,
                         Action<?> delegate)
    {
        this.configuration = configuration;
        this.delegate = delegate;
    }

    @Override
    public F.Promise<SimpleResult> applyRestriction(Http.Context ctx,
                                                    DeadboltHandler deadboltHandler) throws Throwable
    {
        F.Promise<SimpleResult> result;

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

    private F.Promise<SimpleResult> custom(Http.Context ctx,
                                           DeadboltHandler deadboltHandler) throws Throwable
    {
        DynamicResourceHandler resourceHandler = deadboltHandler.getDynamicResourceHandler(ctx);
        F.Promise<SimpleResult> result;

        if (resourceHandler == null)
        {
            throw new RuntimeException(
                    "A custom permission type is specified but no dynamic resource handler is provided");
        }
        else
        {
            if (resourceHandler.checkPermission(getValue(),
                                                deadboltHandler,
                                                ctx))
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
        }
        return result;
    }

    public String getValue()
    {
        return configuration.value();
    }

    private F.Promise<SimpleResult> equality(Http.Context ctx,
                                             DeadboltHandler deadboltHandler) throws Throwable
    {
        F.Promise<SimpleResult> result;

        final String patternValue = getValue();

        if (JavaDeadboltAnalyzer.checkPatternEquality(getSubject(ctx,
                                                                 deadboltHandler),
                                                      patternValue))
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

    /**
     * Checks access to the resource based on the regex
     *
     * @param ctx             the HTTP context
     * @param deadboltHandler the Deadbolt handler
     * @return the necessary result
     * @throws Throwable if something needs throwing
     */
    private F.Promise<SimpleResult> regex(Http.Context ctx,
                                          DeadboltHandler deadboltHandler) throws Throwable
    {
        F.Promise<SimpleResult> result;

        final String patternValue = getValue();
        java.util.regex.Pattern pattern = DeadboltViewSupport.getPattern(patternValue);

        if (JavaDeadboltAnalyzer.checkRegexPattern(getSubject(ctx,
                                                              deadboltHandler),
                                                   pattern))
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

    @Override
    public Class<? extends DeadboltHandler> getDeadboltHandlerClass()
    {
        return configuration.handler();
    }
}
