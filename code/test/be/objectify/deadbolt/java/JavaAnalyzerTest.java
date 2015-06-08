package be.objectify.deadbolt.java;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import play.libs.F;
import play.mvc.Http;

import java.util.Optional;

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
    public void testCheckCustomPattern_patternDoesNotPass()
    {
        DynamicResourceHandler dynamicResourceHandler = new AbstractDynamicResourceHandler()
        {
            @Override
            public F.Promise<Boolean> checkPermission(String permissionValue,
                                           DeadboltHandler deadboltHandler,
                                           Http.Context ctx)
            {
                return F.Promise.pure(false);
            }
        };

        final DeadboltHandler deadboltHandler = Mockito.mock(DeadboltHandler.class);
        final Http.Context context = Mockito.mock(Http.Context.class);
        Mockito.when(deadboltHandler.getDynamicResourceHandler(context))
               .thenReturn(F.Promise.promise(() -> Optional.of(dynamicResourceHandler)));

        final F.Promise<Boolean> result = new JavaAnalyzer().checkCustomPattern(deadboltHandler,
                                                                                        context,
                                                                                        "foo");
        Assert.assertFalse(result.get(1000));
    }

    @Test
    public void testCheckCustomPattern_patternPasses()
    {
        DynamicResourceHandler dynamicResourceHandler = new AbstractDynamicResourceHandler()
        {
            @Override
            public F.Promise<Boolean> checkPermission(String permissionValue,
                                           DeadboltHandler deadboltHandler,
                                           Http.Context ctx)
            {
                return F.Promise.pure(true);
            }
        };

        final DeadboltHandler deadboltHandler = Mockito.mock(DeadboltHandler.class);
        final Http.Context context = Mockito.mock(Http.Context.class);
        Mockito.when(deadboltHandler.getDynamicResourceHandler(context))
               .thenReturn(F.Promise.promise(() -> Optional.of(dynamicResourceHandler)));

        final F.Promise<Boolean> result = new JavaAnalyzer().checkCustomPattern(deadboltHandler,
                                                                                        context,
                                                                                        "foo");
        Assert.assertTrue(result.get(1000));
    }
}
