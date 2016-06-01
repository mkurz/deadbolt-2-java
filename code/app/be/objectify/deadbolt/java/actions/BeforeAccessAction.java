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

import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.ExecutionContextProvider;
import be.objectify.deadbolt.java.cache.HandlerCache;
import play.Configuration;
import play.mvc.Http;
import play.mvc.Result;
import scala.concurrent.ExecutionContextExecutor;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Invokes beforeAuthCheck on the global or a specific {@link be.objectify.deadbolt.java.DeadboltHandler}.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public class  BeforeAccessAction extends AbstractDeadboltAction<BeforeAccess>
{
    @Inject
    public BeforeAccessAction(final HandlerCache handlerCache,
                              final Configuration config,
                              final ExecutionContextProvider ecProvider)
    {
        super(handlerCache,
              config,
              ecProvider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletionStage<Result> execute(final Http.Context ctx) throws Exception
    {
        final CompletionStage<Result> result;
        if (isActionAuthorised(ctx) && !configuration.alwaysExecute())
        {
            result = delegate.call(ctx);
        }
        else
        {
            final DeadboltHandler deadboltHandler = getDeadboltHandler(configuration.handlerKey());
            final ExecutionContextExecutor executor = executor();
            result = preAuth(true,
                             ctx,
                             deadboltHandler)
                    .thenComposeAsync(preAuthResult -> preAuthResult.map(r -> (CompletionStage<Result>)CompletableFuture.completedFuture(r))
                                                                    .orElseGet(() -> sneakyCall(delegate, ctx)),
                                      executor);
        }
        return maybeBlock(result);
    }
}
