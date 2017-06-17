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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import akka.stream.Materializer;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.cache.HandlerCache;
import be.objectify.deadbolt.java.models.Subject;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import play.api.routing.HandlerDef;
import play.core.j.JavaContextComponents;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import play.routing.Router;
import play.test.Helpers;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class DeadboltRoutePathFilterTest extends AbstractDeadboltFilterTest
{
    @Test
    public void testPass_defaultHandler() throws ExecutionException, InterruptedException
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
        final DeadboltRoutePathFilter filter = new DeadboltRoutePathFilter(Mockito.mock(Materializer.class),
                                                                           Mockito.mock(JavaContextComponents.class),
                                                                           handlerCache,
                                                                           () -> new AuthorizedRoutes(() -> filterConstraints)
                                                                           {
                                                                               @Override
                                                                               public List<AuthorizedRoute> routes()
                                                                               {
                                                                                   return Collections.singletonList(new AuthorizedRoute(Methods.GET,
                                                                                                                                        "/foo",
                                                                                                                                        filterConstraints.subjectPresent()));
                                                                               }
                                                                           });
        final boolean[] flag = {false};
        final CompletionStage<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    request());
        ((CompletableFuture) eventualResult).get();
        Assert.assertTrue(flag[0]);
    }

    @Test
    public void testFail_defaultHandler_noContent() throws ExecutionException, InterruptedException
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
        final DeadboltRoutePathFilter filter = new DeadboltRoutePathFilter(Mockito.mock(Materializer.class),
                                                                           Mockito.mock(JavaContextComponents.class),
                                                                           handlerCache,
                                                                           () -> new AuthorizedRoutes(() -> filterConstraints)
                                                                           {
                                                                               @Override
                                                                               public List<AuthorizedRoute> routes()
                                                                               {
                                                                                   return Collections.singletonList(new AuthorizedRoute(Methods.GET,
                                                                                                                                        "/foo",
                                                                                                                                        filterConstraints.subjectPresent()));
                                                                               }
                                                                           });
        final boolean[] flag = {false};
        final CompletionStage<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    request());
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1))
               .onAuthFailure(Mockito.any(Http.Context.class),
                              Mockito.eq(Optional.empty()));
    }

    @Test
    public void testFail_defaultHandler_withContent() throws ExecutionException, InterruptedException
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
                                           Mockito.eq(Optional.of("foo"))))
               .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));
        final DeadboltRoutePathFilter filter = new DeadboltRoutePathFilter(Mockito.mock(Materializer.class),
                                                                           Mockito.mock(JavaContextComponents.class),
                                                                           handlerCache,
                                                                           () -> new AuthorizedRoutes(() -> filterConstraints)
                                                                           {
                                                                               @Override
                                                                               public List<AuthorizedRoute> routes()
                                                                               {
                                                                                   return Collections.singletonList(new AuthorizedRoute(Methods.GET,
                                                                                                                                        "/foo",
                                                                                                                                        filterConstraints.subjectPresent(Optional.of("foo"))));
                                                                               }
                                                                           });
        final boolean[] flag = {false};
        final CompletionStage<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    request());
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1))
               .onAuthFailure(Mockito.any(Http.Context.class),
                              Mockito.eq(Optional.of("foo")));
    }

    @Test
    public void testPass_specificHandler() throws ExecutionException, InterruptedException
    {
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));

        final HandlerCache handlerCache = Mockito.mock(HandlerCache.class);
        final DeadboltHandler defaultHandler = Mockito.mock(DeadboltHandler.class);
        final DeadboltHandler specificHandler = Mockito.mock(DeadboltHandler.class);
        Mockito.when(handlerCache.get())
               .thenReturn(defaultHandler);
        Mockito.when(specificHandler.getSubject(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));
        Mockito.when(specificHandler.beforeAuthCheck(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        final DeadboltRoutePathFilter filter = new DeadboltRoutePathFilter(Mockito.mock(Materializer.class),
                                                                           Mockito.mock(JavaContextComponents.class),
                                                                           handlerCache,
                                                                           () -> new AuthorizedRoutes(() -> filterConstraints)
                                                                           {
                                                                               @Override
                                                                               public List<AuthorizedRoute> routes()
                                                                               {
                                                                                   return Collections.singletonList(new AuthorizedRoute(Methods.GET,
                                                                                                                                        "/foo",
                                                                                                                                        filterConstraints.subjectPresent(),
                                                                                                                                        Optional.of(specificHandler)));
                                                                               }
                                                                           });
        final boolean[] flag = {false};
        final CompletionStage<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    request());
        ((CompletableFuture) eventualResult).get();
        Assert.assertTrue(flag[0]);
        Mockito.verifyZeroInteractions(defaultHandler);
    }

    @Test
    public void testFail_specificHandler_noContent() throws ExecutionException, InterruptedException
    {
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        final HandlerCache handlerCache = Mockito.mock(HandlerCache.class);
        final DeadboltHandler defaultHandler = Mockito.mock(DeadboltHandler.class);
        final DeadboltHandler specificHandler = Mockito.mock(DeadboltHandler.class);
        Mockito.when(handlerCache.get())
               .thenReturn(defaultHandler);
        Mockito.when(specificHandler.getSubject(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(specificHandler.beforeAuthCheck(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(specificHandler.onAuthFailure(Mockito.any(Http.Context.class),
                                                   Mockito.eq(Optional.empty())))
               .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));
        final DeadboltRoutePathFilter filter = new DeadboltRoutePathFilter(Mockito.mock(Materializer.class),
                                                                           Mockito.mock(JavaContextComponents.class),
                                                                           handlerCache,
                                                                           () -> new AuthorizedRoutes(() -> filterConstraints)
                                                                           {
                                                                               @Override
                                                                               public List<AuthorizedRoute> routes()
                                                                               {
                                                                                   return Collections.singletonList(new AuthorizedRoute(Methods.GET,
                                                                                                                                        "/foo",
                                                                                                                                        filterConstraints.subjectPresent(),
                                                                                                                                        Optional.of(specificHandler)));
                                                                               }
                                                                           });
        final boolean[] flag = {false};
        final CompletionStage<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    request());
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(specificHandler,
                       Mockito.times(1))
               .onAuthFailure(Mockito.any(Http.Context.class),
                              Mockito.eq(Optional.empty()));
        Mockito.verifyZeroInteractions(defaultHandler);
    }

    @Test
    public void testFail_specificHandler_withContent() throws ExecutionException, InterruptedException
    {
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        final HandlerCache handlerCache = Mockito.mock(HandlerCache.class);
        final DeadboltHandler defaultHandler = Mockito.mock(DeadboltHandler.class);
        final DeadboltHandler specificHandler = Mockito.mock(DeadboltHandler.class);
        Mockito.when(handlerCache.get())
               .thenReturn(defaultHandler);
        Mockito.when(specificHandler.getSubject(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(specificHandler.beforeAuthCheck(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(specificHandler.onAuthFailure(Mockito.any(Http.Context.class),
                                                   Mockito.eq(Optional.of("foo"))))
               .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));
        final DeadboltRoutePathFilter filter = new DeadboltRoutePathFilter(Mockito.mock(Materializer.class),
                                                                           Mockito.mock(JavaContextComponents.class),
                                                                           handlerCache,
                                                                           () -> new AuthorizedRoutes(() -> filterConstraints)
                                                                           {
                                                                               @Override
                                                                               public List<AuthorizedRoute> routes()
                                                                               {
                                                                                   return Collections.singletonList(new AuthorizedRoute(Methods.GET,
                                                                                                                                        "/foo",
                                                                                                                                        filterConstraints.subjectPresent(Optional.of("foo")),
                                                                                                                                        Optional.of(specificHandler)));
                                                                               }
                                                                           });
        final boolean[] flag = {false};
        final CompletionStage<Result> eventualResult = filter.apply(rh ->
                                                                    {
                                                                        flag[0] = true;
                                                                        return CompletableFuture.completedFuture(Results.ok());
                                                                    },
                                                                    request());
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(specificHandler,
                       Mockito.times(1))
               .onAuthFailure(Mockito.any(Http.Context.class),
                              Mockito.eq(Optional.of("foo")));
        Mockito.verifyZeroInteractions(defaultHandler);
    }

    private Http.RequestImpl request() {
        //        final Map<String, String> tags = new HashMap<>();
        //        tags.put(Router.Tags.ROUTE_PATTERN,
        //                 "/foo");
        //        requestHeader = Mockito.mock(Http.RequestHeader.class);
        //        Mockito.when(requestHeader.tags()).thenReturn(tags);
        //        Mockito.when(requestHeader.method()).thenReturn("GET");
        //        Mockito.when(requestHeader.uri()).thenReturn("http://localhost/foo");
        //        Mockito.when(requestHeader.clientCertificateChain()).thenReturn(Optional.empty());
        //        Mockito.when(requestHeader.cookies()).thenReturn(new TestCookies());

        return Helpers.fakeRequest("GET", "http://localhost/foo")
                      .attr(Router.Attrs.HANDLER_DEF,
                            HandlerDef.apply(getClass().getClassLoader(),
                                             "",
                                             "",
                                             "",
                                             null,
                                             "GET",
                                             "/foo",
                                             "",
                                             null))
                      .build();
    }
}