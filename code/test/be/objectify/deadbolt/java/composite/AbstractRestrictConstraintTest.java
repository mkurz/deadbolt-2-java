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
import be.objectify.deadbolt.java.testsupport.TestRole;
import org.junit.Assert;
import org.junit.Test;
import play.libs.F;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public abstract class AbstractRestrictConstraintTest extends AbstractConstraintTest
{
    @Test
    public void testSingleRole_present() throws Exception
    {
        final Constraint constraint = constraint(withSubject(() -> subject(new TestRole("foo"))),
                                                 Collections.singletonList(new String[]{"foo"}));
        final CompletionStage<Boolean> result = constraint.test(context,
                                                                withSubject(() -> subject(new TestRole("foo"))),
                                                                Executors.newSingleThreadExecutor());
        Assert.assertTrue(toBoolean(result));
    }

    @Test
    public void testSingleRole_notPresent() throws Exception
    {
        final Constraint constraint = constraint(withSubject(() -> subject(new TestRole("bar"))),
                                                 Collections.singletonList(new String[]{"foo"}));
        final CompletionStage<Boolean> result = constraint.test(context,
                                                                withSubject(() -> subject(new TestRole("bar"))),
                                                                Executors.newSingleThreadExecutor());
        Assert.assertFalse(toBoolean(result));
    }

    @Test
    public void testSingleRole_present_negated() throws Exception
    {
        final Constraint constraint = constraint(withSubject(() -> subject(new TestRole("foo"))),
                                                 Collections.singletonList(new String[]{"!foo"}));
        final CompletionStage<Boolean> result = constraint.test(context,
                                                                withSubject(() -> subject(new TestRole("foo"))),
                                                                Executors.newSingleThreadExecutor());
        Assert.assertFalse(toBoolean(result));
    }

    @Test
    public void testSingleRole_notPresent_negated() throws Exception
    {
        final Constraint constraint = constraint(withSubject(() -> subject(new TestRole("bar"))),
                                                 Collections.singletonList(new String[]{"!foo"}));
        final CompletionStage<Boolean> result = constraint.test(context,
                                                                withSubject(() -> subject(new TestRole("bar"))),
                                                                Executors.newSingleThreadExecutor());
        Assert.assertTrue(toBoolean(result));
    }

    @Test
    public void testAnd_present() throws Exception
    {
        final Constraint constraint = constraint(withSubject(() -> subject(new TestRole("foo"),
                                                                           new TestRole("bar"))),
                                                 Collections.singletonList(new String[]{"foo", "bar"}));
        final CompletionStage<Boolean> result = constraint.test(context,
                                                                withSubject(() -> subject(new TestRole("foo"),
                                                                                          new TestRole("bar"))),
                                                                Executors.newSingleThreadExecutor());
        Assert.assertTrue(toBoolean(result));
    }

    @Test
    public void testAnd_notPresent() throws Exception
    {
        final Constraint constraint = constraint(withSubject(() -> subject(new TestRole("foo"),
                                                                           new TestRole("hurdy"))),
                                                 Collections.singletonList(new String[]{"foo", "bar"}));
        final CompletionStage<Boolean> result = constraint.test(context,
                                                                withSubject(() -> subject(new TestRole("foo"),
                                                                                          new TestRole("hurdy"))),
                                                                Executors.newSingleThreadExecutor());
        Assert.assertFalse(toBoolean(result));
    }

    @Test
    public void testOr_present() throws Exception
    {
        final Constraint constraint = constraint(withSubject(() -> subject(new TestRole("foo"),
                                                                           new TestRole("bar"))),
                                                 Arrays.asList(new String[]{"foo", "bar"},
                                                               new String[]{"hurdy", "gurdy"}));
        final CompletionStage<Boolean> result = constraint.test(context,
                                                                withSubject(() -> subject(new TestRole("foo"),
                                                                                          new TestRole("bar"))),
                                                                Executors.newSingleThreadExecutor());
        Assert.assertTrue(toBoolean(result));
    }

    @Test
    public void testOr_notPresent() throws Exception
    {
        final Constraint constraint = constraint(withSubject(() -> subject(new TestRole("hurdy"))),
                                                 Arrays.asList(new String[]{"foo"},
                                                               new String[]{"bar"}));
        final CompletionStage<Boolean> result = constraint.test(context,
                                                                withSubject(() -> subject(new TestRole("hurdy"))),
                                                                Executors.newSingleThreadExecutor());
        Assert.assertFalse(toBoolean(result));
    }

    @Override
    protected F.Tuple<Constraint, Function<Constraint, CompletionStage<Boolean>>> satisfy()
    {
        return new F.Tuple<>(constraint(withSubject(() -> subject(new TestRole("foo"))),
                                        Collections.singletonList(new String[]{"foo"})),
                             c -> c.test(context,
                                         withSubject(() -> subject(new TestRole("foo"))),
                                         Executors.newSingleThreadExecutor()));
    }

    protected abstract RestrictConstraint constraint(DeadboltHandler handler,
                                                     List<String[]> roleGroups);
}