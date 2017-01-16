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

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;

import be.objectify.deadbolt.java.ConstraintLogic;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.ExecutionContextProvider;
import be.objectify.deadbolt.java.cache.CompositeCache;
import be.objectify.deadbolt.java.cache.HandlerCache;
import be.objectify.deadbolt.java.composite.Constraint;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import play.Configuration;
import play.mvc.Http;
import scala.concurrent.ExecutionContextExecutor;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class CompositeActionTest
{
    @Test
    public void testApplyRestriction() throws Exception
    {
        final Composite composite = Mockito.mock(Composite.class);
        Mockito.when(composite.value())
               .thenReturn("foo");
        Mockito.when(composite.meta())
               .thenReturn("bar");
        Mockito.when(composite.content())
               .thenReturn("x/y");
        final CompositeCache compositeCache = Mockito.mock(CompositeCache.class);

        final Constraint constraint = Mockito.mock(Constraint.class);
        Mockito.when(constraint.test(Mockito.any(Http.Context.class),
                                     Mockito.any(DeadboltHandler.class),
                                     Mockito.any(Executor.class),
                                     Mockito.eq(Optional.of("bar")),
                                     Mockito.any(BiFunction.class)))
               .then(invocation -> ((Optional<String>)invocation.getArguments()[3]).map(meta -> meta.equals("bar"))
                                                                                   .map(CompletableFuture::completedFuture)
                                                                                   .orElse(CompletableFuture.completedFuture(false)));

        Mockito.when(compositeCache.apply("foo"))
               .thenReturn(Optional.of(constraint));

        final CompositeAction action = new CompositeAction(Mockito.mock(HandlerCache.class),
                                                           Mockito.mock(Configuration.class),
                                                           Mockito.mock(ExecutionContextProvider.class),
                                                           compositeCache,
                                                           Mockito.mock(ConstraintLogic.class))
        {
            @Override
            protected ExecutionContextExecutor executor() {
                return Mockito.mock(ExecutionContextExecutor.class);
            }
        };
        action.configuration = composite;

        final Http.Context ctx = Mockito.mock(Http.Context.class);
        final DeadboltHandler handler = Mockito.mock(DeadboltHandler.class);
        action.applyRestriction(ctx,
                                handler);

        Mockito.verify(constraint).test(Mockito.eq(ctx),
                                        Mockito.eq(handler),
                                        Mockito.any(Executor.class),
                                        Mockito.eq(Optional.of("bar")),
                                        Mockito.any(BiFunction.class));
    }

    @Test
    public void testGetMeta() throws Exception
    {
        final Composite composite = Mockito.mock(Composite.class);
        Mockito.when(composite.meta())
               .thenReturn("foo");
        final CompositeAction action = new CompositeAction(Mockito.mock(HandlerCache.class),
                                                           Mockito.mock(Configuration.class),
                                                           Mockito.mock(ExecutionContextProvider.class),
                                                           Mockito.mock(CompositeCache.class),
                                                           Mockito.mock(ConstraintLogic.class));
        action.configuration = composite;

        Assert.assertEquals("foo",
                            action.getMeta());
    }

    @Test
    public void testGetValue() throws Exception
    {
        final Composite composite = Mockito.mock(Composite.class);
        Mockito.when(composite.value())
               .thenReturn("foo");
        final CompositeAction action = new CompositeAction(Mockito.mock(HandlerCache.class),
                                                           Mockito.mock(Configuration.class),
                                                           Mockito.mock(ExecutionContextProvider.class),
                                                           Mockito.mock(CompositeCache.class),
                                                           Mockito.mock(ConstraintLogic.class));
        action.configuration = composite;

        Assert.assertEquals("foo",
                            action.getValue());
    }

    @Test
    public void testGetHandlerKey() throws Exception
    {
        final Composite composite = Mockito.mock(Composite.class);
        Mockito.when(composite.handlerKey())
               .thenReturn("foo");
        final CompositeAction action = new CompositeAction(Mockito.mock(HandlerCache.class),
                                                           Mockito.mock(Configuration.class),
                                                           Mockito.mock(ExecutionContextProvider.class),
                                                           Mockito.mock(CompositeCache.class),
                                                           Mockito.mock(ConstraintLogic.class));
        action.configuration = composite;

        Assert.assertEquals("foo",
                            action.getHandlerKey());
    }
}