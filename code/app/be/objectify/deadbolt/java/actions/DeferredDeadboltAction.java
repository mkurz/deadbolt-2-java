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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;

/**
 * Executes a deferred method-level annotation.  Ideally, the associated annotation would be placed
 * above any other class-level Deadbolt annotations in order to still have things fire in the correct order.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public class DeferredDeadboltAction extends AbstractDeadboltAction<DeferredDeadbolt>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DeferredDeadboltAction.class);

    @Override
    public F.Promise<Result> execute(final Http.Context ctx) throws Throwable
    {
        return F.Promise.promise(new F.Function0<AbstractDeadboltAction>()
        {
            @Override
            public AbstractDeadboltAction apply() throws Throwable
            {
                return getDeferredAction(ctx);
            }
        }).flatMap(new F.Function<AbstractDeadboltAction, F.Promise<Result>>()
        {
            @Override
            public F.Promise<Result> apply(final AbstractDeadboltAction deferredAction) throws Throwable
            {
                F.Promise<Result> result;
                if (deferredAction == null)
                {
                    result = delegate.call(ctx);
                }
                else
                {
                    LOGGER.info(String.format("Executing deferred action [%s]",
                                              deferredAction.getClass().getName()));
                    result = deferredAction.call(ctx);
                }
                return result;
            }
        });
    }
}