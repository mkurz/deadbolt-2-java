package be.objectify.deadbolt.java.filters;

import be.objectify.deadbolt.java.DeadboltHandler;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import play.mvc.Results;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static be.objectify.deadbolt.java.filters.Methods.GET;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class AuthorizedRouteTest
{

    @Test
    public void constructorWithDefaultHandler()
    {
        final FilterFunction constraint = (context, requestHeader, handler, onSuccess) -> CompletableFuture.completedFuture(Results.ok());
        final AuthorizedRoute route = new AuthorizedRoute(GET,
                                                          "/foo",
                                                          constraint);

        Assert.assertEquals(GET,
                            route.method());
        Assert.assertEquals("/foo",
                            route.path());
        Assert.assertEquals(constraint,
                            route.constraint());

        Assert.assertFalse(route.handler().isPresent());
    }

    @Test
    public void constructorWithSpecificHandler()
    {
        final FilterFunction constraint = (context, requestHeader, handler, onSuccess) -> CompletableFuture.completedFuture(Results.ok());
        final DeadboltHandler handler = Mockito.mock(DeadboltHandler.class);
        final AuthorizedRoute route = new AuthorizedRoute(GET,
                                                          "/foo",
                                                          constraint,
                                                          Optional.of(handler));

        Assert.assertEquals(GET,
                            route.method());
        Assert.assertEquals("/foo",
                            route.path());
        Assert.assertEquals(constraint,
                            route.constraint());
        Assert.assertEquals(route.handler().orElse(null),
                            handler);
    }
}