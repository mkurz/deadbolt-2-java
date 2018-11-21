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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public abstract class AbstractPatternConstraintTest extends AbstractConstraintTest
{

    private Http.Request newRequest() {
        return new Http.RequestBuilder().build();
    }

    @Test
    public void testEquality_subjectHasPermission() throws Exception
    {
        final Constraint constraint = constraint(PatternType.EQUALITY,
                                                 withSubject(() -> subject(new TestPermission("foo"))));
        final CompletionStage<F.Tuple<Boolean, Http.RequestHeader>> result = constraint.test(newRequest(),
                                                                withSubject(() -> subject(new TestPermission("foo"))));
        Assert.assertTrue(toBoolean(result));
    }

    @Test
    public void testEquality_subjectDoesNotHavePermission() throws Exception
    {
        final Constraint constraint = constraint(PatternType.EQUALITY,
                                                 withSubject(() -> subject(new TestPermission("bar"))));
        final CompletionStage<F.Tuple<Boolean, Http.RequestHeader>> result = constraint.test(newRequest(),
                                                                withSubject(() -> subject(new TestPermission("bar"))));
        Assert.assertFalse(toBoolean(result));
    }

    @Test
    public void testRegex_subjectHasPermission() throws Exception
    {
        final Constraint constraint = constraint(PatternType.REGEX,
                                                 withSubject(() -> subject(new TestPermission("1"))));
        final CompletionStage<F.Tuple<Boolean, Http.RequestHeader>> result = constraint.test(newRequest(),
                                                                withSubject(() -> subject(new TestPermission("1"))));
        Assert.assertTrue(toBoolean(result));
    }

    @Test
    public void testRegex_subjectDoesNotHavePermission() throws Exception
    {
        final Constraint constraint = constraint(PatternType.REGEX,
                                                 withSubject(() -> subject(new TestPermission("3"))));
        final CompletionStage<F.Tuple<Boolean, Http.RequestHeader>> result = constraint.test(newRequest(),
                                                                withSubject(() -> subject(new TestPermission("3"))));
        Assert.assertFalse(toBoolean(result));
    }

    @Test
    public void testCustom_pass() throws Exception
    {
        final DeadboltHandler handler = withDrh(new AbstractDynamicResourceHandler()
        {
            @Override
            public CompletionStage<Boolean> checkPermission(final String permissionValue,
                                                            final Optional<String> meta,
                                                            final DeadboltHandler deadboltHandler,
                                                            final Http.RequestHeader requestHeader)
            {
                return CompletableFuture.completedFuture(true);
            }
        });
        final Constraint constraint = constraint(PatternType.CUSTOM,
                                                 handler);
        final CompletionStage<F.Tuple<Boolean, Http.RequestHeader>> result = constraint.test(newRequest(),
                                                                handler);
        Assert.assertTrue(toBoolean(result));
    }

    @Test
    public void testCustom_fail() throws Exception
    {
        final DeadboltHandler handler = withDrh(new AbstractDynamicResourceHandler()
        {
            @Override
            public CompletionStage<Boolean> checkPermission(final String permissionValue,
                                                            final Optional<String> meta,
                                                            final DeadboltHandler deadboltHandler,
                                                            final Http.RequestHeader requestHeader)
            {
                return CompletableFuture.completedFuture(false);
            }
        });
        final Constraint constraint = constraint(PatternType.CUSTOM,
                                                 handler);
        final CompletionStage<F.Tuple<Boolean, Http.RequestHeader>> result = constraint.test(newRequest(),
                                                                handler);
        Assert.assertFalse(toBoolean(result));
    }

    protected abstract PatternConstraint constraint(PatternType patternType,
                                                    final DeadboltHandler handler);

    @Override
    protected F.Tuple<Constraint, Function<Constraint, CompletionStage<F.Tuple<Boolean, Http.RequestHeader>>>> satisfy()
    {
        return new F.Tuple<>(constraint(PatternType.EQUALITY,
                                        withSubject(() -> subject(new TestPermission("foo")))),
                             c -> c.test(newRequest(),
                                         withSubject(() -> subject(new TestPermission("foo")))));
    }
}