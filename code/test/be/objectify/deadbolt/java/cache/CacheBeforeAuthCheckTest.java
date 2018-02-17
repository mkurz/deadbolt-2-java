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

import org.junit.Test;
import org.mockito.Mockito;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import be.objectify.deadbolt.java.DeadboltHandler;
import play.mvc.Http;
import play.mvc.Result;

/**
 * @author Matthias Kurz (m.kurz@irregular.at)
 */
public class CacheBeforeAuthCheckTest
{

    @Test
    public void testCacheBeforeAuthCheckWithResultPresent() throws Exception
    {
        final Http.Context ctx = Mockito.mock(Http.Context.class);
        final DeadboltHandler handler = getHandler("deadbolt.java.cache-before-auth-check = true", ctx, Mockito.mock(Result.class));

        // Because we return a result, we don't cache anything. In a real app that result whould be displayed
        Mockito.verify(handler, Mockito.times(9)).beforeAuthCheck(ctx, Optional.empty());
    }

    @Test
    public void testCacheBeforeAuthCheckWithResultNotPresent() throws Exception
    {
        final Http.Context ctx = Mockito.mock(Http.Context.class);
        final DeadboltHandler handler = getHandler("deadbolt.java.cache-before-auth-check = true", ctx, null);

        // beforeAuthCheck returns empty, that means it "passes" and we can cache that "pass" (once per handler)
        Mockito.verify(handler, Mockito.times(2)).beforeAuthCheck(ctx, Optional.empty());
    }

    @Test
    public void testDontCacheBeforeAuthCheckWithResultPresent() throws Exception
    {
        final Http.Context ctx = Mockito.mock(Http.Context.class);
        final DeadboltHandler handler = getHandler("deadbolt.java.cache-before-auth-check = false", ctx, Mockito.mock(Result.class));

        // we don't cache and even there would be a result, of course we run the method each time
        Mockito.verify(handler, Mockito.times(9)).beforeAuthCheck(ctx, Optional.empty());
    }

    @Test
    public void testDontCacheBeforeAuthCheckWithResultNotPresent() throws Exception
    {
        final Http.Context ctx = Mockito.mock(Http.Context.class);
        final DeadboltHandler handler = getHandler("deadbolt.java.cache-before-auth-check = false", ctx, null);

        // we don't cache, so even if there would be empty to cache we run the method each time
        Mockito.verify(handler, Mockito.times(9)).beforeAuthCheck(ctx, Optional.empty());
    }

    private static DeadboltHandler getHandler(final String setting, final Http.Context ctx, final Result result) throws Exception
    {
        ctx.args = new HashMap<>();

        final Config config = ConfigFactory.parseString(setting);

        final BeforeAuthCheckCache beforeAuthCheckCache = new DefaultBeforeAuthCheckCache(config);

        final DeadboltHandler handler = Mockito.mock(DeadboltHandler.class);
        Mockito.when(handler.beforeAuthCheck(ctx, Optional.empty()))
               .thenReturn(CompletableFuture.completedFuture(Optional.ofNullable(result)));

        // Fake the first handler
        Mockito.when(handler.getId())
                .thenReturn(Long.valueOf(0));

        // Lets call apply a couple of times for the first handler
        beforeAuthCheckCache.apply(handler, ctx, Optional.empty());
        beforeAuthCheckCache.apply(handler, ctx, Optional.empty());
        beforeAuthCheckCache.apply(handler, ctx, Optional.empty());
        beforeAuthCheckCache.apply(handler, ctx, Optional.empty());

        // Let's fake a second handler
        Mockito.when(handler.getId())
            .thenReturn(Long.valueOf(1));

        // Lets call apply a couple of times for the second handler
        beforeAuthCheckCache.apply(handler, ctx, Optional.empty());
        beforeAuthCheckCache.apply(handler, ctx, Optional.empty());
        beforeAuthCheckCache.apply(handler, ctx, Optional.empty());
        beforeAuthCheckCache.apply(handler, ctx, Optional.empty());
        beforeAuthCheckCache.apply(handler, ctx, Optional.empty());

        return handler;
    }
}
