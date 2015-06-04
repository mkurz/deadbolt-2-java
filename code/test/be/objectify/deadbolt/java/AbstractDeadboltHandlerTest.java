package be.objectify.deadbolt.java;

import be.objectify.deadbolt.core.models.Subject;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Optional;
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
            public F.Promise<Optional<Result>> beforeAuthCheck(Http.Context context)
            {
                return F.Promise.promise(Optional::empty);
            }
        };

        final Http.Context context = Mockito.mock(Http.Context.class);

        final F.Promise<Optional<Subject>> promise = deadboltHandler.getSubject(context);
        Assert.assertNotNull(promise);

        final Optional<Subject> option = promise.get(1000);
        Assert.assertNotNull(option);
        Assert.assertFalse(option.isPresent());
    }

    @Test
    public void testOnAuthFailure()
    {
        DeadboltHandler deadboltHandler = new AbstractDeadboltHandler()
        {
            @Override
            public F.Promise<Optional<Result>> beforeAuthCheck(Http.Context context)
            {
                return F.Promise.promise(Optional::empty);
            }
        };

        final Http.Context context = Mockito.mock(Http.Context.class);

        final F.Promise<Result> promise = deadboltHandler.onAuthFailure(context,
                                                                       "foo");
        Assert.assertNotNull(promise);

        final Result result = promise.get(100, TimeUnit.MILLISECONDS);
        Assert.assertNotNull(result);
        Assert.assertEquals(401,
                            result.status());
    }

    @Test
    public void testGetDynamicResourceHandler()
    {
        DeadboltHandler deadboltHandler = new AbstractDeadboltHandler()
        {
            @Override
            public F.Promise<Optional<Result>> beforeAuthCheck(Http.Context context)
            {
                return F.Promise.promise(Optional::empty);
            }
        };

        final Http.Context context = Mockito.mock(Http.Context.class);

        final F.Promise<Optional<DynamicResourceHandler>> promise = deadboltHandler.getDynamicResourceHandler(context);
        Assert.assertNotNull(promise);

        final Optional<DynamicResourceHandler> option = promise.get(1000);
        Assert.assertNotNull(option);
        Assert.assertFalse(option.isPresent());
    }
}
