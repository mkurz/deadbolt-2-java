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

import be.objectify.deadbolt.java.AbstractDynamicResourceHandler;
import be.objectify.deadbolt.java.DeadboltHandler;
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
public abstract class AbstractDynamicConstraintTest extends AbstractConstraintTest
{
    protected abstract DynamicConstraint constraint(final DeadboltHandler handler);

    @Test
    public void testPass() throws Exception
    {
        final DeadboltHandler handler = withDrh(new AbstractDynamicResourceHandler()
        {
            @Override
            public CompletionStage<Boolean> isAllowed(final String name,
                                                      final Optional<String> meta,
                                                      final DeadboltHandler deadboltHandler,
                                                      final Http.RequestHeader requestHeader)
            {
                return CompletableFuture.completedFuture(true);
            }
        });
        final DynamicConstraint constraint = constraint(handler);
        final CompletionStage<F.Tuple<Boolean, Http.RequestHeader>> result = constraint.test(new Http.RequestBuilder().build(),
                                                                handler);
        Assert.assertTrue(toBoolean(result));
    }

    @Test
    public void testFail() throws Exception
    {
        final DeadboltHandler handler = withDrh(new AbstractDynamicResourceHandler()
        {
            @Override
            public CompletionStage<Boolean> isAllowed(final String name,
                                                      final Optional<String> meta,
                                                      final DeadboltHandler deadboltHandler,
                                                      final Http.RequestHeader requestHeader)
            {
                return CompletableFuture.completedFuture(false);
            }
        });
        final DynamicConstraint constraint = constraint(handler);
        final CompletionStage<F.Tuple<Boolean, Http.RequestHeader>> result = constraint.test(new Http.RequestBuilder().build(),
                                                                handler);
        Assert.assertFalse(toBoolean(result));
    }

    @Override
    protected F.Tuple<Constraint, Function<Constraint, CompletionStage<F.Tuple<Boolean, Http.RequestHeader>>>> satisfy()
    {
        final DeadboltHandler handler = withDrh(new AbstractDynamicResourceHandler()
        {
            @Override
            public CompletionStage<Boolean> isAllowed(final String name,
                                                      final Optional<String> meta,
                                                      final DeadboltHandler deadboltHandler,
                                                      final Http.RequestHeader requestHeader)
            {
                return CompletableFuture.completedFuture(true);
            }
        });
        return new F.Tuple<>(constraint(handler),
                             c -> c.test(new Http.RequestBuilder().build(),
                                         handler));
    }
}
