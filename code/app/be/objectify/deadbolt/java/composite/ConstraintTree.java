/*
 * Copyright 2010-2016 Steve Chaloner
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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * A list of constraints combined by {@link Operator}.  Those constraints may themselves be trees.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public class ConstraintTree implements Constraint
{
    private final Operator operator;

    private final List<Constraint> constraints = new LinkedList<>();

    public ConstraintTree(final Operator operator,
                          final Constraint... constraints)
    {
        this.operator = operator;
        Arrays.stream(constraints)
              .filter(c -> c != null)
              .collect(Collectors.toCollection(() -> this.constraints));
    }

    @Override
    public CompletionStage<Boolean> test(final Http.Context context,
                                         final DeadboltHandler handler,
                                         final Executor executor)
    {
        final CompletionStage<Boolean> result;
        if (constraints.isEmpty()) {
            result = CompletableFuture.completedFuture(false);
        }
        else
        {
            Constraint current = constraints.get(0);
            for (int i = 1; i < constraints.size(); i++)
            {
                current = operator.apply(current,
                                         constraints.get(i));
            }
            result = current.test(context,
                                  handler,
                                  executor);
        }
        return result;
    }
}
