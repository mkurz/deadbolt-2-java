/*
 * Copyright 2012-2016 Steve Chaloner
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

import be.objectify.deadbolt.java.DeadboltAnalyzer;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.ExecutionContextProvider;
import be.objectify.deadbolt.java.cache.CompositeCache;
import be.objectify.deadbolt.java.cache.HandlerCache;
import be.objectify.deadbolt.java.cache.SubjectCache;
import play.Configuration;
import play.libs.concurrent.HttpExecution;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.concurrent.CompletionStage;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class CompositeAction extends AbstractRestrictiveAction<Composite>
{
    private final CompositeCache compositeCache;

    @Inject
    public CompositeAction(final DeadboltAnalyzer analyzer,
                           final SubjectCache subjectCache,
                           final HandlerCache handlerCache,
                           final Configuration config,
                           final ExecutionContextProvider ecProvider,
                           final CompositeCache compositeCache)
    {
        super(analyzer,
              subjectCache,
              handlerCache,
              config,
              ecProvider);
        this.compositeCache = compositeCache;
    }

    @Override
    public CompletionStage<Result> applyRestriction(final Http.Context ctx,
                                                    final DeadboltHandler handler)
    {
        return compositeCache.apply(getValue())
                             .map(constraint -> constraint.test(ctx,
                                                                handler,
                                                                HttpExecution.defaultContext())
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
                                             result = onAuthFailure(handler,
                                                                    configuration.content(),
                                                                    ctx);
                                         }
                                         return result;
                                     },
                                                       HttpExecution.defaultContext()))
                             .orElseGet(() -> {
                                 markActionAsUnauthorised(ctx);
                                 return onAuthFailure(handler,
                                                      configuration.content(),
                                                      ctx);
                             });
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
