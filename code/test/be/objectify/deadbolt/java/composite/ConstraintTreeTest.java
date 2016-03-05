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
package be.objectify.deadbolt.java.composite;

import be.objectify.deadbolt.java.DeadboltHandler;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import play.libs.F;
import play.mvc.Http;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class ConstraintTreeTest extends AbstractConstraintTest
{
    private final Http.Context context = Mockito.mock(Http.Context.class);
    private final DeadboltHandler handler = Mockito.mock(DeadboltHandler.class);

    @Test
    public void testAnd_false_false() throws Exception
    {
        final Constraint constraint = (c, h, e) -> CompletableFuture.completedFuture(false);
        final Constraint tree = new ConstraintTree(Operator.AND,
                                                   constraint,
                                                   constraint);

        final CompletionStage<Boolean> result = tree.test(context,
                                                          handler,
                                                          Executors.newSingleThreadExecutor());
        Assert.assertFalse(toBoolean(result));
    }

    @Test
    public void testAnd_true_false() throws Exception
    {
        final Constraint c1 = (c, h, e) -> CompletableFuture.completedFuture(true);
        final Constraint c2 = (c, h, e) -> CompletableFuture.completedFuture(false);
        final Constraint tree = new ConstraintTree(Operator.AND,
                                                   c1,
                                                   c2);

        final CompletionStage<Boolean> result = tree.test(context,
                                                          handler,
                                                          Executors.newSingleThreadExecutor());
        Assert.assertFalse(toBoolean(result));
    }

    @Test
    public void testAnd_false_true() throws Exception
    {
        final Constraint c1 = (c, h, e) -> CompletableFuture.completedFuture(false);
        final Constraint c2 = (c, h, e) -> CompletableFuture.completedFuture(true);
        final Constraint tree = new ConstraintTree(Operator.AND,
                                                   c1,
                                                   c2);

        final CompletionStage<Boolean> result = tree.test(context,
                                                          handler,
                                                          Executors.newSingleThreadExecutor());
        Assert.assertFalse(toBoolean(result));
    }

    @Test
    public void testAnd_true_true() throws Exception
    {
        final Constraint c1 = (c, h, e) -> CompletableFuture.completedFuture(true);
        final Constraint c2 = (c, h, e) -> CompletableFuture.completedFuture(true);
        final Constraint tree = new ConstraintTree(Operator.AND,
                                                   c1,
                                                   c2);

        final CompletionStage<Boolean> result = tree.test(context,
                                                          handler,
                                                          Executors.newSingleThreadExecutor());
        Assert.assertTrue(toBoolean(result));
    }

    @Test
    public void testOr_false_false() throws Exception
    {
        final Constraint constraint = (c, h, e) -> CompletableFuture.completedFuture(false);
        final Constraint tree = new ConstraintTree(Operator.OR,
                                                   constraint,
                                                   constraint);

        final CompletionStage<Boolean> result = tree.test(context,
                                                          handler,
                                                          Executors.newSingleThreadExecutor());
        Assert.assertFalse(toBoolean(result));
    }

    @Test
    public void testOr_true_false() throws Exception
    {
        final Constraint c1 = (c, h, e) -> CompletableFuture.completedFuture(true);
        final Constraint c2 = (c, h, e) -> CompletableFuture.completedFuture(false);
        final Constraint tree = new ConstraintTree(Operator.OR,
                                                   c1,
                                                   c2);

        final CompletionStage<Boolean> result = tree.test(context,
                                                          handler,
                                                          Executors.newSingleThreadExecutor());
        Assert.assertTrue(toBoolean(result));
    }

    @Test
    public void testOr_false_true() throws Exception
    {
        final Constraint c1 = (c, h, e) -> CompletableFuture.completedFuture(false);
        final Constraint c2 = (c, h, e) -> CompletableFuture.completedFuture(true);
        final Constraint tree = new ConstraintTree(Operator.OR,
                                                   c1,
                                                   c2);

        final CompletionStage<Boolean> result = tree.test(context,
                                                          handler,
                                                          Executors.newSingleThreadExecutor());
        Assert.assertTrue(toBoolean(result));
    }

    @Test
    public void testOr_true_true() throws Exception
    {
        final Constraint c1 = (c, h, e) -> CompletableFuture.completedFuture(true);
        final Constraint c2 = (c, h, e) -> CompletableFuture.completedFuture(true);
        final Constraint tree = new ConstraintTree(Operator.OR,
                                                   c1,
                                                   c2);

        final CompletionStage<Boolean> result = tree.test(context,
                                                          handler,
                                                          Executors.newSingleThreadExecutor());
        Assert.assertTrue(toBoolean(result));
    }

    @Override
    protected F.Tuple<Constraint, Function<Constraint, CompletionStage<Boolean>>> satisfy()
    {
        final Constraint constraint = (c, h, e) -> CompletableFuture.completedFuture(true);
        final Constraint tree = new ConstraintTree(Operator.AND,
                                                   constraint,
                                                   constraint);
        return new F.Tuple<>(tree,
                             c -> c.test(context,
                                         handler,
                                         Executors.newSingleThreadExecutor()));
    }
}