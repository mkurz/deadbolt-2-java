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
package be.objectify.deadbolt.java.actions;

import be.objectify.deadbolt.java.ConstraintLogic;
import be.objectify.deadbolt.java.ConstraintPoint;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.cache.HandlerCache;
import be.objectify.deadbolt.java.utils.TriFunction;
import com.typesafe.config.ConfigFactory;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import play.mvc.Action;
import play.mvc.Http;

import java.util.Optional;
import java.util.function.Function;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class DynamicActionTest
{
    @Test
    public void testApplyRestriction() throws Exception
    {
        final Dynamic dynamic = Mockito.mock(Dynamic.class);
        Mockito.when(dynamic.value())
               .thenReturn("foo");
        Mockito.when(dynamic.meta())
               .thenReturn("bar");
        Mockito.when(dynamic.content())
               .thenReturn("x/y");
        final ConstraintLogic constraintLogic = Mockito.mock(ConstraintLogic.class);
        final DynamicAction action = new DynamicAction(Mockito.mock(HandlerCache.class),
                                                       ConfigFactory.empty(),
                                                       dynamic,
                                                       Mockito.mock(Action.class),
                                                       constraintLogic);

        final Http.Context ctx = Mockito.mock(Http.Context.class);
        final DeadboltHandler handler = Mockito.mock(DeadboltHandler.class);
        action.applyRestriction(ctx,
                                handler);

        Mockito.verify(constraintLogic).dynamic(Mockito.eq(ctx),
                                                Mockito.eq(handler),
                                                Mockito.eq(Optional.of("x/y")),
                                                Mockito.eq("foo"),
                                                Mockito.eq(Optional.of("bar")),
                                                Mockito.any(Function.class),
                                                Mockito.any(TriFunction.class),
                                                Mockito.eq(ConstraintPoint.CONTROLLER));
    }

    @Test
    public void testGetMeta() throws Exception
    {
        final Dynamic dynamic = Mockito.mock(Dynamic.class);
        Mockito.when(dynamic.meta())
               .thenReturn("foo");
        final DynamicAction action = new DynamicAction(Mockito.mock(HandlerCache.class),
                                                       ConfigFactory.empty(),
                                                       dynamic,
                                                       Mockito.mock(Action.class),
                                                       Mockito.mock(ConstraintLogic.class));

        final Optional<String> maybeMeta = action.getMeta();
        Assert.assertEquals("foo",
                            maybeMeta.orElse(null));
    }

    @Test
    public void testGetValue() throws Exception
    {
        final Dynamic dynamic = Mockito.mock(Dynamic.class);
        Mockito.when(dynamic.value())
               .thenReturn("foo");
        final DynamicAction action = new DynamicAction(Mockito.mock(HandlerCache.class),
                                                       ConfigFactory.empty(),
                                                       dynamic,
                                                       Mockito.mock(Action.class),
                                                       Mockito.mock(ConstraintLogic.class));

        Assert.assertEquals("foo",
                            action.getValue());
    }

    @Test
    public void testGetHandlerKey() throws Exception
    {
        final Dynamic dynamic = Mockito.mock(Dynamic.class);
        Mockito.when(dynamic.handlerKey())
               .thenReturn("foo");
        final DynamicAction action = new DynamicAction(Mockito.mock(HandlerCache.class),
                                                       ConfigFactory.empty(),
                                                       dynamic,
                                                       Mockito.mock(Action.class),
                                                       Mockito.mock(ConstraintLogic.class));

        Assert.assertEquals("foo",
                            action.getHandlerKey());
    }
}