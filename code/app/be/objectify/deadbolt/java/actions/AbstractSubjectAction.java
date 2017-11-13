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
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.cache.BeforeAuthCheckCache;
import be.objectify.deadbolt.java.cache.HandlerCache;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public abstract class AbstractSubjectAction<T> extends AbstractDeadboltAction<T>
{
    private final ConstraintLogic constraintLogic;

    AbstractSubjectAction(final HandlerCache handlerCache,
                          final BeforeAuthCheckCache beforeAuthCheckCache,
                          final com.typesafe.config.Config config,
                          final ConstraintLogic constraintLogic)
    {
        super(handlerCache,
              beforeAuthCheckCache,
              config);
        this.constraintLogic = constraintLogic;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompletionStage<Result> execute(final Http.Context ctx) throws Exception
    {
        final CompletionStage<Result> result;
        if (isActionUnauthorised(ctx))
        {
            result = onAuthFailure(getDeadboltHandler(getHandlerKey()),
                                   getContent(),
                                   ctx);
        }
        else if (isActionAuthorised(ctx))
        {
            result = delegate.call(ctx);
        }
        else
        {
            final DeadboltHandler deadboltHandler = getDeadboltHandler(getHandlerKey());
            result = preAuth(isForceBeforeAuthCheck(),
                             ctx,
                             getContent(),
                             deadboltHandler)
                    .thenCompose(maybePreAuth -> maybePreAuth.map(CompletableFuture::completedFuture)
                                                             .orElseGet(testSubject(constraintLogic,
                                                                                    ctx,
                                                                                    deadboltHandler)));
        }
        return maybeBlock(result);
    }

    abstract Supplier<CompletableFuture<Result>> testSubject(final ConstraintLogic constraintLogic,
                                                              final Http.Context context,
                                                              final DeadboltHandler deadboltHandler);

    abstract CompletionStage<Result> present(Http.Context context,
                                             DeadboltHandler handler,
                                             Optional<String> content);

    abstract CompletionStage<Result> notPresent(Http.Context context,
                                                DeadboltHandler handler,
                                                Optional<String> content);

    public abstract Optional<String> getContent();

    public abstract String getHandlerKey();

    public abstract boolean isForceBeforeAuthCheck();
}
