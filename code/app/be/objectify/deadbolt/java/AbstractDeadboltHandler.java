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
package be.objectify.deadbolt.java;

import be.objectify.deadbolt.core.models.Subject;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import views.html.defaultpages.unauthorized;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Abstract implementation of {@link DeadboltHandler} that gives a standard unauthorised result when access fails.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public abstract class AbstractDeadboltHandler extends Results implements DeadboltHandler
{
    /**
     * {@inheritDoc}
     */
    public CompletionStage<Optional<Subject>> getSubject(final Http.Context context)
    {
        return CompletableFuture.completedFuture(Optional.empty());
    }

    /**
     * {@inheritDoc}
     */
    public CompletionStage<Result> onAuthFailure(final Http.Context context,
                                           final String content)
    {
        return CompletableFuture.supplyAsync(unauthorized::render)
                                .thenApply(Results::unauthorized);
    }

    /**
     * {@inheritDoc}
     */
    public CompletionStage<Optional<DynamicResourceHandler>> getDynamicResourceHandler(final Http.Context context)
    {
        return CompletableFuture.completedFuture(Optional.empty());
    }
}
