package be.objectify.deadbolt.java;

import be.objectify.deadbolt.java.cache.DefaultPatternCache;
import be.objectify.deadbolt.java.cache.HandlerCache;
import be.objectify.deadbolt.java.cache.SubjectCache;
import be.objectify.deadbolt.java.testsupport.FakeCache;
import be.objectify.deadbolt.java.testsupport.TestHandlerCache;
import be.objectify.deadbolt.java.testsupport.TestRole;
import be.objectify.deadbolt.java.testsupport.TestSubject;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import play.mvc.Http;

import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

public class ConstraintLogicTest extends AbstractFakeApplicationTest
{
    @Test
    public void testRestrict_pass() throws Exception
    {
        testRestrict(new String[]{"foo"},
                     future -> {
                         try
                         {
                             Assert.assertTrue(future.toCompletableFuture().get());
                         }
                         catch (Exception e)
                         {
                             throw new RuntimeException(e);
                         }
                     });
    }

    @Test
    public void testRestrict_fail() throws Exception
    {
        testRestrict(new String[]{"bar"},
                     future -> {
                         try
                         {
                             Assert.assertFalse(future.toCompletableFuture().get());
                         }
                         catch (Exception e)
                         {
                             throw new RuntimeException(e);
                         }
                     });
    }

    private void testRestrict(final String[] requiredRoles,
                              final Consumer<CompletionStage<Boolean>> test)
    {
        final ExecutionContextProvider ecProvider = Mockito.mock(ExecutionContextProvider.class);
        Mockito.when(ecProvider.get()).thenReturn(new DefaultDeadboltExecutionContextProvider());
        final SubjectCache subjectCache = Mockito.mock(SubjectCache.class);
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(new TestSubject.Builder().role(new TestRole("foo")).build())));
        final ConstraintLogic logic = new ConstraintLogic(new DeadboltAnalyzer(),
                                                          subjectCache,
                                                          new DefaultPatternCache(new FakeCache()),
                                                          ecProvider);

        final CompletionStage<Boolean> result = logic.restrict(context(),
                                                               handler(() -> new TestSubject.Builder().role(new TestRole("foo")).build()),
                                                               Optional.of("json"),
                                                               () -> Collections.singletonList(requiredRoles),
                                                               ctx -> CompletableFuture.completedFuture(true),
                                                               (ctx, handler, context) -> CompletableFuture.completedFuture(false));
        test.accept(result);

    }

    @Test
    public void testDynamic_pass() throws Exception
    {
        testDynamic(true,
                    future -> {
                        try
                        {
                            Assert.assertTrue(future.toCompletableFuture().get());
                        }
                        catch (Exception e)
                        {
                            throw new RuntimeException(e);
                        }
                    });
    }

    @Test
    public void testDynamic_fail() throws Exception
    {
        testDynamic(false,
                    future -> {
                        try
                        {
                            Assert.assertFalse(future.toCompletableFuture().get());
                        }
                        catch (Exception e)
                        {
                            throw new RuntimeException(e);
                        }
                    });
    }

    private void testDynamic(final boolean allow,
                             final Consumer<CompletionStage<Boolean>> test)
    {
        final ExecutionContextProvider ecProvider = Mockito.mock(ExecutionContextProvider.class);
        Mockito.when(ecProvider.get()).thenReturn(new DefaultDeadboltExecutionContextProvider());
        final SubjectCache subjectCache = Mockito.mock(SubjectCache.class);
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(new TestSubject.Builder().role(new TestRole("foo")).build())));
        final ConstraintLogic logic = new ConstraintLogic(new DeadboltAnalyzer(),
                                                          subjectCache,
                                                          new DefaultPatternCache(new FakeCache()),
                                                          ecProvider);

        final CompletionStage<Boolean> result = logic.dynamic(context(),
                                                              withDrh(() -> new AbstractDynamicResourceHandler()
                                                              {
                                                                  @Override
                                                                  public CompletionStage<Boolean> isAllowed(final String name,
                                                                                                            final Optional<String> meta,
                                                                                                            final DeadboltHandler deadboltHandler,
                                                                                                            final Http.Context ctx)
                                                                  {
                                                                      return CompletableFuture.completedFuture(allow);
                                                                  }
                                                              }),
                                                              Optional.of("json"),
                                                              "foo",
                                                              Optional.of("bar"),
                                                              ctx -> CompletableFuture.completedFuture(true),
                                                              (ctx, handler, context) -> CompletableFuture.completedFuture(false));
        test.accept(result);

    }

    @Override
    public HandlerCache handlers()
    {
        // using new instances of handlers in the test
        return new TestHandlerCache(null,
                                    new HashMap<>());
    }
}