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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.ExecutionContextProvider;
import be.objectify.deadbolt.java.JavaAnalyzer;
import be.objectify.deadbolt.java.cache.HandlerCache;
import be.objectify.deadbolt.java.cache.SubjectCache;
import play.Configuration;
import play.mvc.Http;
import play.mvc.Result;

/**
 * Convenience class for checking if an action has already been authorised before applying the restrictions.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public abstract class AbstractRestrictiveAction<T> extends AbstractDeadboltAction<T>
{
    public AbstractRestrictiveAction(final JavaAnalyzer analyzer,
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

    @Override
    public CompletionStage<Result> execute(final Http.Context ctx) throws Exception
    {
        final CompletionStage<Result> result;
        if (isActionAuthorised(ctx))
        {
            result = delegate.call(ctx);
        }
        else
        {
            final DeadboltHandler deadboltHandler = getDeadboltHandler(getHandlerKey());
            result = preAuth(true,
                             ctx,
                             deadboltHandler)
                    .thenCompose(option -> option.map(value -> (CompletionStage<Result>)CompletableFuture.completedFuture(value))
                                                 .orElseGet(() -> applyRestriction(ctx,
                                                                                   deadboltHandler)));
        }
        return result;
    }

    /**
     * Get the key of a specific DeadboltHandler instance.
     *
     * @return a key.  May be null.
     */
    public abstract String getHandlerKey();

    public abstract CompletionStage<Result> applyRestriction(Http.Context ctx,
                                                             DeadboltHandler deadboltHandler);
}
