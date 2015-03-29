package be.objectify.deadbolt.java;

import be.objectify.deadbolt.core.models.Subject;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;

import java.util.concurrent.TimeUnit;

/**
 * Tests the behaviour of {#link AbstractDeadboltHandler}.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public class AbstractDeadboltHandlerTest
{
    @Test
    public void testGetSubject()
    {
        DeadboltHandler deadboltHandler = new AbstractDeadboltHandler()
        {
            @Override
            public F.Promise<Result> beforeAuthCheck(Http.Context context)
            {
                return null;
            }
        };

        final Http.Context context = Mockito.mock(Http.Context.class);

        final Subject subject = deadboltHandler.getSubject(context);
        Assert.assertNull(subject);
    }

    @Test
    public void testOnAuthFailure()
    {
        DeadboltHandler deadboltHandler = new AbstractDeadboltHandler()
        {
            @Override
            public F.Promise<Result> beforeAuthCheck(Http.Context context)
            {
                return null;
            }
        };

        final Http.Context context = Mockito.mock(Http.Context.class);

        final F.Promise<Result> promise = deadboltHandler.onAuthFailure(context,
                                                                       "foo");
        Assert.assertNotNull(promise);

        final play.api.mvc.Result result = promise.get(100, TimeUnit.MILLISECONDS).toScala();
    }

    @Test
    public void testGetDynamicResourceHandler()
    {
        DeadboltHandler deadboltHandler = new AbstractDeadboltHandler()
        {
            @Override
            public F.Promise<Result> beforeAuthCheck(Http.Context context)
            {
                return null;
            }
        };

        final Http.Context context = Mockito.mock(Http.Context.class);

        final DynamicResourceHandler dynamicResourceHandler = deadboltHandler.getDynamicResourceHandler(context);
        Assert.assertNull(dynamicResourceHandler);
    }
}
