package be.objectify.deadbolt.java.filters;

import akka.stream.Materializer;
import be.objectify.deadbolt.java.ConstraintLogic;
import be.objectify.deadbolt.java.DeadboltAnalyzer;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.DefaultDeadboltExecutionContextProvider;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import be.objectify.deadbolt.java.ExecutionContextProvider;
import be.objectify.deadbolt.java.cache.CompositeCache;
import be.objectify.deadbolt.java.cache.HandlerCache;
import be.objectify.deadbolt.java.cache.PatternCache;
import be.objectify.deadbolt.java.cache.SubjectCache;
import be.objectify.deadbolt.java.models.Subject;
import be.objectify.deadbolt.java.testsupport.TestCookies;
import be.objectify.deadbolt.java.testsupport.TestHandlerCache;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import play.mvc.Filter;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import play.routing.Router;

import java.util.Collections;
import java.util.HashMap;
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
        final CompletionStage<Result> eventualResult = filter.apply(rh -> {
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
        final CompletionStage<Result> eventualResult = filter.apply(rh -> {
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
        final CompletionStage<Result> eventualResult = filter.apply(rh -> {
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

    private void comment(final String comment)
    {
        tags.put(Router.Tags.ROUTE_COMMENTS,
                 comment);

    }
}