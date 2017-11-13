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

import be.objectify.deadbolt.java.cache.BeforeAuthCheckCache;
import be.objectify.deadbolt.java.cache.HandlerCache;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Executes a deferred method-level annotation.  Ideally, the associated annotation would be placed
 * above any other class-level Deadbolt annotations in order to still have things fire in the correct order.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public class DeferredDeadboltAction extends AbstractDeadboltAction<DeferredDeadbolt>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DeferredDeadboltAction.class);

    @Inject
    public DeferredDeadboltAction(final HandlerCache handlerCache,
                                  final BeforeAuthCheckCache beforeAuthCheckCache,
                                  final Config config)
    {
        super(handlerCache,
              beforeAuthCheckCache,
              config);
    }

    @Override
    public CompletionStage<Result> execute(final Http.Context ctx) throws Exception
    {
        final CompletableFuture<Result> eventualResult = CompletableFuture.completedFuture(getDeferredAction(ctx))
                                                                          .thenCompose(deferredAction ->
                                                                                            {
                                                                                                final CompletionStage<Result> result;
                                                                                                if (deferredAction == null)
                                                                                                {
                                                                                                    result = delegate.call(ctx);
                                                                                                }
                                                                                                else
                                                                                                {
                                                                                                    LOGGER.debug("Executing deferred action [{}]",
                                                                                                                 deferredAction.getClass().getName());
                                                                                                    result = deferredAction.call(ctx);
                                                                                                }
                                                                                                return result;
                                                                                            });
        return maybeBlock(eventualResult);
    }
}
