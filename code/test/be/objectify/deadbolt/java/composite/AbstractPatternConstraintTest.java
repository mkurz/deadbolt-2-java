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

import be.objectify.deadbolt.java.AbstractDynamicResourceHandler;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.models.PatternType;
import be.objectify.deadbolt.java.testsupport.TestPermission;
import org.junit.Assert;
import org.junit.Test;
import play.libs.F;
import play.mvc.Http;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.function.Function;

public abstract class AbstractPatternConstraintTest extends AbstractConstraintTest
{
    @Test
    public void testEquality_subjectHasPermission() throws Exception
    {
        final Constraint constraint = constraint(PatternType.EQUALITY);
        final CompletionStage<Boolean> result = constraint.test(context,
                                                                withSubject(() -> subject(new TestPermission("foo"))),
                                                                Executors.newSingleThreadExecutor());
        Assert.assertTrue(toBoolean(result));
    }

    @Test
    public void testEquality_subjectDoesNotHavePermission() throws Exception
    {
        final Constraint constraint = constraint(PatternType.EQUALITY);
        final CompletionStage<Boolean> result = constraint.test(context,
                                                                withSubject(() -> subject(new TestPermission("bar"))),
                                                                Executors.newSingleThreadExecutor());
        Assert.assertFalse(toBoolean(result));
    }

    @Test
    public void testRegex_subjectHasPermission() throws Exception
    {
        final Constraint constraint = constraint(PatternType.REGEX);
        final CompletionStage<Boolean> result = constraint.test(context,
                                                                withSubject(() -> subject(new TestPermission("1"))),
                                                                Executors.newSingleThreadExecutor());
        Assert.assertTrue(toBoolean(result));
    }

    @Test
    public void testRegex_subjectDoesNotHavePermission() throws Exception
    {
        final Constraint constraint = constraint(PatternType.REGEX);
        final CompletionStage<Boolean> result = constraint.test(context,
                                                                withSubject(() -> subject(new TestPermission("3"))),
                                                                Executors.newSingleThreadExecutor());
        Assert.assertFalse(toBoolean(result));
    }

    @Test
    public void testCustom_pass() throws Exception
    {
        final Constraint constraint = constraint(PatternType.CUSTOM);
        final CompletionStage<Boolean> result = constraint.test(context,
                                                                withDrh(new AbstractDynamicResourceHandler()
                                                                {
                                                                    @Override
                                                                    public CompletionStage<Boolean> checkPermission(final String permissionValue,
                                                                                                                    final DeadboltHandler deadboltHandler,
                                                                                                                    final Http.Context ctx)
                                                                    {
                                                                        return CompletableFuture.completedFuture(true);
                                                                    }
                                                                }),
                                                                Executors.newSingleThreadExecutor());
        Assert.assertTrue(toBoolean(result));
    }

    @Test
    public void testCustom_fail() throws Exception
    {
        final Constraint constraint = constraint(PatternType.CUSTOM);
        final CompletionStage<Boolean> result = constraint.test(context,
                                                                withDrh(new AbstractDynamicResourceHandler()
                                                                {
                                                                    @Override
                                                                    public CompletionStage<Boolean> checkPermission(final String permissionValue,
                                                                                                                    final DeadboltHandler deadboltHandler,
                                                                                                                    final Http.Context ctx)
                                                                    {
                                                                        return CompletableFuture.completedFuture(false);
                                                                    }
                                                                }),
                                                                Executors.newSingleThreadExecutor());
        Assert.assertFalse(toBoolean(result));
    }

    protected abstract PatternConstraint constraint(PatternType patternType);

    @Override
    protected F.Tuple<Constraint, Function<Constraint, CompletionStage<Boolean>>> satisfy()
    {
        return new F.Tuple<>(constraint(PatternType.EQUALITY),
                             c -> c.test(context,
                                         withSubject(() -> subject(new TestPermission("foo"))),
                                         Executors.newSingleThreadExecutor()));
    }
}