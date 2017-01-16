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

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.ExecutionContextProvider;
import be.objectify.deadbolt.java.cache.HandlerCache;
import org.junit.Test;
import org.mockito.Mockito;
import play.Configuration;
import play.libs.concurrent.HttpExecution;
import play.mvc.Action;
import play.mvc.Http;
import scala.concurrent.ExecutionContextExecutor;

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
        Mockito.when(handler.beforeAuthCheck(ctx))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        final HandlerCache handlerCache = Mockito.mock(HandlerCache.class);
        Mockito.when(handlerCache.get())
               .thenReturn(handler);

        final BeforeAccessAction action = new BeforeAccessAction(handlerCache,
                                                                 Mockito.mock(Configuration.class),
                                                                 Mockito.mock(ExecutionContextProvider.class))
        {
            @Override
            protected ExecutionContextExecutor executor() {
                return HttpExecution.defaultContext();
            }
        };
        action.configuration = Mockito.mock(BeforeAccess.class);
        Mockito.when(action.configuration.alwaysExecute())
               .thenReturn(true);
        action.delegate = Mockito.mock(Action.class);

        action.execute(ctx);

        Mockito.verify(handler).beforeAuthCheck(ctx);
    }

    @Test
    public void testExecute_alreadyAuthorised_alwaysExecuteFalse() throws Exception
    {
        final BeforeAccessAction action = new BeforeAccessAction(Mockito.mock(HandlerCache.class),
                                                                 Mockito.mock(Configuration.class),
                                                                 Mockito.mock(ExecutionContextProvider.class));
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