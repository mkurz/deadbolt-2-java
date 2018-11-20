/*
 * Copyright 2010-2017 Steve Chaloner
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

import be.objectify.deadbolt.java.ConstraintLogic;
import be.objectify.deadbolt.java.ConstraintPoint;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.cache.BeforeAuthCheckCache;
import be.objectify.deadbolt.java.cache.CompositeCache;
import be.objectify.deadbolt.java.cache.HandlerCache;
import com.typesafe.config.Config;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class CompositeAction extends AbstractRestrictiveAction<Composite>
{
    private final CompositeCache compositeCache;

    @Inject
    public CompositeAction(final HandlerCache handlerCache,
                           final BeforeAuthCheckCache beforeAuthCheckCache,
                           final Config config,
                           final CompositeCache compositeCache,
                           final ConstraintLogic constraintLogic)
    {
        super(handlerCache,
              beforeAuthCheckCache,
              config,
              constraintLogic);
        this.compositeCache = compositeCache;
    }

    @Override
    public CompletionStage<Result> applyRestriction(final Http.RequestHeader request,
                                                    final DeadboltHandler handler)
    {
        return compositeCache.apply(configuration.value())
                             .map(constraint ->
                                  {
                                      final boolean preferGlobalMeta = configuration.preferGlobalMeta();
                                      return constraint.test(request,
                                                             handler,
                                                             Optional.ofNullable(configuration.meta()),
                                                             (globalMd, localMd) -> preferGlobalMeta ? globalMd.isPresent() ? globalMd : localMd
                                                                                                     : localMd.isPresent() ? localMd : globalMd)
                                                       .thenCompose(allowed -> allowed._1 ? authorizeAndExecute(allowed._2,
                                                                                                             handler)
                                                                                       : unauthorizeAndFail(allowed._2,
                                                                                                            handler,
                                                                                                            getContent()));
                                  })
                             .orElseGet(() -> unauthorizeAndFail(request,
                                                                 handler,
                                                                 getContent()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean deferred() {
        return configuration.deferred();
    }

    private CompletionStage<Result> authorizeAndExecute(final Http.RequestHeader request,
                                                        final DeadboltHandler handler) 
    {
        handler.onAuthSuccess(request,
                              "composite",
                              ConstraintPoint.CONTROLLER);
        return authorizeAndExecute(request);
    }

    @Override
    public Optional<String> getContent()
    {
        return Optional.ofNullable(configuration.content());
    }

    @Override
    public String getHandlerKey()
    {
        return configuration.handlerKey();
    }
}
