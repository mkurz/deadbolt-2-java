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

import be.objectify.deadbolt.java.ExecutionContextProvider;
import be.objectify.deadbolt.java.JavaAnalyzer;
import be.objectify.deadbolt.java.cache.HandlerCache;
import be.objectify.deadbolt.java.cache.SubjectCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Configuration;
import play.mvc.Http;
import play.mvc.Result;
import scala.concurrent.ExecutionContext;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import javax.inject.Inject;

/**
 * Implements the {@link Unrestricted} functionality, i.e. there are no restrictions on the resource.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public class UnrestrictedAction extends AbstractDeadboltAction<Unrestricted>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(UnrestrictedAction.class);

    @Inject
    public UnrestrictedAction(final JavaAnalyzer analyzer,
                              final SubjectCache subjectCache,
                              final HandlerCache handlerCache,
                              final Configuration config,
                              final ExecutionContextProvider ecProvider)
    {
        super(analyzer,
              subjectCache,
              handlerCache,
              config,
              ecProvider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletionStage<Result> execute(final Http.Context ctx) throws Exception
    {
        final ExecutionContext executionContext = executionContextProvider.get();
        final CompletableFuture<Result> eventualResult = CompletableFuture.supplyAsync(() -> isActionUnauthorised(ctx))
                                                                          .thenCompose(unauthorised -> {
                                                                              try
                                                                              {
                                                                                  final CompletionStage<Result> result;
                                                                                  if (unauthorised)
                                                                                  {
                                                                                      result = onAuthFailure(getDeadboltHandler(configuration.handlerKey()),
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
                                                                              catch (Exception e)
                                                                              {
                                                                                  LOGGER.error("Something bad happened",
                                                                                               e);
                                                                                  throw new RuntimeException(e);
                                                                              }
                                                                          });
        return maybeBlock(eventualResult);
    }
}
