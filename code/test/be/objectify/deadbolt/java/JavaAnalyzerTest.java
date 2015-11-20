package be.objectify.deadbolt.java;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import play.mvc.Http;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class JavaAnalyzerTest
{
    @Test(expected = RuntimeException.class)
    public void testCheckCustomPattern_noDynamicResourceHandler()
    {
        final DeadboltHandler deadboltHandler = Mockito.mock(DeadboltHandler.class);
        final Http.Context context = Mockito.mock(Http.Context.class);
        Mockito.when(deadboltHandler.getDynamicResourceHandler(context))
               .thenReturn(null);

        new JavaAnalyzer().checkCustomPattern(deadboltHandler,
                                              context,
                                              "foo");
    }

    @Test
    public void testCheckCustomPattern_patternDoesNotPass() throws Exception
    {
        DynamicResourceHandler dynamicResourceHandler = new AbstractDynamicResourceHandler()
        {
            @Override
            public CompletionStage<Boolean> checkPermission(final String permissionValue,
                                                            final DeadboltHandler deadboltHandler,
                                                            final Http.Context ctx)
            {
                return CompletableFuture.completedFuture(false);
            }
        };

        final DeadboltHandler deadboltHandler = Mockito.mock(DeadboltHandler.class);
        final Http.Context context = Mockito.mock(Http.Context.class);
        Mockito.when(deadboltHandler.getDynamicResourceHandler(context))
               .thenReturn(CompletableFuture.supplyAsync(() -> Optional.of(dynamicResourceHandler)));

        final CompletionStage<Boolean> result = new JavaAnalyzer().checkCustomPattern(deadboltHandler,
                                                                                      context,
                                                                                      "foo");
        Assert.assertFalse(result.toCompletableFuture().get(1000,
                                                            TimeUnit.MILLISECONDS));
    }

    @Test
    public void testCheckCustomPattern_patternPasses() throws Exception
    {
        DynamicResourceHandler dynamicResourceHandler = new AbstractDynamicResourceHandler()
        {
            @Override
            public CompletionStage<Boolean> checkPermission(final String permissionValue,
                                                            final DeadboltHandler deadboltHandler,
                                                            final Http.Context ctx)
            {
                return CompletableFuture.completedFuture(true);
            }
        };

        final DeadboltHandler deadboltHandler = Mockito.mock(DeadboltHandler.class);
        final Http.Context context = Mockito.mock(Http.Context.class);
        Mockito.when(deadboltHandler.getDynamicResourceHandler(context))
               .thenReturn(CompletableFuture.supplyAsync(() -> Optional.of(dynamicResourceHandler)));

        final CompletionStage<Boolean> result = new JavaAnalyzer().checkCustomPattern(deadboltHandler,
                                                                                      context,
                                                                                      "foo");
        Assert.assertTrue(result.toCompletableFuture().get(1000,
                                                           TimeUnit.MILLISECONDS));
    }
}
