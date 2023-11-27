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
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import play.libs.F;
import play.mvc.Http;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public abstract class AbstractConstraintTest extends AbstractCompositeTest
{

    @Test
    public void testAnd() throws Exception
    {
        final Constraint c2 = Mockito.mock(Constraint.class);
        Mockito.when(c2.test(Mockito.any(Http.Request.class),
                             Mockito.any(DeadboltHandler.class),
                             Mockito.any(Optional.class),
                             Mockito.any(BiFunction.class)))
               .thenReturn(CompletableFuture.completedFuture(F.Tuple(false, new Http.RequestBuilder().build())));
        final Constraint negated = Mockito.mock(Constraint.class);
        Mockito.when(negated.test(Mockito.any(Http.Request.class),
                                  Mockito.any(DeadboltHandler.class),
                                  Mockito.any(Optional.class),
                                  Mockito.any(BiFunction.class)))
               .thenReturn(CompletableFuture.completedFuture(F.Tuple(true, new Http.RequestBuilder().build())));
        Mockito.when(c2.negate())
               .thenReturn(negated);
        final F.Tuple<Constraint, Function<Constraint, CompletionStage<F.Tuple<Boolean, Http.RequestHeader>>>> satisfy = satisfy();
        Assert.assertTrue(toBoolean(satisfy._2.apply(satisfy._1)));
        Assert.assertFalse(toBoolean(satisfy._2.apply(satisfy._1.and(c2))));
        Assert.assertTrue(toBoolean(satisfy._2.apply(satisfy._1.and(c2.negate()))));
    }

    @Test
    public void testOr() throws Exception
    {
        final Constraint c2 = Mockito.mock(Constraint.class);
        Mockito.when(c2.test(Mockito.any(Http.Request.class),
                             Mockito.any(DeadboltHandler.class),
                             Mockito.any(Optional.class),
                             Mockito.any(BiFunction.class)))
               .thenReturn(CompletableFuture.completedFuture(F.Tuple(false, new Http.RequestBuilder().build())));
        final F.Tuple<Constraint, Function<Constraint, CompletionStage<F.Tuple<Boolean, Http.RequestHeader>>>> satisfy = satisfy();
        Assert.assertTrue(toBoolean(satisfy._2.apply(satisfy._1)));
        Assert.assertTrue(toBoolean(satisfy._2.apply(satisfy._1.or(c2))));
        Assert.assertFalse(toBoolean(satisfy._2.apply(satisfy._1.negate().or(c2))));
    }

    @Test
    public void testNegate() throws Exception
    {
        final F.Tuple<Constraint, Function<Constraint, CompletionStage<F.Tuple<Boolean, Http.RequestHeader>>>> satisfy = satisfy();
        Assert.assertTrue(toBoolean(satisfy._2.apply(satisfy._1)));
        Assert.assertFalse(toBoolean(satisfy._2.apply(satisfy._1.negate())));
    }

    protected abstract F.Tuple<Constraint, Function<Constraint, CompletionStage<F.Tuple<Boolean, Http.RequestHeader>>>> satisfy();
}
