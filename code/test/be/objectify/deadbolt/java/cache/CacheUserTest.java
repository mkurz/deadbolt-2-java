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
import be.objectify.deadbolt.java.models.Subject;
import play.mvc.Http;

/**
 * @author Matthias Kurz (m.kurz@irregular.at)
 */
public class CacheUserTest
{

    @Test
    public void testCacheUserWithSubjectPresent() throws Exception
    {
        final Http.Request request = new Http.RequestBuilder().build();
        final DeadboltHandler handler = getHandler("deadbolt.java.cache-user = true", request, Mockito.mock(Subject.class));

        // should be called only once per handler
        Mockito.verify(handler, Mockito.times(2)).getSubject(Mockito.any(Http.RequestHeader.class));
    }

    @Test
    public void testCacheUserWithSubjectNotPresent() throws Exception
    {
        final Http.Request request = new Http.RequestBuilder().build();
        final DeadboltHandler handler = getHandler("deadbolt.java.cache-user = true", request, null);

        // even though we cache, there is not user so we try each time
        Mockito.verify(handler, Mockito.times(9)).getSubject(request);
    }

    @Test
    public void testDontCacheUserWithSubjectPresent() throws Exception
    {
        final Http.Request request = Mockito.mock(Http.Request.class);
        final DeadboltHandler handler = getHandler("deadbolt.java.cache-user = false", request, Mockito.mock(Subject.class));

        // we don't cache, so even there is a user we run the method each time
        Mockito.verify(handler, Mockito.times(9)).getSubject(request);
    }

    @Test
    public void testDontCacheUserWithSubjectNotPresent() throws Exception
    {
        final Http.Request request = Mockito.mock(Http.Request.class);
        final DeadboltHandler handler = getHandler("deadbolt.java.cache-user = false", request, null);

        // we don't cache and even there is no user, of course run the method each time
        Mockito.verify(handler, Mockito.times(9)).getSubject(request);
    }

    private static DeadboltHandler getHandler(final String setting, final Http.Request request, final Subject subject) throws Exception
    {
        final Config config = ConfigFactory.parseString(setting);

        final SubjectCache subjectCache = new DefaultSubjectCache(config);

        final DeadboltHandler handler = Mockito.mock(DeadboltHandler.class);
        Mockito.when(handler.getSubject(Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.ofNullable(subject)));

        // Fake the first handler
        Mockito.when(handler.getId())
                .thenReturn(Long.valueOf(0));

        Http.RequestHeader rh = request;

        // Lets call apply a couple of times for the first handler
        rh = subjectCache.apply(handler, rh).toCompletableFuture().get()._2;
        rh = subjectCache.apply(handler, rh).toCompletableFuture().get()._2;
        rh = subjectCache.apply(handler, rh).toCompletableFuture().get()._2;
        rh = subjectCache.apply(handler, rh).toCompletableFuture().get()._2;

        // Let's fake a second handler
        Mockito.when(handler.getId())
            .thenReturn(Long.valueOf(1));

        // Lets call apply a couple of times for the second handler
        rh = subjectCache.apply(handler, rh).toCompletableFuture().get()._2;
        rh = subjectCache.apply(handler, rh).toCompletableFuture().get()._2;
        rh = subjectCache.apply(handler, rh).toCompletableFuture().get()._2;
        rh = subjectCache.apply(handler, rh).toCompletableFuture().get()._2;
        rh = subjectCache.apply(handler, rh).toCompletableFuture().get()._2;

        return handler;
    }
}
