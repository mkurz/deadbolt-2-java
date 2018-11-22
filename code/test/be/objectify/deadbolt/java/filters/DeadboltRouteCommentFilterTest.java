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
package be.objectify.deadbolt.java.filters;

import static org.awaitility.Awaitility.await;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import akka.stream.Materializer;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import be.objectify.deadbolt.java.cache.HandlerCache;
import be.objectify.deadbolt.java.models.Subject;
import be.objectify.deadbolt.java.testsupport.TestHandlerCache;
import be.objectify.deadbolt.java.testsupport.TestPermission;
import be.objectify.deadbolt.java.testsupport.TestSubject;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import play.api.routing.HandlerDef;
import play.libs.F;
import play.mvc.Filter;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import play.routing.Router;
import play.test.Helpers;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class DeadboltRouteCommentFilterTest extends AbstractDeadboltFilterTest
{
    @Test
    public void testSubjectPresent_subjectIsPresent_defaultHandler() throws ExecutionException, InterruptedException
    {
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(F.Tuple(Optional.of(Mockito.mock(Subject.class)), Mockito.mock(Http.RequestHeader.class))));

        final HandlerCache handlerCache = Mockito.mock(HandlerCache.class);
        final DeadboltHandler handler = Mockito.mock(DeadboltHandler.class);
        Mockito.when(handlerCache.get())
               .thenReturn(handler);
        Mockito.when(handler.getSubject(Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));
        Mockito.when(handler.beforeAuthCheck(Mockito.any(Http.RequestHeader.class), Mockito.any(Optional.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(handler.onAuthFailure(Mockito.any(Http.RequestHeader.class),
                Mockito.any(Optional.class)))
                .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));

        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletableFuture<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    request("deadbolt:subjectPresent"))
                                                               .toCompletableFuture();
        await().until(eventualResult::isDone);
        Assert.assertTrue(flag[0]);
        Mockito.verify(handler,
                Mockito.never())
                .onAuthFailure(Mockito.any(Http.RequestHeader.class),
                        Mockito.any(Optional.class));
    }

    @Test
    public void testSubjectPresent_subjectIsNotPresent_defaultHandler() throws ExecutionException, InterruptedException
    {
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(F.Tuple(Optional.empty(), Mockito.mock(Http.RequestHeader.class))));

        final HandlerCache handlerCache = Mockito.mock(HandlerCache.class);
        final DeadboltHandler handler = Mockito.mock(DeadboltHandler.class);
        Mockito.when(handlerCache.get())
               .thenReturn(handler);
        Mockito.when(handler.getSubject(Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(handler.beforeAuthCheck(Mockito.any(Http.RequestHeader.class), Mockito.any(Optional.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(handler.onAuthFailure(Mockito.any(Http.RequestHeader.class),
                                           Mockito.eq(Optional.empty())))
               .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));

        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletableFuture<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    request("deadbolt:subjectPresent"))
                                                             .toCompletableFuture();
        await().until(eventualResult::isDone);
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1))
               .onAuthFailure(Mockito.any(Http.RequestHeader.class),
                              Mockito.eq(Optional.empty()));
    }

    @Test
    public void testSubjectPresent_subjectIsNotPresent_withContent() throws ExecutionException, InterruptedException
    {
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(F.Tuple(Optional.empty(), Mockito.mock(Http.RequestHeader.class))));

        final HandlerCache handlerCache = Mockito.mock(HandlerCache.class);
        final DeadboltHandler handler = Mockito.mock(DeadboltHandler.class);
        Mockito.when(handlerCache.get())
               .thenReturn(handler);
        Mockito.when(handler.getSubject(Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(handler.beforeAuthCheck(Mockito.any(Http.RequestHeader.class), Mockito.any(Optional.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(handler.onAuthFailure(Mockito.any(Http.RequestHeader.class),
                                           Mockito.eq(Optional.of("bar"))))
               .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));

        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletableFuture<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    request("deadbolt:subjectPresent:content[bar]"))
                                                             .toCompletableFuture();
        await().until(eventualResult::isDone);
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1))
               .onAuthFailure(Mockito.any(Http.RequestHeader.class),
                              Mockito.eq(Optional.of("bar")));
    }

    @Test
    public void testSubjectPresent_subjectIsPresent_specificHandler() throws ExecutionException, InterruptedException
    {
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(F.Tuple(Optional.of(Mockito.mock(Subject.class)), Mockito.mock(Http.RequestHeader.class))));

        final DeadboltHandler defaultHandler = Mockito.mock(DeadboltHandler.class);
        final DeadboltHandler specificHandler = Mockito.mock(DeadboltHandler.class);
        final HandlerCache handlerCache = new TestHandlerCache(defaultHandler,
                                                               Collections.singletonMap("foo",
                                                                                        specificHandler));
        Mockito.when(specificHandler.getSubject(Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));
        Mockito.when(specificHandler.beforeAuthCheck(Mockito.any(Http.RequestHeader.class), Mockito.any(Optional.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(specificHandler.onAuthFailure(Mockito.any(Http.RequestHeader.class),
                Mockito.any(Optional.class)))
                .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));

        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletableFuture<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    request("deadbolt:subjectPresent:handler[foo]"))
                                                             .toCompletableFuture();
        await().until(eventualResult::isDone);
        Assert.assertTrue(flag[0]);
        Mockito.verify(specificHandler,
                Mockito.never())
                .onAuthFailure(Mockito.any(Http.RequestHeader.class),
                        Mockito.any(Optional.class));
        Mockito.verifyZeroInteractions(defaultHandler);
    }

    @Test
    public void testSubjectPresent_subjectIsNotPresent_specificHandler() throws ExecutionException, InterruptedException
    {
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(F.Tuple(Optional.empty(), Mockito.mock(Http.RequestHeader.class))));

        final DeadboltHandler defaultHandler = Mockito.mock(DeadboltHandler.class);
        final DeadboltHandler specificHandler = Mockito.mock(DeadboltHandler.class);
        final HandlerCache handlerCache = new TestHandlerCache(defaultHandler,
                                                               Collections.singletonMap("foo",
                                                                                        specificHandler));
        Mockito.when(specificHandler.getSubject(Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(specificHandler.beforeAuthCheck(Mockito.any(Http.RequestHeader.class), Mockito.any(Optional.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(specificHandler.onAuthFailure(Mockito.any(Http.RequestHeader.class),
                                                   Mockito.eq(Optional.empty())))
               .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));

        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletableFuture<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    request("deadbolt:subjectPresent:handler[foo]"))
                                                             .toCompletableFuture();
        await().until(eventualResult::isDone);
        Assert.assertFalse(flag[0]);
        Mockito.verify(specificHandler,
                       Mockito.times(1))
               .onAuthFailure(Mockito.any(Http.RequestHeader.class),
                              Mockito.eq(Optional.empty()));
        Mockito.verifyZeroInteractions(defaultHandler);
    }

    @Test
    public void testSubjectPresent_subjectIsNotPresent_specificHandler_withContent() throws ExecutionException, InterruptedException
    {
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(F.Tuple(Optional.empty(), Mockito.mock(Http.RequestHeader.class))));

        final DeadboltHandler defaultHandler = Mockito.mock(DeadboltHandler.class);
        final DeadboltHandler specificHandler = Mockito.mock(DeadboltHandler.class);
        final HandlerCache handlerCache = new TestHandlerCache(defaultHandler,
                                                               Collections.singletonMap("foo",
                                                                                        specificHandler));
        Mockito.when(specificHandler.getSubject(Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(specificHandler.beforeAuthCheck(Mockito.any(Http.RequestHeader.class), Mockito.any(Optional.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(specificHandler.onAuthFailure(Mockito.any(Http.RequestHeader.class),
                                                   Mockito.eq(Optional.of("bar"))))
               .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));

        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletableFuture<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    request("deadbolt:subjectPresent:content[bar]:handler[foo]"))
                                                             .toCompletableFuture();
        await().until(eventualResult::isDone);
        Assert.assertFalse(flag[0]);
        Mockito.verify(specificHandler,
                       Mockito.times(1))
               .onAuthFailure(Mockito.any(Http.RequestHeader.class),
                              Mockito.eq(Optional.of("bar")));
        Mockito.verifyZeroInteractions(defaultHandler);
    }

    @Test
    public void testSubjectNotPresent_subjectIsPresent_defaultHandler() throws ExecutionException, InterruptedException
    {

        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(F.Tuple(Optional.of(Mockito.mock(Subject.class)), Mockito.mock(Http.RequestHeader.class))));

        final HandlerCache handlerCache = Mockito.mock(HandlerCache.class);
        final DeadboltHandler handler = Mockito.mock(DeadboltHandler.class);
        Mockito.when(handlerCache.get())
               .thenReturn(handler);
        Mockito.when(handler.getSubject(Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));
        Mockito.when(handler.beforeAuthCheck(Mockito.any(Http.RequestHeader.class), Mockito.any(Optional.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(handler.onAuthFailure(Mockito.any(Http.RequestHeader.class),
                                           Mockito.eq(Optional.empty())))
               .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));

        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletableFuture<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    request("deadbolt:subjectNotPresent"))
                                                             .toCompletableFuture();
        await().until(eventualResult::isDone);
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1))
               .onAuthFailure(Mockito.any(Http.RequestHeader.class),
                              Mockito.eq(Optional.empty()));
    }

    @Test
    public void testSubjectNotPresent_subjectIsPresent_withContent() throws ExecutionException, InterruptedException
    {

        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(F.Tuple(Optional.of(Mockito.mock(Subject.class)), Mockito.mock(Http.RequestHeader.class))));

        final HandlerCache handlerCache = Mockito.mock(HandlerCache.class);
        final DeadboltHandler handler = Mockito.mock(DeadboltHandler.class);
        Mockito.when(handlerCache.get())
               .thenReturn(handler);
        Mockito.when(handler.getSubject(Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));
        Mockito.when(handler.beforeAuthCheck(Mockito.any(Http.RequestHeader.class), Mockito.any(Optional.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(handler.onAuthFailure(Mockito.any(Http.RequestHeader.class),
                                           Mockito.eq(Optional.of("bar"))))
               .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));

        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletableFuture<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    request("deadbolt:subjectNotPresent:content[bar]"))
                                                             .toCompletableFuture();
        await().until(eventualResult::isDone);
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1))
               .onAuthFailure(Mockito.any(Http.RequestHeader.class),
                              Mockito.eq(Optional.of("bar")));
    }

    @Test
    public void testSubjectNotPresent_subjectIsNotPresent_defaultHandler() throws ExecutionException, InterruptedException
    {

        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(F.Tuple(Optional.empty(), Mockito.mock(Http.RequestHeader.class))));

        final HandlerCache handlerCache = Mockito.mock(HandlerCache.class);
        final DeadboltHandler handler = Mockito.mock(DeadboltHandler.class);
        Mockito.when(handlerCache.get())
               .thenReturn(handler);
        Mockito.when(handler.getSubject(Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(handler.beforeAuthCheck(Mockito.any(Http.RequestHeader.class), Mockito.any(Optional.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(handler.onAuthFailure(Mockito.any(Http.RequestHeader.class),
                Mockito.any(Optional.class)))
                .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));

        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletableFuture<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    request("deadbolt:subjectNotPresent"))
                                                             .toCompletableFuture();
        await().until(eventualResult::isDone);
        Assert.assertTrue(flag[0]);
        Mockito.verify(handler,
                Mockito.never())
                .onAuthFailure(Mockito.any(Http.RequestHeader.class),
                        Mockito.any(Optional.class));
    }

    @Test
    public void testSubjectNotPresent_subjectIsPresent_specificHandler() throws ExecutionException, InterruptedException
    {
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(F.Tuple(Optional.of(Mockito.mock(Subject.class)), Mockito.mock(Http.RequestHeader.class))));

        final DeadboltHandler defaultHandler = Mockito.mock(DeadboltHandler.class);
        final DeadboltHandler specificHandler = Mockito.mock(DeadboltHandler.class);
        final HandlerCache handlerCache = new TestHandlerCache(defaultHandler,
                                                               Collections.singletonMap("foo",
                                                                                        specificHandler));
        Mockito.when(specificHandler.getSubject(Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));
        Mockito.when(specificHandler.beforeAuthCheck(Mockito.any(Http.RequestHeader.class), Mockito.any(Optional.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(specificHandler.onAuthFailure(Mockito.any(Http.RequestHeader.class),
                                                   Mockito.eq(Optional.empty())))
               .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));

        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletableFuture<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    request("deadbolt:subjectNotPresent:handler[foo]"))
                                                             .toCompletableFuture();
        await().until(eventualResult::isDone);
        Assert.assertFalse(flag[0]);
        Mockito.verify(specificHandler,
                       Mockito.times(1))
               .onAuthFailure(Mockito.any(Http.RequestHeader.class),
                              Mockito.eq(Optional.empty()));
        Mockito.verifyZeroInteractions(defaultHandler);
    }

    @Test
    public void testSubjectNotPresent_subjectIsPresent_specificHandler_withContent() throws ExecutionException, InterruptedException
    {
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(F.Tuple(Optional.of(Mockito.mock(Subject.class)), Mockito.mock(Http.RequestHeader.class))));

        final DeadboltHandler defaultHandler = Mockito.mock(DeadboltHandler.class);
        final DeadboltHandler specificHandler = Mockito.mock(DeadboltHandler.class);
        final HandlerCache handlerCache = new TestHandlerCache(defaultHandler,
                                                               Collections.singletonMap("foo",
                                                                                        specificHandler));
        Mockito.when(specificHandler.getSubject(Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));
        Mockito.when(specificHandler.beforeAuthCheck(Mockito.any(Http.RequestHeader.class), Mockito.any(Optional.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(specificHandler.onAuthFailure(Mockito.any(Http.RequestHeader.class),
                                                   Mockito.eq(Optional.of("bar"))))
               .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));

        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletableFuture<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    request("deadbolt:subjectNotPresent:content[bar]:handler[foo]"))
                                                             .toCompletableFuture();
        await().until(eventualResult::isDone);
        Assert.assertFalse(flag[0]);
        Mockito.verify(specificHandler,
                       Mockito.times(1))
               .onAuthFailure(Mockito.any(Http.RequestHeader.class),
                              Mockito.eq(Optional.of("bar")));
        Mockito.verifyZeroInteractions(defaultHandler);
    }

    @Test
    public void testSubjectNotPresent_subjectIsNotPresent_specificHandler() throws ExecutionException, InterruptedException
    {
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(F.Tuple(Optional.empty(), Mockito.mock(Http.RequestHeader.class))));

        final DeadboltHandler defaultHandler = Mockito.mock(DeadboltHandler.class);
        final DeadboltHandler specificHandler = Mockito.mock(DeadboltHandler.class);
        final HandlerCache handlerCache = new TestHandlerCache(defaultHandler,
                                                               Collections.singletonMap("foo",
                                                                                        specificHandler));
        Mockito.when(specificHandler.getSubject(Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(specificHandler.beforeAuthCheck(Mockito.any(Http.RequestHeader.class), Mockito.any(Optional.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(specificHandler.onAuthFailure(Mockito.any(Http.RequestHeader.class),
                                                   Mockito.any(Optional.class)))
               .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));

        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletableFuture<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    request("deadbolt:subjectNotPresent:handler[foo]"))
                                                             .toCompletableFuture();
        await().until(eventualResult::isDone);
        Assert.assertTrue(flag[0]);
        Mockito.verify(specificHandler,
                Mockito.never())
                .onAuthFailure(Mockito.any(Http.RequestHeader.class),
                        Mockito.any(Optional.class));
        Mockito.verifyZeroInteractions(defaultHandler);
    }

    @Test
    public void testDynamic_pass_defaultHandler() throws ExecutionException, InterruptedException
    {
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(F.Tuple(Optional.of(Mockito.mock(Subject.class)), Mockito.mock(Http.RequestHeader.class))));

        final HandlerCache handlerCache = Mockito.mock(HandlerCache.class);
        final DeadboltHandler handler = Mockito.mock(DeadboltHandler.class);
        final DynamicResourceHandler drh = Mockito.mock(DynamicResourceHandler.class);
        Mockito.when(handlerCache.get())
               .thenReturn(handler);
        Mockito.when(handler.beforeAuthCheck(Mockito.any(Http.RequestHeader.class), Mockito.any(Optional.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(handler.getDynamicResourceHandler(Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(drh)));
        Mockito.when(drh.isAllowed(Mockito.eq("foo"),
                                   Mockito.eq(Optional.empty()),
                                   Mockito.eq(handler),
                                   Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(Boolean.TRUE));
        Mockito.when(handler.onAuthFailure(Mockito.any(Http.RequestHeader.class),
                Mockito.any(Optional.class)))
                .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));

        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletableFuture<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    request("deadbolt:dynamic:name[foo]"))
                                                             .toCompletableFuture();
        await().until(eventualResult::isDone);
        Assert.assertTrue(flag[0]);
        Mockito.verify(handler,
                Mockito.never())
                .onAuthFailure(Mockito.any(Http.RequestHeader.class),
                        Mockito.any(Optional.class));
    }

    @Test
    public void testDynamic_fail_defaultHandler() throws ExecutionException, InterruptedException
    {
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(F.Tuple(Optional.of(Mockito.mock(Subject.class)), Mockito.mock(Http.RequestHeader.class))));

        final HandlerCache handlerCache = Mockito.mock(HandlerCache.class);
        final DeadboltHandler handler = Mockito.mock(DeadboltHandler.class);
        final DynamicResourceHandler drh = Mockito.mock(DynamicResourceHandler.class);
        Mockito.when(handlerCache.get())
               .thenReturn(handler);
        Mockito.when(handler.beforeAuthCheck(Mockito.any(Http.RequestHeader.class), Mockito.any(Optional.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(handler.getDynamicResourceHandler(Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(drh)));
        Mockito.when(drh.isAllowed(Mockito.eq("foo"),
                                   Mockito.eq(Optional.empty()),
                                   Mockito.eq(handler),
                                   Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(Boolean.FALSE));
        Mockito.when(handler.onAuthFailure(Mockito.any(Http.RequestHeader.class),
                                           Mockito.eq(Optional.empty())))
               .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));

        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletableFuture<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    request("deadbolt:dynamic:name[foo]"))
                                                             .toCompletableFuture();
        await().until(eventualResult::isDone);
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1))
               .onAuthFailure(Mockito.any(Http.RequestHeader.class),
                              Mockito.eq(Optional.empty()));
    }

    @Test
    public void testDynamic_fail_withContent() throws ExecutionException, InterruptedException
    {
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(F.Tuple(Optional.of(Mockito.mock(Subject.class)), Mockito.mock(Http.RequestHeader.class))));

        final HandlerCache handlerCache = Mockito.mock(HandlerCache.class);
        final DeadboltHandler handler = Mockito.mock(DeadboltHandler.class);
        final DynamicResourceHandler drh = Mockito.mock(DynamicResourceHandler.class);
        Mockito.when(handlerCache.get())
               .thenReturn(handler);
        Mockito.when(handler.beforeAuthCheck(Mockito.any(Http.RequestHeader.class), Mockito.any(Optional.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(handler.getDynamicResourceHandler(Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(drh)));
        Mockito.when(drh.isAllowed(Mockito.eq("foo"),
                                   Mockito.eq(Optional.empty()),
                                   Mockito.eq(handler),
                                   Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(Boolean.FALSE));
        Mockito.when(handler.onAuthFailure(Mockito.any(Http.RequestHeader.class),
                                           Mockito.eq(Optional.of("bar"))))
               .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));

        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletableFuture<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    request("deadbolt:dynamic:name[foo]:content[bar]"))
                                                             .toCompletableFuture();
        await().until(eventualResult::isDone);
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1))
               .onAuthFailure(Mockito.any(Http.RequestHeader.class),
                              Mockito.eq(Optional.of("bar")));
    }

    @Test
    public void testDynamic_pass_specificHandler() throws ExecutionException, InterruptedException
    {
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(F.Tuple(Optional.of(Mockito.mock(Subject.class)), Mockito.mock(Http.RequestHeader.class))));

        final DeadboltHandler defaultHandler = Mockito.mock(DeadboltHandler.class);
        final DeadboltHandler specificHandler = Mockito.mock(DeadboltHandler.class);
        final DynamicResourceHandler drh = Mockito.mock(DynamicResourceHandler.class);
        final HandlerCache handlerCache = new TestHandlerCache(defaultHandler,
                                                               Collections.singletonMap("gurdy",
                                                                                        specificHandler));
        Mockito.when(specificHandler.beforeAuthCheck(Mockito.any(Http.RequestHeader.class), Mockito.any(Optional.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(specificHandler.getDynamicResourceHandler(Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(drh)));
        Mockito.when(drh.isAllowed(Mockito.eq("foo"),
                                   Mockito.eq(Optional.empty()),
                                   Mockito.eq(specificHandler),
                                   Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(Boolean.TRUE));
        Mockito.when(specificHandler.onAuthFailure(Mockito.any(Http.RequestHeader.class),
                Mockito.any(Optional.class)))
                .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));

        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletableFuture<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    request("deadbolt:dynamic:name[foo]:handler[gurdy]"))
                                                             .toCompletableFuture();
        await().until(eventualResult::isDone);
        Assert.assertTrue(flag[0]);
        Mockito.verify(specificHandler,
                Mockito.never())
                .onAuthFailure(Mockito.any(Http.RequestHeader.class),
                        Mockito.any(Optional.class));
        Mockito.verifyZeroInteractions(defaultHandler);
    }

    @Test
    public void testDynamic_fail_specificHandler() throws ExecutionException, InterruptedException
    {
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(F.Tuple(Optional.of(Mockito.mock(Subject.class)), Mockito.mock(Http.RequestHeader.class))));

        final DeadboltHandler defaultHandler = Mockito.mock(DeadboltHandler.class);
        final DeadboltHandler specificHandler = Mockito.mock(DeadboltHandler.class);
        final DynamicResourceHandler drh = Mockito.mock(DynamicResourceHandler.class);
        final HandlerCache handlerCache = new TestHandlerCache(defaultHandler,
                                                               Collections.singletonMap("gurdy",
                                                                                        specificHandler));
        Mockito.when(specificHandler.beforeAuthCheck(Mockito.any(Http.RequestHeader.class), Mockito.any(Optional.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(specificHandler.getDynamicResourceHandler(Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(drh)));
        Mockito.when(drh.isAllowed(Mockito.eq("foo"),
                                   Mockito.eq(Optional.empty()),
                                   Mockito.eq(specificHandler),
                                   Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(Boolean.FALSE));
        Mockito.when(specificHandler.onAuthFailure(Mockito.any(Http.RequestHeader.class),
                                                   Mockito.eq(Optional.empty())))
               .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));

        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletableFuture<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    request("deadbolt:dynamic:name[foo]:handler[gurdy]"))
                                                             .toCompletableFuture();
        await().until(eventualResult::isDone);
        Assert.assertFalse(flag[0]);
        Mockito.verify(specificHandler,
                       Mockito.times(1))
               .onAuthFailure(Mockito.any(Http.RequestHeader.class),
                              Mockito.eq(Optional.empty()));
        Mockito.verifyZeroInteractions(defaultHandler);
    }

    @Test
    public void testDynamic_fail_specificHandler_withContent() throws ExecutionException, InterruptedException
    {
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(F.Tuple(Optional.of(Mockito.mock(Subject.class)), Mockito.mock(Http.RequestHeader.class))));

        final DeadboltHandler defaultHandler = Mockito.mock(DeadboltHandler.class);
        final DeadboltHandler specificHandler = Mockito.mock(DeadboltHandler.class);
        final DynamicResourceHandler drh = Mockito.mock(DynamicResourceHandler.class);
        final HandlerCache handlerCache = new TestHandlerCache(defaultHandler,
                                                               Collections.singletonMap("gurdy",
                                                                                        specificHandler));
        Mockito.when(specificHandler.beforeAuthCheck(Mockito.any(Http.RequestHeader.class), Mockito.any(Optional.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(specificHandler.getDynamicResourceHandler(Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(drh)));
        Mockito.when(drh.isAllowed(Mockito.eq("foo"),
                                   Mockito.eq(Optional.empty()),
                                   Mockito.eq(specificHandler),
                                   Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(Boolean.FALSE));
        Mockito.when(specificHandler.onAuthFailure(Mockito.any(Http.RequestHeader.class),
                                                   Mockito.eq(Optional.of("bar"))))
               .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));

        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletableFuture<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    request("deadbolt:dynamic:name[foo]:content[bar]:handler[gurdy]"))
                                                             .toCompletableFuture();
        await().until(eventualResult::isDone);
        Assert.assertFalse(flag[0]);
        Mockito.verify(specificHandler,
                       Mockito.times(1))
               .onAuthFailure(Mockito.any(Http.RequestHeader.class),
                              Mockito.eq(Optional.of("bar")));
        Mockito.verifyZeroInteractions(defaultHandler);
    }

    @Test
    public void testUnknownDeadboltComment() throws ExecutionException, InterruptedException
    {
        final HandlerCache handlerCache = Mockito.mock(HandlerCache.class);
        final DeadboltHandler handler = Mockito.mock(DeadboltHandler.class);
        Mockito.when(handlerCache.get())
               .thenReturn(handler);
        Mockito.when(handler.onAuthFailure(Mockito.any(Http.RequestHeader.class),
                                           Mockito.eq(Optional.empty())))
               .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));

        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletableFuture<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    request("deadbolt:sbujectPresent"))
                                                             .toCompletableFuture();
        await().until(eventualResult::isDone);
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1))
               .onAuthFailure(Mockito.any(Http.RequestHeader.class),
                              Mockito.eq(Optional.empty()));
    }

    @Test
    public void testRoleBasedPermissions_subjectHasPermissions_defaultHandler() throws ExecutionException, InterruptedException
    {
        final Subject subject = new TestSubject.Builder().permission(new TestPermission("bar"))
                                                         .build();
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(F.Tuple(Optional.of(subject), Mockito.mock(Http.RequestHeader.class))));

        final HandlerCache handlerCache = Mockito.mock(HandlerCache.class);
        final DeadboltHandler handler = Mockito.mock(DeadboltHandler.class);
        Mockito.when(handlerCache.get())
               .thenReturn(handler);
        Mockito.when(handler.getSubject(Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(subject)));
        Mockito.when(handler.getPermissionsForRole("foo"))
               .then(invocation -> CompletableFuture.completedFuture(Collections.singletonList(new TestPermission("bar"))));
        Mockito.when(handler.beforeAuthCheck(Mockito.any(Http.RequestHeader.class), Mockito.any(Optional.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(handler.onAuthFailure(Mockito.any(Http.RequestHeader.class),
                Mockito.any(Optional.class)))
                .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));

        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletableFuture<Result> eventualResult = filter.apply(rh -> {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    request("deadbolt:rbp:name[foo]"))
                                                             .toCompletableFuture();
        await().until(eventualResult::isDone);
        Mockito.verify(handler,
                Mockito.never())
                .onAuthFailure(Mockito.any(Http.RequestHeader.class),
                        Mockito.any(Optional.class));
        Assert.assertTrue(flag[0]);
    }

    @Test
    public void testRoleBasedPermissions_subjectDoesNotHavePermission_defaultHandler() throws ExecutionException, InterruptedException
    {
        final Subject subject = new TestSubject.Builder().permission(new TestPermission("hurdy"))
                                                         .build();
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(F.Tuple(Optional.of(subject), Mockito.mock(Http.RequestHeader.class))));

        final HandlerCache handlerCache = Mockito.mock(HandlerCache.class);
        final DeadboltHandler handler = Mockito.mock(DeadboltHandler.class);
        Mockito.when(handlerCache.get())
               .thenReturn(handler);
        Mockito.when(handler.getSubject(Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(handler.beforeAuthCheck(Mockito.any(Http.RequestHeader.class), Mockito.any(Optional.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(handler.getPermissionsForRole("foo"))
               .then(invocation -> CompletableFuture.completedFuture(Collections.singletonList(new TestPermission("bar"))));
        Mockito.when(handler.onAuthFailure(Mockito.any(Http.RequestHeader.class),
                                           Mockito.eq(Optional.empty())))
               .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));

        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletableFuture<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    request("deadbolt:rbp:name[foo]"))
                                                             .toCompletableFuture();
        await().until(eventualResult::isDone);
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1))
               .onAuthFailure(Mockito.any(Http.RequestHeader.class),
                              Mockito.eq(Optional.empty()));
    }

    @Test
    public void testRoleBasedPermissions_subjectDoesNotHavePermission_withContent() throws ExecutionException, InterruptedException
    {
        final Subject subject = new TestSubject.Builder().permission(new TestPermission("hurdy"))
                                                         .build();
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(F.Tuple(Optional.of(subject), Mockito.mock(Http.RequestHeader.class))));

        final HandlerCache handlerCache = Mockito.mock(HandlerCache.class);
        final DeadboltHandler handler = Mockito.mock(DeadboltHandler.class);
        Mockito.when(handlerCache.get())
               .thenReturn(handler);
        Mockito.when(handler.getSubject(Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(handler.beforeAuthCheck(Mockito.any(Http.RequestHeader.class), Mockito.any(Optional.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(handler.getPermissionsForRole("foo"))
               .then(invocation -> CompletableFuture.completedFuture(Collections.singletonList(new TestPermission("bar"))));
        Mockito.when(handler.onAuthFailure(Mockito.any(Http.RequestHeader.class),
                                           Mockito.eq(Optional.of("bar"))))
               .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));

        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletableFuture<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    request("deadbolt:rbp:name[foo]:content[bar]"))
                                                             .toCompletableFuture();
        await().until(eventualResult::isDone);
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1))
               .onAuthFailure(Mockito.any(Http.RequestHeader.class),
                              Mockito.eq(Optional.of("bar")));
    }

    @Test
    public void testRoleBasedPermissions_subjectHasPermissions_specificHandler() throws ExecutionException, InterruptedException
    {
        final Subject subject = new TestSubject.Builder().permission(new TestPermission("bar"))
                                                         .build();
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(F.Tuple(Optional.of(subject), Mockito.mock(Http.RequestHeader.class))));

        final DeadboltHandler defaultHandler = Mockito.mock(DeadboltHandler.class);
        final DeadboltHandler specificHandler = Mockito.mock(DeadboltHandler.class);
        final HandlerCache handlerCache = new TestHandlerCache(defaultHandler,
                                                               Collections.singletonMap("foo",
                                                                                        specificHandler));
        Mockito.when(specificHandler.getSubject(Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));
        Mockito.when(specificHandler.beforeAuthCheck(Mockito.any(Http.RequestHeader.class), Mockito.any(Optional.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(specificHandler.getPermissionsForRole("foo"))
               .then(invocation -> CompletableFuture.completedFuture(Collections.singletonList(new TestPermission("bar"))));
        Mockito.when(specificHandler.onAuthFailure(Mockito.any(Http.RequestHeader.class),
                Mockito.any(Optional.class)))
                .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));

        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletableFuture<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    request("deadbolt:rbp:name[foo]:handler[foo]"))
                                                             .toCompletableFuture();
        await().until(eventualResult::isDone);
        Assert.assertTrue(flag[0]);
        Mockito.verify(specificHandler,
                Mockito.never())
                .onAuthFailure(Mockito.any(Http.RequestHeader.class),
                        Mockito.any(Optional.class));
        Mockito.verifyZeroInteractions(defaultHandler);
    }

    @Test
    public void testRoleBasedPermissions_subjectDoesNotHavePermission_specificHandler() throws ExecutionException, InterruptedException
    {
        final Subject subject = new TestSubject.Builder().permission(new TestPermission("hurdy"))
                                                         .build();
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(F.Tuple(Optional.of(subject), Mockito.mock(Http.RequestHeader.class))));

        final DeadboltHandler defaultHandler = Mockito.mock(DeadboltHandler.class);
        final DeadboltHandler specificHandler = Mockito.mock(DeadboltHandler.class);
        final HandlerCache handlerCache = new TestHandlerCache(defaultHandler,
                                                               Collections.singletonMap("foo",
                                                                                        specificHandler));
        Mockito.when(specificHandler.getSubject(Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(specificHandler.beforeAuthCheck(Mockito.any(Http.RequestHeader.class), Mockito.any(Optional.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(specificHandler.onAuthFailure(Mockito.any(Http.RequestHeader.class),
                                                   Mockito.eq(Optional.empty())))
               .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));
        Mockito.when(specificHandler.getPermissionsForRole("foo"))
               .then(invocation -> CompletableFuture.completedFuture(Collections.singletonList(new TestPermission("bar"))));
        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletableFuture<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    request("deadbolt:rbp:name[foo]:handler[foo]"))
                                                             .toCompletableFuture();
        await().until(eventualResult::isDone);
        Assert.assertFalse(flag[0]);
        Mockito.verify(specificHandler,
                       Mockito.times(1))
               .onAuthFailure(Mockito.any(Http.RequestHeader.class),
                              Mockito.eq(Optional.empty()));
        Mockito.verifyZeroInteractions(defaultHandler);
    }

    @Test
    public void testRoleBasedPermissions_subjectDoesNotHavePermission_specificHandler_withContent() throws ExecutionException, InterruptedException
    {
        final Subject subject = new TestSubject.Builder().permission(new TestPermission("hurdy"))
                                                         .build();
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(F.Tuple(Optional.of(subject), Mockito.mock(Http.RequestHeader.class))));

        final DeadboltHandler defaultHandler = Mockito.mock(DeadboltHandler.class);
        final DeadboltHandler specificHandler = Mockito.mock(DeadboltHandler.class);
        final HandlerCache handlerCache = new TestHandlerCache(defaultHandler,
                                                               Collections.singletonMap("foo",
                                                                                        specificHandler));
        Mockito.when(specificHandler.getSubject(Mockito.any(Http.RequestHeader.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(specificHandler.beforeAuthCheck(Mockito.any(Http.RequestHeader.class), Mockito.any(Optional.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(specificHandler.onAuthFailure(Mockito.any(Http.RequestHeader.class),
                                                   Mockito.eq(Optional.of("bar"))))
               .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));
        Mockito.when(specificHandler.getPermissionsForRole("foo"))
               .then(invocation -> CompletableFuture.completedFuture(Collections.singletonList(new TestPermission("bar"))));
        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletableFuture<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    request("deadbolt:rbp:name[foo]:content[bar]:handler[foo]"))
                                                             .toCompletableFuture();
        await().until(eventualResult::isDone);
        Assert.assertFalse(flag[0]);
        Mockito.verify(specificHandler,
                       Mockito.times(1))
               .onAuthFailure(Mockito.any(Http.RequestHeader.class),
                              Mockito.eq(Optional.of("bar")));
        Mockito.verifyZeroInteractions(defaultHandler);
    }

    private Http.RequestImpl request(final String comment) {
        return Helpers.fakeRequest("GET", "http://localhost/foo")
                      .attr(Router.Attrs.HANDLER_DEF,
                            HandlerDef.apply(getClass().getClassLoader(),
                                             "",
                                             "",
                                             "",
                                             null,
                                             "",
                                             "",
                                             comment,
                                             null))
                      .build();
    }
}