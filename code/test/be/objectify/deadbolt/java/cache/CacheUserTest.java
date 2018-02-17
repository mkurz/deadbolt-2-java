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
package be.objectify.deadbolt.java.cache;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import org.junit.Test;
import org.mockito.Mockito;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.DefaultDeadboltExecutionContextProvider;
import be.objectify.deadbolt.java.ExecutionContextProvider;
import be.objectify.deadbolt.java.models.Subject;
import play.core.j.HttpExecutionContext;
import play.mvc.Http;

/**
 * @author Matthias Kurz (m.kurz@irregular.at)
 */
public class CacheUserTest
{

    @Test
    public void testCacheUserWithSubjectPresent() throws Exception
    {
        final Http.Context ctx = Mockito.mock(Http.Context.class);
        final DeadboltHandler handler = getHandler("deadbolt.java.cache-user = true", ctx, Mockito.mock(Subject.class));

        // should be called only once per handler
        Mockito.verify(handler, Mockito.times(2)).getSubject(ctx);
    }

    @Test
    public void testCacheUserWithSubjectNotPresent() throws Exception
    {
        final Http.Context ctx = Mockito.mock(Http.Context.class);
        final DeadboltHandler handler = getHandler("deadbolt.java.cache-user = true", ctx, null);

        // even though we cache, there is not user so we try each time
        Mockito.verify(handler, Mockito.times(9)).getSubject(ctx);
    }

    @Test
    public void testDontCacheUserWithSubjectPresent() throws Exception
    {
        final Http.Context ctx = Mockito.mock(Http.Context.class);
        final DeadboltHandler handler = getHandler("deadbolt.java.cache-user = false", ctx, Mockito.mock(Subject.class));

        // we don't cache, so even there is a user we run the method each time
        Mockito.verify(handler, Mockito.times(9)).getSubject(ctx);
    }

    @Test
    public void testDontCacheUserWithSubjectNotPresent() throws Exception
    {
        final Http.Context ctx = Mockito.mock(Http.Context.class);
        final DeadboltHandler handler = getHandler("deadbolt.java.cache-user = false", ctx, null);

        // we don't cache and even there is no user, of course run the method each time
        Mockito.verify(handler, Mockito.times(9)).getSubject(ctx);
    }

    private static DeadboltHandler getHandler(final String setting, final Http.Context ctx, final Subject subject) throws Exception
    {
        ctx.args = new HashMap<>();

        final Config config = ConfigFactory.parseString(setting);

        final ExecutionContextProvider ecProvider = Mockito.mock(ExecutionContextProvider.class);
        Mockito.when(ecProvider.get()).thenReturn(new DefaultDeadboltExecutionContextProvider(HttpExecutionContext.fromThread(Executors.newSingleThreadExecutor())));

        final SubjectCache subjectCache = new DefaultSubjectCache(config, ecProvider);

        final DeadboltHandler handler = Mockito.mock(DeadboltHandler.class);
        Mockito.when(handler.getSubject(ctx))
               .thenReturn(CompletableFuture.completedFuture(Optional.ofNullable(subject)));

        // Fake the first handler
        Mockito.when(handler.getId())
                .thenReturn(Long.valueOf(0));

        // Lets call apply a couple of times for the first handler
        subjectCache.apply(handler, ctx);
        subjectCache.apply(handler, ctx);
        subjectCache.apply(handler, ctx);
        subjectCache.apply(handler, ctx);

        // Let's fake a second handler
        Mockito.when(handler.getId())
            .thenReturn(Long.valueOf(1));

        // Lets call apply a couple of times for the second handler
        subjectCache.apply(handler, ctx);
        subjectCache.apply(handler, ctx);
        subjectCache.apply(handler, ctx);
        subjectCache.apply(handler, ctx);
        subjectCache.apply(handler, ctx);

        return handler;
    }
}
