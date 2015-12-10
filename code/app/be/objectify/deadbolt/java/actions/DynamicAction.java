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
import be.objectify.deadbolt.java.ExceptionThrowingDynamicResourceHandler;
import be.objectify.deadbolt.java.ExecutionContextProvider;
import be.objectify.deadbolt.java.JavaAnalyzer;
import be.objectify.deadbolt.java.cache.HandlerCache;
import be.objectify.deadbolt.java.cache.SubjectCache;
import play.Configuration;
import play.libs.concurrent.HttpExecution;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * A dynamic restriction is user-defined, and so completely arbitrary.  Hence, no checks on subjects, etc, occur
 * here.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public class DynamicAction extends AbstractRestrictiveAction<Dynamic>
{
    @Inject
    public DynamicAction(final JavaAnalyzer analyzer,
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

    public DynamicAction(final JavaAnalyzer analyzer,
                         final SubjectCache subjectCache,
                         final HandlerCache handlerCache,
                         final Configuration config,
                         final Dynamic configuration,
                         final Action<?> delegate,
                         final ExecutionContextProvider ecProvider)
    {
        this(analyzer,
             subjectCache,
             handlerCache,
             config,
             ecProvider);
        this.configuration = configuration;
        this.delegate = delegate;
    }

    @Override
    public CompletionStage<Result> applyRestriction(final Http.Context ctx,
                                                    final DeadboltHandler deadboltHandler)
    {
        final CompletionStage<Result> eventualResult = deadboltHandler.getDynamicResourceHandler(ctx)
                                                                      .thenApplyAsync(option -> option.orElseGet(() -> ExceptionThrowingDynamicResourceHandler.INSTANCE), HttpExecution.defaultContext())
                                                                      .thenComposeAsync(drh -> drh.isAllowed(getValue(),
                                                                                                        getMeta(),
                                                                                                        deadboltHandler,
                                                                                                        ctx), HttpExecution.defaultContext())
                                                                      .thenComposeAsync(allowed -> {
                                                                          final CompletionStage<Result> result;
                                                                          if (allowed)
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
                                                                      }, HttpExecution.defaultContext());

        try
        {
            return maybeBlock(eventualResult);
        }
        catch (InterruptedException | ExecutionException | TimeoutException e)
        {
            throw new RuntimeException("Failed to apply dynamic constraint",
                                       e);
        }
    }

    public String getMeta()
    {
        return configuration.meta();
    }

    public String getValue()
    {
        return configuration.value();
    }

    @Override
    public String getHandlerKey()
    {
        return configuration.handlerKey();
    }
}
