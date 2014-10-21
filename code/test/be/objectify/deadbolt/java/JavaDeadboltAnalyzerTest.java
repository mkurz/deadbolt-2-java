package be.objectify.deadbolt.java;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import play.mvc.Http;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class JavaDeadboltAnalyzerTest
{
    @Test(expected = RuntimeException.class)
    public void testCheckCustomPattern_noDynamicResourceHandler()
    {
        final DeadboltHandler deadboltHandler = Mockito.mock(DeadboltHandler.class);
        final Http.Context context = Mockito.mock(Http.Context.class);
        Mockito.when(deadboltHandler.getDynamicResourceHandler(context))
               .thenReturn(null);

        JavaDeadboltAnalyzer.checkCustomPattern(deadboltHandler,
                                                context,
                                                "foo");
    }

    @Test
    public void testCheckCustomPattern_patternDoesNotPass()
    {
        DynamicResourceHandler dynamicResourceHandler = new AbstractDynamicResourceHandler()
        {
            @Override
            public boolean checkPermission(String permissionValue,
                                           DeadboltHandler deadboltHandler,
                                           Http.Context ctx)
            {
                return false;
            }
        };

        final DeadboltHandler deadboltHandler = Mockito.mock(DeadboltHandler.class);
        final Http.Context context = Mockito.mock(Http.Context.class);
        Mockito.when(deadboltHandler.getDynamicResourceHandler(context))
               .thenReturn(dynamicResourceHandler);

        final boolean result = JavaDeadboltAnalyzer.checkCustomPattern(deadboltHandler,
                                                                       context,
                                                                       "foo");
        Assert.assertFalse(result);
    }

    @Test
    public void testCheckCustomPattern_patternPasses()
    {
        DynamicResourceHandler dynamicResourceHandler = new AbstractDynamicResourceHandler()
        {
            @Override
            public boolean checkPermission(String permissionValue,
                                           DeadboltHandler deadboltHandler,
                                           Http.Context ctx)
            {
                return true;
            }
        };

        final DeadboltHandler deadboltHandler = Mockito.mock(DeadboltHandler.class);
        final Http.Context context = Mockito.mock(Http.Context.class);
        Mockito.when(deadboltHandler.getDynamicResourceHandler(context))
               .thenReturn(dynamicResourceHandler);

        final boolean result = JavaDeadboltAnalyzer.checkCustomPattern(deadboltHandler,
                                                                       context,
                                                                       "foo");
        Assert.assertTrue(result);
    }
}
