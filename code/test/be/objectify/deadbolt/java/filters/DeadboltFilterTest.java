package be.objectify.deadbolt.java.filters;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import akka.stream.Materializer;
import be.objectify.deadbolt.java.ConstraintLogic;
import be.objectify.deadbolt.java.DeadboltAnalyzer;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.DefaultDeadboltExecutionContextProvider;
import be.objectify.deadbolt.java.ExecutionContextProvider;
import be.objectify.deadbolt.java.cache.CompositeCache;
import be.objectify.deadbolt.java.cache.HandlerCache;
import be.objectify.deadbolt.java.cache.PatternCache;
import be.objectify.deadbolt.java.cache.SubjectCache;
import be.objectify.deadbolt.java.models.Subject;
import be.objectify.deadbolt.java.testsupport.TestCookies;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import play.routing.Router;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class DeadboltFilterTest {

    private final DeadboltAnalyzer analyzer = new DeadboltAnalyzer();
    private FilterConstraints filterConstraints;
    private Http.RequestHeader requestHeader;
    private SubjectCache subjectCache;

    @Before
    public void setUp() {

        final ExecutionContextProvider ecProvider = Mockito.mock(ExecutionContextProvider.class);
        Mockito.when(ecProvider.get())
               .thenReturn(new DefaultDeadboltExecutionContextProvider());

        subjectCache = Mockito.mock(SubjectCache.class);

        final ConstraintLogic constraintLogic = new ConstraintLogic(analyzer,
                                                                    subjectCache,
                                                                    Mockito.mock(PatternCache.class),
                                                                    ecProvider);
        filterConstraints = new FilterConstraints(constraintLogic,
                                                  ecProvider,
                                                  Mockito.mock(CompositeCache.class));

        final Map<String, String> tags = new HashMap<>();
        tags.put(Router.Tags.ROUTE_PATTERN,
                 "/foo");
        requestHeader = Mockito.mock(Http.RequestHeader.class);
        Mockito.when(requestHeader.tags()).thenReturn(tags);
        Mockito.when(requestHeader.method()).thenReturn("GET");
        Mockito.when(requestHeader.uri()).thenReturn("http://localhost/foo");
        Mockito.when(requestHeader.clientCertificateChain()).thenReturn(Optional.empty());
        Mockito.when(requestHeader.cookies()).thenReturn(new TestCookies());
    }

    @After
    public void tearDown() {
        filterConstraints = null;
        requestHeader = null;
        subjectCache = null;
    }

    @Test
    public void testPass_defaultHandler() throws ExecutionException, InterruptedException {

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
        final DeadboltFilter filter = new DeadboltFilter(Mockito.mock(Materializer.class),
                                                         handlerCache,
                                                         () -> new AuthorizedRoutes(() -> filterConstraints) {
                                                             @Override
                                                             public List<AuthorizedRoute> routes() {
                                                                 return Collections.singletonList(new AuthorizedRoute(Methods.GET,
                                                                                                                      "/foo",
                                                                                                                      filterConstraints.subjectPresent()));
                                                             }
                                                         });
        final boolean[] flag = {false};
        final CompletionStage<Result> eventualResult = filter.apply(rh -> {
                                                               flag[0] = true;
                                                               return CompletableFuture.completedFuture(Results.ok());
                                                           },
                                                           requestHeader);
        ((CompletableFuture) eventualResult).get();
        Assert.assertTrue(flag[0]);
    }

    @Test
    public void testFail_defaultHandler_noContent() throws ExecutionException, InterruptedException {

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
        final DeadboltFilter filter = new DeadboltFilter(Mockito.mock(Materializer.class),
                                                         handlerCache,
                                                         () -> new AuthorizedRoutes(() -> filterConstraints) {
                                                             @Override
                                                             public List<AuthorizedRoute> routes() {
                                                                 return Collections.singletonList(new AuthorizedRoute(Methods.GET,
                                                                                                                      "/foo",
                                                                                                                      filterConstraints.subjectPresent()));
                                                             }
                                                         });
        final boolean[] flag = {false};
        final CompletionStage<Result> eventualResult = filter.apply(rh -> {
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
    public void testFail_defaultHandler_withContent() throws ExecutionException, InterruptedException {

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
        final DeadboltFilter filter = new DeadboltFilter(Mockito.mock(Materializer.class),
                                                         handlerCache,
                                                         () -> new AuthorizedRoutes(() -> filterConstraints) {
                                                             @Override
                                                             public List<AuthorizedRoute> routes() {
                                                                 return Collections.singletonList(new AuthorizedRoute(Methods.GET,
                                                                                                                      "/foo",
                                                                                                                      filterConstraints.subjectPresent(Optional.of("foo"))));
                                                             }
                                                         });
        final boolean[] flag = {false};
        final CompletionStage<Result> eventualResult = filter.apply(rh -> {
            flag[0] = true;
            return CompletableFuture.completedFuture(Results.ok());
        },
                                                                    requestHeader);
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1))
                .onAuthFailure(Mockito.any(Http.Context.class),
                               Mockito.eq(Optional.of("foo")));
    }

    @Test
    public void testPass_specificHandler() throws ExecutionException, InterruptedException {

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
        final DeadboltFilter filter = new DeadboltFilter(Mockito.mock(Materializer.class),
                                                         handlerCache,
                                                         () -> new AuthorizedRoutes(() -> filterConstraints) {
                                                             @Override
                                                             public List<AuthorizedRoute> routes() {
                                                                 return Collections.singletonList(new AuthorizedRoute(Methods.GET,
                                                                                                                      "/foo",
                                                                                                                      filterConstraints.subjectPresent(),
                                                                                                                      Optional.of(specificHandler)));
                                                             }
                                                         });
        final boolean[] flag = {false};
        final CompletionStage<Result> eventualResult = filter.apply(rh -> {
                                                               flag[0] = true;
                                                               return CompletableFuture.completedFuture(Results.ok());
                                                           },
                                                           requestHeader);
        ((CompletableFuture) eventualResult).get();
        Assert.assertTrue(flag[0]);
        Mockito.verifyZeroInteractions(defaultHandler);
    }

    @Test
    public void testFail_specificHandler_noContent() throws ExecutionException, InterruptedException {

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
        final DeadboltFilter filter = new DeadboltFilter(Mockito.mock(Materializer.class),
                                                         handlerCache,
                                                         () -> new AuthorizedRoutes(() -> filterConstraints) {
                                                             @Override
                                                             public List<AuthorizedRoute> routes() {
                                                                 return Collections.singletonList(new AuthorizedRoute(Methods.GET,
                                                                                                                      "/foo",
                                                                                                                      filterConstraints.subjectPresent(),
                                                                                                                      Optional.of(specificHandler)));
                                                             }
                                                         });
        final boolean[] flag = {false};
        final CompletionStage<Result> eventualResult = filter.apply(rh -> {
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
    public void testFail_specificHandler_withContent() throws ExecutionException, InterruptedException {

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
        final DeadboltFilter filter = new DeadboltFilter(Mockito.mock(Materializer.class),
                                                         handlerCache,
                                                         () -> new AuthorizedRoutes(() -> filterConstraints) {
                                                             @Override
                                                             public List<AuthorizedRoute> routes() {
                                                                 return Collections.singletonList(new AuthorizedRoute(Methods.GET,
                                                                                                                      "/foo",
                                                                                                                      filterConstraints.subjectPresent(Optional.of("foo")),
                                                                                                                      Optional.of(specificHandler)));
                                                             }
                                                         });
        final boolean[] flag = {false};
        final CompletionStage<Result> eventualResult = filter.apply(rh -> {
            flag[0] = true;
            return CompletableFuture.completedFuture(Results.ok());
        },
                                                                    requestHeader);
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(specificHandler,
                       Mockito.times(1))
                .onAuthFailure(Mockito.any(Http.Context.class),
                               Mockito.eq(Optional.of("foo")));
        Mockito.verifyZeroInteractions(defaultHandler);
    }
}