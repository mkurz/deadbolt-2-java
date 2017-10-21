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

import akka.stream.Materializer;
import be.objectify.deadbolt.java.ConstraintLogic;
import be.objectify.deadbolt.java.DeadboltAnalyzer;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import be.objectify.deadbolt.java.cache.CompositeCache;
import be.objectify.deadbolt.java.cache.DefaultPatternCache;
import be.objectify.deadbolt.java.cache.HandlerCache;
import be.objectify.deadbolt.java.cache.SubjectCache;
import be.objectify.deadbolt.java.models.Permission;
import be.objectify.deadbolt.java.models.Subject;
import be.objectify.deadbolt.java.testsupport.FakeCache;
import be.objectify.deadbolt.java.testsupport.TestCookies;
import be.objectify.deadbolt.java.testsupport.TestHandlerCache;
import be.objectify.deadbolt.java.testsupport.TestPermission;
import be.objectify.deadbolt.java.testsupport.TestSubject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import play.mvc.Filter;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import play.routing.Router;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class DeadboltRouteCommentFilterTest
{

    private final DeadboltAnalyzer analyzer = new DeadboltAnalyzer();
    private FilterConstraints filterConstraints;
    private Http.RequestHeader requestHeader;
    private SubjectCache subjectCache;
    private Map<String, String> tags = new HashMap<>();

    @Before
    public void setUp()
    {
        subjectCache = Mockito.mock(SubjectCache.class);

        final ConstraintLogic constraintLogic = new ConstraintLogic(analyzer,
                                                                    subjectCache,
                                                                    new DefaultPatternCache(new FakeCache()));
        filterConstraints = new FilterConstraints(constraintLogic,
                                                  Mockito.mock(CompositeCache.class));

        requestHeader = Mockito.mock(Http.RequestHeader.class);
        Mockito.when(requestHeader.tags()).thenReturn(tags);
        Mockito.when(requestHeader.method()).thenReturn("GET");
        Mockito.when(requestHeader.uri()).thenReturn("http://localhost/foo");
        Mockito.when(requestHeader.clientCertificateChain()).thenReturn(Optional.empty());
        Mockito.when(requestHeader.cookies()).thenReturn(new TestCookies());
    }

    @After
    public void tearDown()
    {
        filterConstraints = null;
        requestHeader = null;
        subjectCache = null;
        tags.clear();
    }

    @Test
    public void testSubjectPresent_subjectIsPresent_defaultHandler() throws ExecutionException, InterruptedException
    {

        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));

        final HandlerCache handlerCache = Mockito.mock(HandlerCache.class);
        final DeadboltHandler handler = Mockito.mock(DeadboltHandler.class);
        Mockito.when(handlerCache.get())
               .thenReturn(handler);
        Mockito.when(handler.getSubject(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));
        Mockito.when(handler.beforeAuthCheck(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        comment("deadbolt:subjectPresent");
        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletionStage<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    requestHeader);
        ((CompletableFuture) eventualResult).get();
        Assert.assertTrue(flag[0]);
    }

    @Test
    public void testSubjectPresent_subjectIsNotPresent_defaultHandler() throws ExecutionException, InterruptedException
    {

        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        final HandlerCache handlerCache = Mockito.mock(HandlerCache.class);
        final DeadboltHandler handler = Mockito.mock(DeadboltHandler.class);
        Mockito.when(handlerCache.get())
               .thenReturn(handler);
        Mockito.when(handler.getSubject(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(handler.beforeAuthCheck(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(handler.onAuthFailure(Mockito.any(Http.Context.class),
                                           Mockito.eq(Optional.empty())))
               .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));

        comment("deadbolt:subjectPresent");
        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletionStage<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    requestHeader);
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1))
               .onAuthFailure(Mockito.any(Http.Context.class),
                              Mockito.eq(Optional.empty()));
    }

    @Test
    public void testSubjectPresent_subjectIsNotPresent_withContent() throws ExecutionException, InterruptedException
    {

        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        final HandlerCache handlerCache = Mockito.mock(HandlerCache.class);
        final DeadboltHandler handler = Mockito.mock(DeadboltHandler.class);
        Mockito.when(handlerCache.get())
               .thenReturn(handler);
        Mockito.when(handler.getSubject(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(handler.beforeAuthCheck(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(handler.onAuthFailure(Mockito.any(Http.Context.class),
                                           Mockito.eq(Optional.of("bar"))))
               .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));

        comment("deadbolt:subjectPresent:content[bar]");
        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletionStage<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    requestHeader);
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1))
               .onAuthFailure(Mockito.any(Http.Context.class),
                              Mockito.eq(Optional.of("bar")));
    }

    @Test
    public void testSubjectPresent_subjectIsPresent_specificHandler() throws ExecutionException, InterruptedException
    {
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));

        final DeadboltHandler defaultHandler = Mockito.mock(DeadboltHandler.class);
        final DeadboltHandler specificHandler = Mockito.mock(DeadboltHandler.class);
        final HandlerCache handlerCache = new TestHandlerCache(defaultHandler,
                                                               Collections.singletonMap("foo",
                                                                                        specificHandler));
        Mockito.when(specificHandler.getSubject(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));
        Mockito.when(specificHandler.beforeAuthCheck(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        comment("deadbolt:subjectPresent:handler[foo]");
        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletionStage<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    requestHeader);
        ((CompletableFuture) eventualResult).get();
        Assert.assertTrue(flag[0]);
        Mockito.verifyZeroInteractions(defaultHandler);
    }

    @Test
    public void testSubjectPresent_subjectIsNotPresent_specificHandler() throws ExecutionException, InterruptedException
    {
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        final DeadboltHandler defaultHandler = Mockito.mock(DeadboltHandler.class);
        final DeadboltHandler specificHandler = Mockito.mock(DeadboltHandler.class);
        final HandlerCache handlerCache = new TestHandlerCache(defaultHandler,
                                                               Collections.singletonMap("foo",
                                                                                        specificHandler));
        Mockito.when(specificHandler.getSubject(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(specificHandler.beforeAuthCheck(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(specificHandler.onAuthFailure(Mockito.any(Http.Context.class),
                                                   Mockito.eq(Optional.empty())))
               .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));

        comment("deadbolt:subjectPresent:handler[foo]");
        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletionStage<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    requestHeader);
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(specificHandler,
                       Mockito.times(1))
               .onAuthFailure(Mockito.any(Http.Context.class),
                              Mockito.eq(Optional.empty()));
        Mockito.verifyZeroInteractions(defaultHandler);
    }

    @Test
    public void testSubjectPresent_subjectIsNotPresent_specificHandler_withContent() throws ExecutionException, InterruptedException
    {
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        final DeadboltHandler defaultHandler = Mockito.mock(DeadboltHandler.class);
        final DeadboltHandler specificHandler = Mockito.mock(DeadboltHandler.class);
        final HandlerCache handlerCache = new TestHandlerCache(defaultHandler,
                                                               Collections.singletonMap("foo",
                                                                                        specificHandler));
        Mockito.when(specificHandler.getSubject(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(specificHandler.beforeAuthCheck(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(specificHandler.onAuthFailure(Mockito.any(Http.Context.class),
                                                   Mockito.eq(Optional.of("bar"))))
               .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));

        comment("deadbolt:subjectPresent:content[bar]:handler[foo]");
        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletionStage<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    requestHeader);
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(specificHandler,
                       Mockito.times(1))
               .onAuthFailure(Mockito.any(Http.Context.class),
                              Mockito.eq(Optional.of("bar")));
        Mockito.verifyZeroInteractions(defaultHandler);
    }

    @Test
    public void testSubjectNotPresent_subjectIsPresent_defaultHandler() throws ExecutionException, InterruptedException
    {

        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));

        final HandlerCache handlerCache = Mockito.mock(HandlerCache.class);
        final DeadboltHandler handler = Mockito.mock(DeadboltHandler.class);
        Mockito.when(handlerCache.get())
               .thenReturn(handler);
        Mockito.when(handler.getSubject(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));
        Mockito.when(handler.beforeAuthCheck(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(handler.onAuthFailure(Mockito.any(Http.Context.class),
                                           Mockito.eq(Optional.empty())))
               .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));

        comment("deadbolt:subjectNotPresent");
        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletionStage<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    requestHeader);
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1))
               .onAuthFailure(Mockito.any(Http.Context.class),
                              Mockito.eq(Optional.empty()));
    }

    @Test
    public void testSubjectNotPresent_subjectIsPresent_withContent() throws ExecutionException, InterruptedException
    {

        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));

        final HandlerCache handlerCache = Mockito.mock(HandlerCache.class);
        final DeadboltHandler handler = Mockito.mock(DeadboltHandler.class);
        Mockito.when(handlerCache.get())
               .thenReturn(handler);
        Mockito.when(handler.getSubject(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));
        Mockito.when(handler.beforeAuthCheck(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(handler.onAuthFailure(Mockito.any(Http.Context.class),
                                           Mockito.eq(Optional.of("bar"))))
               .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));

        comment("deadbolt:subjectNotPresent:content[bar]");
        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletionStage<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    requestHeader);
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1))
               .onAuthFailure(Mockito.any(Http.Context.class),
                              Mockito.eq(Optional.of("bar")));
    }

    @Test
    public void testSubjectNotPresent_subjectIsNotPresent_defaultHandler() throws ExecutionException, InterruptedException
    {

        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        final HandlerCache handlerCache = Mockito.mock(HandlerCache.class);
        final DeadboltHandler handler = Mockito.mock(DeadboltHandler.class);
        Mockito.when(handlerCache.get())
               .thenReturn(handler);
        Mockito.when(handler.getSubject(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(handler.beforeAuthCheck(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        comment("deadbolt:subjectNotPresent");
        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletionStage<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    requestHeader);
        ((CompletableFuture) eventualResult).get();
        Assert.assertTrue(flag[0]);
    }

    @Test
    public void testSubjectNotPresent_subjectIsPresent_specificHandler() throws ExecutionException, InterruptedException
    {
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));

        final DeadboltHandler defaultHandler = Mockito.mock(DeadboltHandler.class);
        final DeadboltHandler specificHandler = Mockito.mock(DeadboltHandler.class);
        final HandlerCache handlerCache = new TestHandlerCache(defaultHandler,
                                                               Collections.singletonMap("foo",
                                                                                        specificHandler));
        Mockito.when(specificHandler.getSubject(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));
        Mockito.when(specificHandler.beforeAuthCheck(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(specificHandler.onAuthFailure(Mockito.any(Http.Context.class),
                                                   Mockito.eq(Optional.empty())))
               .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));

        comment("deadbolt:subjectNotPresent:handler[foo]");
        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletionStage<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    requestHeader);
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(specificHandler,
                       Mockito.times(1))
               .onAuthFailure(Mockito.any(Http.Context.class),
                              Mockito.eq(Optional.empty()));
        Mockito.verifyZeroInteractions(defaultHandler);
    }

    @Test
    public void testSubjectNotPresent_subjectIsPresent_specificHandler_withContent() throws ExecutionException, InterruptedException
    {
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));

        final DeadboltHandler defaultHandler = Mockito.mock(DeadboltHandler.class);
        final DeadboltHandler specificHandler = Mockito.mock(DeadboltHandler.class);
        final HandlerCache handlerCache = new TestHandlerCache(defaultHandler,
                                                               Collections.singletonMap("foo",
                                                                                        specificHandler));
        Mockito.when(specificHandler.getSubject(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));
        Mockito.when(specificHandler.beforeAuthCheck(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(specificHandler.onAuthFailure(Mockito.any(Http.Context.class),
                                                   Mockito.eq(Optional.of("bar"))))
               .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));

        comment("deadbolt:subjectNotPresent:content[bar]:handler[foo]");
        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletionStage<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    requestHeader);
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(specificHandler,
                       Mockito.times(1))
               .onAuthFailure(Mockito.any(Http.Context.class),
                              Mockito.eq(Optional.of("bar")));
        Mockito.verifyZeroInteractions(defaultHandler);
    }

    @Test
    public void testSubjectNotPresent_subjectIsNotPresent_specificHandler() throws ExecutionException, InterruptedException
    {
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        final DeadboltHandler defaultHandler = Mockito.mock(DeadboltHandler.class);
        final DeadboltHandler specificHandler = Mockito.mock(DeadboltHandler.class);
        final HandlerCache handlerCache = new TestHandlerCache(defaultHandler,
                                                               Collections.singletonMap("foo",
                                                                                        specificHandler));
        Mockito.when(specificHandler.getSubject(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(specificHandler.beforeAuthCheck(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(specificHandler.onAuthFailure(Mockito.any(Http.Context.class),
                                                   Mockito.eq(Optional.empty())))
               .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));

        comment("deadbolt:subjectNotPresent:handler[foo]");
        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletionStage<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    requestHeader);
        ((CompletableFuture) eventualResult).get();
        Assert.assertTrue(flag[0]);
        Mockito.verifyZeroInteractions(defaultHandler);
    }

    @Test
    public void testDynamic_pass_defaultHandler() throws ExecutionException, InterruptedException
    {
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));

        final HandlerCache handlerCache = Mockito.mock(HandlerCache.class);
        final DeadboltHandler handler = Mockito.mock(DeadboltHandler.class);
        final DynamicResourceHandler drh = Mockito.mock(DynamicResourceHandler.class);
        Mockito.when(handlerCache.get())
               .thenReturn(handler);
        Mockito.when(handler.beforeAuthCheck(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(handler.getDynamicResourceHandler(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(drh)));
        Mockito.when(drh.isAllowed(Mockito.eq("foo"),
                                   Mockito.eq(Optional.empty()),
                                   Mockito.eq(handler),
                                   Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Boolean.TRUE));


        comment("deadbolt:dynamic:name[foo]");
        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletionStage<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    requestHeader);
        ((CompletableFuture) eventualResult).get();
        Assert.assertTrue(flag[0]);
    }

    @Test
    public void testDynamic_fail_defaultHandler() throws ExecutionException, InterruptedException
    {
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));

        final HandlerCache handlerCache = Mockito.mock(HandlerCache.class);
        final DeadboltHandler handler = Mockito.mock(DeadboltHandler.class);
        final DynamicResourceHandler drh = Mockito.mock(DynamicResourceHandler.class);
        Mockito.when(handlerCache.get())
               .thenReturn(handler);
        Mockito.when(handler.beforeAuthCheck(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(handler.getDynamicResourceHandler(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(drh)));
        Mockito.when(drh.isAllowed(Mockito.eq("foo"),
                                   Mockito.eq(Optional.empty()),
                                   Mockito.eq(handler),
                                   Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Boolean.FALSE));
        Mockito.when(handler.onAuthFailure(Mockito.any(Http.Context.class),
                                           Mockito.eq(Optional.empty())))
               .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));

        comment("deadbolt:dynamic:name[foo]");
        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletionStage<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    requestHeader);
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1))
               .onAuthFailure(Mockito.any(Http.Context.class),
                              Mockito.eq(Optional.empty()));
    }

    @Test
    public void testDynamic_fail_withContent() throws ExecutionException, InterruptedException
    {
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));

        final HandlerCache handlerCache = Mockito.mock(HandlerCache.class);
        final DeadboltHandler handler = Mockito.mock(DeadboltHandler.class);
        final DynamicResourceHandler drh = Mockito.mock(DynamicResourceHandler.class);
        Mockito.when(handlerCache.get())
               .thenReturn(handler);
        Mockito.when(handler.beforeAuthCheck(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(handler.getDynamicResourceHandler(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(drh)));
        Mockito.when(drh.isAllowed(Mockito.eq("foo"),
                                   Mockito.eq(Optional.empty()),
                                   Mockito.eq(handler),
                                   Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Boolean.FALSE));
        Mockito.when(handler.onAuthFailure(Mockito.any(Http.Context.class),
                                           Mockito.eq(Optional.of("bar"))))
               .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));


        comment("deadbolt:dynamic:name[foo]:content[bar]");
        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletionStage<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    requestHeader);
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1))
               .onAuthFailure(Mockito.any(Http.Context.class),
                              Mockito.eq(Optional.of("bar")));
    }

    @Test
    public void testDynamic_pass_specificHandler() throws ExecutionException, InterruptedException
    {
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));

        final DeadboltHandler defaultHandler = Mockito.mock(DeadboltHandler.class);
        final DeadboltHandler specificHandler = Mockito.mock(DeadboltHandler.class);
        final DynamicResourceHandler drh = Mockito.mock(DynamicResourceHandler.class);
        final HandlerCache handlerCache = new TestHandlerCache(defaultHandler,
                                                               Collections.singletonMap("gurdy",
                                                                                        specificHandler));
        Mockito.when(specificHandler.beforeAuthCheck(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(specificHandler.getDynamicResourceHandler(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(drh)));
        Mockito.when(drh.isAllowed(Mockito.eq("foo"),
                                   Mockito.eq(Optional.empty()),
                                   Mockito.eq(specificHandler),
                                   Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Boolean.TRUE));

        comment("deadbolt:dynamic:name[foo]:handler[gurdy]");
        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletionStage<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    requestHeader);
        ((CompletableFuture) eventualResult).get();
        Assert.assertTrue(flag[0]);
        Mockito.verifyZeroInteractions(defaultHandler);
    }

    @Test
    public void testDynamic_fail_specificHandler() throws ExecutionException, InterruptedException
    {
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));

        final DeadboltHandler defaultHandler = Mockito.mock(DeadboltHandler.class);
        final DeadboltHandler specificHandler = Mockito.mock(DeadboltHandler.class);
        final DynamicResourceHandler drh = Mockito.mock(DynamicResourceHandler.class);
        final HandlerCache handlerCache = new TestHandlerCache(defaultHandler,
                                                               Collections.singletonMap("gurdy",
                                                                                        specificHandler));
        Mockito.when(specificHandler.beforeAuthCheck(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(specificHandler.getDynamicResourceHandler(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(drh)));
        Mockito.when(drh.isAllowed(Mockito.eq("foo"),
                                   Mockito.eq(Optional.empty()),
                                   Mockito.eq(specificHandler),
                                   Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Boolean.FALSE));
        Mockito.when(specificHandler.onAuthFailure(Mockito.any(Http.Context.class),
                                                   Mockito.eq(Optional.empty())))
               .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));

        comment("deadbolt:dynamic:name[foo]:handler[gurdy]");
        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletionStage<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    requestHeader);
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(specificHandler,
                       Mockito.times(1))
               .onAuthFailure(Mockito.any(Http.Context.class),
                              Mockito.eq(Optional.empty()));
        Mockito.verifyZeroInteractions(defaultHandler);
    }

    @Test
    public void testDynamic_fail_specificHandler_withContent() throws ExecutionException, InterruptedException
    {
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));

        final DeadboltHandler defaultHandler = Mockito.mock(DeadboltHandler.class);
        final DeadboltHandler specificHandler = Mockito.mock(DeadboltHandler.class);
        final DynamicResourceHandler drh = Mockito.mock(DynamicResourceHandler.class);
        final HandlerCache handlerCache = new TestHandlerCache(defaultHandler,
                                                               Collections.singletonMap("gurdy",
                                                                                        specificHandler));
        Mockito.when(specificHandler.beforeAuthCheck(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(specificHandler.getDynamicResourceHandler(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(drh)));
        Mockito.when(drh.isAllowed(Mockito.eq("foo"),
                                   Mockito.eq(Optional.empty()),
                                   Mockito.eq(specificHandler),
                                   Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Boolean.FALSE));
        Mockito.when(specificHandler.onAuthFailure(Mockito.any(Http.Context.class),
                                                   Mockito.eq(Optional.of("bar"))))
               .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));

        comment("deadbolt:dynamic:name[foo]:content[bar]:handler[gurdy]");
        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletionStage<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    requestHeader);
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(specificHandler,
                       Mockito.times(1))
               .onAuthFailure(Mockito.any(Http.Context.class),
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
        Mockito.when(handler.onAuthFailure(Mockito.any(Http.Context.class),
                                           Mockito.eq(Optional.empty())))
               .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));

        comment("deadbolt:sbujectPresent");
        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletionStage<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    requestHeader);
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1))
               .onAuthFailure(Mockito.any(Http.Context.class),
                              Mockito.eq(Optional.empty()));
    }

    @Test
    public void testRoleBasedPermissions_subjectHasPermissions_defaultHandler() throws ExecutionException, InterruptedException
    {
        final Subject subject = new TestSubject.Builder().permission(new TestPermission("bar"))
                                                         .build();
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(subject)));

        final HandlerCache handlerCache = Mockito.mock(HandlerCache.class);
        final DeadboltHandler handler = Mockito.mock(DeadboltHandler.class);
        Mockito.when(handlerCache.get())
               .thenReturn(handler);
        Mockito.when(handler.getSubject(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(subject)));
        Mockito.when(handler.getPermissionsForRole("foo"))
               .then((Answer<CompletionStage<List<? extends Permission>>>) invocation -> CompletableFuture.completedFuture(Collections.singletonList(new TestPermission("bar"))));
        Mockito.when(handler.beforeAuthCheck(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        comment("deadbolt:rbp:name[foo]");
        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletionStage<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    requestHeader);
        ((CompletableFuture) eventualResult).get();
        Assert.assertTrue(flag[0]);
    }

    @Test
    public void testRoleBasedPermissions_subjectDoesNotHavePermission_defaultHandler() throws ExecutionException, InterruptedException
    {
        final Subject subject = new TestSubject.Builder().permission(new TestPermission("hurdy"))
                                                         .build();
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(subject)));

        final HandlerCache handlerCache = Mockito.mock(HandlerCache.class);
        final DeadboltHandler handler = Mockito.mock(DeadboltHandler.class);
        Mockito.when(handlerCache.get())
               .thenReturn(handler);
        Mockito.when(handler.getSubject(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(handler.beforeAuthCheck(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(handler.getPermissionsForRole("foo"))
               .then((Answer<CompletionStage<List<? extends Permission>>>) invocation -> CompletableFuture.completedFuture(Collections.singletonList(new TestPermission("bar"))));
        Mockito.when(handler.onAuthFailure(Mockito.any(Http.Context.class),
                                           Mockito.eq(Optional.empty())))
               .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));

        comment("deadbolt:rbp:name[foo]");
        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletionStage<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    requestHeader);
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1))
               .onAuthFailure(Mockito.any(Http.Context.class),
                              Mockito.eq(Optional.empty()));
    }

    @Test
    public void testRoleBasedPermissions_subjectDoesNotHavePermission_withContent() throws ExecutionException, InterruptedException
    {
        final Subject subject = new TestSubject.Builder().permission(new TestPermission("hurdy"))
                                                         .build();
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(subject)));

        final HandlerCache handlerCache = Mockito.mock(HandlerCache.class);
        final DeadboltHandler handler = Mockito.mock(DeadboltHandler.class);
        Mockito.when(handlerCache.get())
               .thenReturn(handler);
        Mockito.when(handler.getSubject(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(handler.beforeAuthCheck(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(handler.getPermissionsForRole("foo"))
               .then((Answer<CompletionStage<List<? extends Permission>>>) invocation -> CompletableFuture.completedFuture(Collections.singletonList(new TestPermission("bar"))));
        Mockito.when(handler.onAuthFailure(Mockito.any(Http.Context.class),
                                           Mockito.eq(Optional.of("bar"))))
               .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));

        comment("deadbolt:rbp:name[foo]:content[bar]");
        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletionStage<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    requestHeader);
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1))
               .onAuthFailure(Mockito.any(Http.Context.class),
                              Mockito.eq(Optional.of("bar")));
    }

    @Test
    public void testRoleBasedPermissions_subjectHasPermissions_specificHandler() throws ExecutionException, InterruptedException
    {
        final Subject subject = new TestSubject.Builder().permission(new TestPermission("bar"))
                                                         .build();
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(subject)));

        final DeadboltHandler defaultHandler = Mockito.mock(DeadboltHandler.class);
        final DeadboltHandler specificHandler = Mockito.mock(DeadboltHandler.class);
        final HandlerCache handlerCache = new TestHandlerCache(defaultHandler,
                                                               Collections.singletonMap("foo",
                                                                                        specificHandler));
        Mockito.when(specificHandler.getSubject(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));
        Mockito.when(specificHandler.beforeAuthCheck(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(specificHandler.getPermissionsForRole("foo"))
               .then((Answer<CompletionStage<List<? extends Permission>>>) invocation -> CompletableFuture.completedFuture(Collections.singletonList(new TestPermission("bar"))));
        comment("deadbolt:rbp:name[foo]:handler[foo]");
        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletionStage<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    requestHeader);
        ((CompletableFuture) eventualResult).get();
        Assert.assertTrue(flag[0]);
        Mockito.verifyZeroInteractions(defaultHandler);
    }

    @Test
    public void testRoleBasedPermissions_subjectDoesNotHavePermission_specificHandler() throws ExecutionException, InterruptedException
    {
        final Subject subject = new TestSubject.Builder().permission(new TestPermission("hurdy"))
                                                         .build();
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(subject)));

        final DeadboltHandler defaultHandler = Mockito.mock(DeadboltHandler.class);
        final DeadboltHandler specificHandler = Mockito.mock(DeadboltHandler.class);
        final HandlerCache handlerCache = new TestHandlerCache(defaultHandler,
                                                               Collections.singletonMap("foo",
                                                                                        specificHandler));
        Mockito.when(specificHandler.getSubject(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(specificHandler.beforeAuthCheck(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(specificHandler.onAuthFailure(Mockito.any(Http.Context.class),
                                                   Mockito.eq(Optional.empty())))
               .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));
        Mockito.when(specificHandler.getPermissionsForRole("foo"))
               .then((Answer<CompletionStage<List<? extends Permission>>>) invocation -> CompletableFuture.completedFuture(Collections.singletonList(new TestPermission("bar"))));
        comment("deadbolt:rbp:name[foo]:handler[foo]");
        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletionStage<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    requestHeader);
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(specificHandler,
                       Mockito.times(1))
               .onAuthFailure(Mockito.any(Http.Context.class),
                              Mockito.eq(Optional.empty()));
        Mockito.verifyZeroInteractions(defaultHandler);
    }

    @Test
    public void testRoleBasedPermissions_subjectDoesNotHavePermission_specificHandler_withContent() throws ExecutionException, InterruptedException
    {
        final Subject subject = new TestSubject.Builder().permission(new TestPermission("hurdy"))
                                                         .build();
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(subject)));

        final DeadboltHandler defaultHandler = Mockito.mock(DeadboltHandler.class);
        final DeadboltHandler specificHandler = Mockito.mock(DeadboltHandler.class);
        final HandlerCache handlerCache = new TestHandlerCache(defaultHandler,
                                                               Collections.singletonMap("foo",
                                                                                        specificHandler));
        Mockito.when(specificHandler.getSubject(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(specificHandler.beforeAuthCheck(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(specificHandler.onAuthFailure(Mockito.any(Http.Context.class),
                                                   Mockito.eq(Optional.of("bar"))))
               .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));
        Mockito.when(specificHandler.getPermissionsForRole("foo"))
               .then((Answer<CompletionStage<List<? extends Permission>>>) invocation -> CompletableFuture.completedFuture(Collections.singletonList(new TestPermission("bar"))));
        comment("deadbolt:rbp:name[foo]:content[bar]:handler[foo]");
        final Filter filter = new DeadboltRouteCommentFilter(Mockito.mock(Materializer.class),
                                                             handlerCache,
                                                             filterConstraints);
        final boolean[] flag = {false};
        final CompletionStage<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    requestHeader);
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(specificHandler,
                       Mockito.times(1))
               .onAuthFailure(Mockito.any(Http.Context.class),
                              Mockito.eq(Optional.of("bar")));
        Mockito.verifyZeroInteractions(defaultHandler);
    }


    private void comment(final String comment)
    {
        tags.put(Router.Tags.ROUTE_COMMENTS,
                 comment);
    }
}