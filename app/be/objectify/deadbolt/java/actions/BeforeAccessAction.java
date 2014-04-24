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
import be.objectify.deadbolt.java.utils.PluginUtils;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;

import java.util.concurrent.TimeUnit;

/**
 * Invokes beforeAuthCheck on the global or a specific {@link be.objectify.deadbolt.java.DeadboltHandler}.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public class BeforeAccessAction extends AbstractDeadboltAction<BeforeAccess>
{
    /**
     * {@inheritDoc}
     */
    @Override
    public F.Promise<Result> execute(Http.Context ctx) throws Throwable
    {
        F.Promise<Result> result;
        if (isActionAuthorised(ctx) && !configuration.alwaysExecute())
        {
            result = delegate.call(ctx);
        }
        else
        {
            DeadboltHandler deadboltHandler = getDeadboltHandler(configuration.handlerKey(),
                                                                 configuration.value());
            result = deadboltHandler.beforeAuthCheck(ctx);

            Result futureResult = result.get(PluginUtils.getBeforeAuthCheckTimeout(),
                                                   TimeUnit.MILLISECONDS);

            if (futureResult == null)
            {
                result = delegate.call(ctx);
            }
        }
        return result;
    }
}
