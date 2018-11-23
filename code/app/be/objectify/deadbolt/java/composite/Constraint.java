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
import play.libs.F;
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
    default CompletionStage<F.Tuple<Boolean, Http.RequestHeader>> test(Http.RequestHeader requestHeader,
                                          DeadboltHandler handler)
    {
        return test(requestHeader,
                    handler,
                    Optional.empty(),
                    (globalMeta, localMeta) -> localMeta);
    }

    CompletionStage<F.Tuple<Boolean, Http.RequestHeader>> test(Http.RequestHeader requestHeader,
                                  DeadboltHandler handler,
                                  Optional<String> globalMetaData,
                                  BiFunction<Optional<String>, Optional<String>, Optional<String>> metaFn);

    default Constraint and(final Constraint other)
    {
        Objects.requireNonNull(other);
        return (rh, handler, global, fMeta) ->
                test(rh, handler, global, fMeta).thenCompose(passed1 -> passed1._1 ? other.test(passed1._2, handler, global, fMeta).thenApply(passed2 -> passed2)
                                                                                 : CompletableFuture.completedFuture(F.Tuple(false, passed1._2)));
    }

    default Constraint negate()
    {
        return (rh, handler, global, fMeta) -> test(rh, handler, global, fMeta).thenApply(p -> F.Tuple(!p._1, p._2));
    }

    default Constraint or(final Constraint other)
    {
        Objects.requireNonNull(other);
        return (rh, handler, global, fMeta) ->
                test(rh, handler, global, fMeta).thenCompose(passed1 -> passed1._1 ? CompletableFuture.completedFuture(F.Tuple(true, passed1._2))
                                                                                                : other.test(passed1._2, handler, global, fMeta).thenApply(passed2 -> passed2));
    }
}
