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

import be.objectify.deadbolt.java.ConstraintLogic;
import be.objectify.deadbolt.java.ConstraintPoint;
import be.objectify.deadbolt.java.DeadboltHandler;
import play.mvc.Http;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class RestrictConstraint implements Constraint
{
    private final List<String[]> roleGroups = new LinkedList<>();
    private final ConstraintLogic constraintLogic;
    private final Optional<String> content;

    public RestrictConstraint(final List<String[]> roleGroups,
                              final Optional<String> content,
                              final ConstraintLogic constraintLogic)
    {
        this.content = content;
        roleGroups.stream()
                  .filter(Objects::nonNull)
                  .collect(Collectors.toCollection(() -> this.roleGroups));
        this.constraintLogic = constraintLogic;
    }

    @Override
    public CompletionStage<Boolean> test(final Http.Context context,
                                         final DeadboltHandler handler,
                                         final Executor executor,
                                         final Optional<String> globalMetaData,
                                         final BiFunction<Optional<String>, Optional<String>, Optional<String>> metaFn)
    {
        return constraintLogic.restrict(context,
                                        handler,
                                        content,
                                        () -> roleGroups,
                                        ctx -> CompletableFuture.completedFuture(Boolean.TRUE),
                                        (ctx, dh, cnt) -> CompletableFuture.completedFuture(Boolean.FALSE),
                                        ConstraintPoint.CONTROLLER);
    }
}
