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
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;

/**
 * Convenience class for checking if an qction has already been authorised before applying the restrictions.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public abstract class AbstractRestrictiveAction<T> extends AbstractDeadboltAction<T>
{
    @Override
    public F.Promise<Result> execute(final Http.Context ctx) throws Throwable
    {
        final F.Promise<Result> result;
        if (isActionAuthorised(ctx))
        {
            result = delegate.call(ctx);
        }
        else
        {
            final DeadboltHandler deadboltHandler = getDeadboltHandler(getHandlerKey(),
                                                                       getDeadboltHandlerClass());
            result = preAuth(true,
                             ctx,
                             deadboltHandler)
                    .flatMap(new F.Function<Result, F.Promise<Result>>()
                    {
                        @Override
                        public F.Promise<Result> apply(final Result preAuthResult) throws Throwable
                        {
                            final F.Promise<Result> innerResult;
                            if (preAuthResult != null)
                            {
                                innerResult = F.Promise.pure(preAuthResult);
                            }
                            else
                            {
                                innerResult = applyRestriction(ctx,
                                                               deadboltHandler);
                            }
                            return innerResult;
                        }
                    });
        }
        return result;
    }

    /**
     * Get the key of a specific DeadboltHandler instance.
     *
     * @return a key.  May be null.
     */
    public abstract String getHandlerKey();

    /**
     * Get the class of a specific Deadbolt handler.
     *
     * @return the class of a DeadboltHandler implementation.  May be null.
     * @deprecated Prefer {@link be.objectify.deadbolt.java.actions.AbstractRestrictiveAction#getHandlerKey()} instead
     */
    public abstract Class<? extends DeadboltHandler> getDeadboltHandlerClass();

    public abstract F.Promise<Result> applyRestriction(Http.Context ctx,
                                                       DeadboltHandler deadboltHandler) throws Throwable;
}
