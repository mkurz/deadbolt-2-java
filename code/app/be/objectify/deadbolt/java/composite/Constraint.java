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
package be.objectify.deadbolt.java.composite;

import be.objectify.deadbolt.java.DeadboltHandler;
import play.mvc.Http;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@FunctionalInterface
public interface Constraint
{
    default CompletionStage<Boolean> test(Http.Context context,
                                          DeadboltHandler handler)
    {
        return test(context,
                    handler,
                    Optional.empty(),
                    (globalMeta, localMeta) -> localMeta);
    }

    CompletionStage<Boolean> test(Http.Context context,
                                  DeadboltHandler handler,
                                  Optional<String> globalMetaData,
                                  BiFunction<Optional<String>, Optional<String>, Optional<String>> metaFn);

    default Constraint and(final Constraint other)
    {
        Objects.requireNonNull(other);
        return (ctx, handler, global, fMeta) ->
                test(ctx, handler, global, fMeta).thenCompose(passed1 -> passed1 ? other.test(ctx, handler, global, fMeta).thenApply(passed2 -> passed2)
                                                                                 : CompletableFuture.completedFuture(false));
    }

    default Constraint negate()
    {
        return (ctx, handler, global, fMeta) -> test(ctx, handler, global, fMeta).thenApplyAsync(p -> !p);
    }

    default Constraint or(final Constraint other)
    {
        Objects.requireNonNull(other);
        return (ctx, handler, global, fMeta) ->
                test(ctx, handler, global, fMeta).thenCompose(passed1 -> passed1 ? CompletableFuture.completedFuture(true)
                                                                                                : other.test(ctx, handler, global, fMeta).thenApply(passed2 -> passed2));
    }
}
