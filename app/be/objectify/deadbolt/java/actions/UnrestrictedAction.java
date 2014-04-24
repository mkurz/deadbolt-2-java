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

import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;

/**
 * Implements the {@link Unrestricted} functionality, i.e. there are no restrictions on the resource.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public class UnrestrictedAction extends AbstractDeadboltAction<Unrestricted>
{
    /**
     * {@inheritDoc}
     */
    @Override
    public F.Promise<Result> execute(Http.Context ctx) throws Throwable
    {
        F.Promise<Result> result;
        if (isActionUnauthorised(ctx))
        {
            result = onAuthFailure(getDeadboltHandler(configuration.handlerKey(),
                                                      configuration.handler()),
                                   configuration.content(),
                                   ctx);
        }
        else
        {
            markActionAsAuthorised(ctx);
            result = delegate.call(ctx);
        }

        return result;
    }
}
