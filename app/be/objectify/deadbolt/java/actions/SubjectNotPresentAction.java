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

import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.utils.PluginUtils;
import be.objectify.deadbolt.java.utils.RequestUtils;
import play.libs.F;
import play.mvc.Http;
import play.mvc.SimpleResult;

import java.util.concurrent.TimeUnit;

/**
 * Implements the {@link SubjectNotPresent} functionality, i.e. the
 * {@link be.objectify.deadbolt.core.models.Subject} provided by the {@link be.objectify.deadbolt.java.DeadboltHandler}
 * must be null to have access to the resource.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public class SubjectNotPresentAction extends AbstractDeadboltAction<SubjectNotPresent>
{
    /**
     * {@inheritDoc}
     */
    @Override
    public F.Promise<SimpleResult> execute(Http.Context ctx) throws Throwable
    {
        F.Promise<SimpleResult> result = F.Promise.pure(null);
        if (isActionUnauthorised(ctx))
        {
            result = onAuthFailure(getDeadboltHandler(configuration.handlerKey(),
                                                      configuration.handler()),
                                   configuration.content(),
                                   ctx);
        }
        else
        {
            DeadboltHandler deadboltHandler = getDeadboltHandler(configuration.handlerKey(),
                                                                 configuration.handler());
            if (configuration.forceBeforeAuthCheck())
            {
               result = deadboltHandler.beforeAuthCheck(ctx);
            }

            SimpleResult futureResult = result.get(PluginUtils.getBeforeAuthCheckTimeout(),
                                                   TimeUnit.MILLISECONDS);
            if (futureResult == null)
            {
                Subject subject = getSubject(ctx,
                                             deadboltHandler);

                if (subject == null)
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
        }

        return result;
    }

    /**
     * Gets the {@link be.objectify.deadbolt.core.models.Subject} from the {@link DeadboltHandler}.
     *
     * @param ctx             the request context
     * @param deadboltHandler the Deadbolt handler
     * @return the Subject, if any
     */
    @Override
    protected Subject getSubject(Http.Context ctx,
                                 DeadboltHandler deadboltHandler)
    {
        return RequestUtils.getSubject(deadboltHandler,
                                       ctx);
    }
}
