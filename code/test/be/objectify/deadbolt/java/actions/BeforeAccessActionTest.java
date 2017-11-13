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

import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.cache.BeforeAuthCheckCache;
import be.objectify.deadbolt.java.cache.DefaultBeforeAuthCheckCache;
import be.objectify.deadbolt.java.cache.HandlerCache;
import com.typesafe.config.ConfigFactory;
import org.junit.Test;
import org.mockito.Mockito;
import play.mvc.Action;
import play.mvc.Http;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class BeforeAccessActionTest
{
    @Test
    public void testExecute_alreadyAuthorised_alwaysExecuteTrue() throws Exception
    {
        final Http.Context ctx = Mockito.mock(Http.Context.class);
        ctx.args = new HashMap<>();
        ctx.args.put("deadbolt.action-authorised",
                     true);

        final DeadboltHandler handler = Mockito.mock(DeadboltHandler.class);
        Mockito.when(handler.beforeAuthCheck(ctx, Optional.empty()))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        final HandlerCache handlerCache = Mockito.mock(HandlerCache.class);
        Mockito.when(handlerCache.get())
               .thenReturn(handler);

        final BeforeAuthCheckCache beforeAuthCheckCache = new DefaultBeforeAuthCheckCache(ConfigFactory.empty());

        final BeforeAccessAction action = new BeforeAccessAction(handlerCache,
                                                                 beforeAuthCheckCache,
                                                                 ConfigFactory.empty());
        action.configuration = Mockito.mock(BeforeAccess.class);
        Mockito.when(action.configuration.alwaysExecute())
               .thenReturn(true);
        action.delegate = Mockito.mock(Action.class);

        action.execute(ctx);

        Mockito.verify(handler).beforeAuthCheck(ctx, Optional.empty());
    }

    @Test
    public void testExecute_alreadyAuthorised_alwaysExecuteFalse() throws Exception
    {
        final BeforeAccessAction action = new BeforeAccessAction(Mockito.mock(HandlerCache.class),
                                                                 Mockito.mock(BeforeAuthCheckCache.class),
                                                                 ConfigFactory.empty());
        action.configuration = Mockito.mock(BeforeAccess.class);
        Mockito.when(action.configuration.alwaysExecute())
               .thenReturn(false);
        action.delegate = Mockito.mock(Action.class);

        final Http.Context ctx = Mockito.mock(Http.Context.class);
        ctx.args = new HashMap<>();
        ctx.args.put("deadbolt.action-authorised",
                     true);

        action.execute(ctx);

        Mockito.verify(action.delegate).call(ctx);
    }
}