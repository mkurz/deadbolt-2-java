package be.objectify.deadbolt.java.filters;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class AuthorizedRoutesTest {

    @Test
    public void testMatchOnMethodAndPath_match() {
        final FilterConstraints constraints = Mockito.mock(FilterConstraints.class);
        final AuthorizedRoutes authRoutes = new AuthorizedRoutes(() -> constraints) {
            @Override
            public List<AuthorizedRoute> routes() {
                return Collections.singletonList(new AuthorizedRoute(Methods.GET,
                                                                     "/foo",
                                                                     Mockito.mock(FilterFunction.class)));
            }
        };

        final Optional<AuthorizedRoute> maybeRoute = authRoutes.apply("GET",
                                                                      "/foo");
        Assert.assertTrue(maybeRoute.isPresent());
    }

    @Test
    public void testMatchOnMethodAndPath_noMatchOnMethod() {
        final FilterConstraints constraints = Mockito.mock(FilterConstraints.class);
        final AuthorizedRoutes authRoutes = new AuthorizedRoutes(() -> constraints) {
            @Override
            public List<AuthorizedRoute> routes() {
                return Collections.singletonList(new AuthorizedRoute(Methods.GET,
                                                                     "/foo",
                                                                     Mockito.mock(FilterFunction.class)));
            }
        };

        final Optional<AuthorizedRoute> maybeRoute = authRoutes.apply("POST",
                                                                      "/foo");
        Assert.assertFalse(maybeRoute.isPresent());
    }

    @Test
    public void testMatchOnMethodAndPath_noMatchOnPath() {
        final FilterConstraints constraints = Mockito.mock(FilterConstraints.class);
        final AuthorizedRoutes authRoutes = new AuthorizedRoutes(() -> constraints) {
            @Override
            public List<AuthorizedRoute> routes() {
                return Collections.singletonList(new AuthorizedRoute(Methods.GET,
                                                                     "/foo",
                                                                     Mockito.mock(FilterFunction.class)));
            }
        };

        final Optional<AuthorizedRoute> maybeRoute = authRoutes.apply("GET",
                                                                      "/foo/bar");
        Assert.assertFalse(maybeRoute.isPresent());
    }

    @Test
    public void testMatchOnPath_match() {
        final FilterConstraints constraints = Mockito.mock(FilterConstraints.class);
        final AuthorizedRoutes authRoutes = new AuthorizedRoutes(() -> constraints) {
            @Override
            public List<AuthorizedRoute> routes() {
                return Collections.singletonList(new AuthorizedRoute(Methods.ANY,
                                                                     "/foo",
                                                                     Mockito.mock(FilterFunction.class)));
            }
        };

        final Optional<AuthorizedRoute> maybeRoute = authRoutes.apply("PUT",
                                                                      "/foo");
        Assert.assertTrue(maybeRoute.isPresent());
    }

    @Test
    public void testMatchOnPath_noMatchOnPath() {
        final FilterConstraints constraints = Mockito.mock(FilterConstraints.class);
        final AuthorizedRoutes authRoutes = new AuthorizedRoutes(() -> constraints) {
            @Override
            public List<AuthorizedRoute> routes() {
                return Collections.singletonList(new AuthorizedRoute(Methods.ANY,
                                                                     "/foo",
                                                                     Mockito.mock(FilterFunction.class)));
            }
        };

        final Optional<AuthorizedRoute> maybeRoute = authRoutes.apply("GET",
                                                                      "/foo/bar");
        Assert.assertFalse(maybeRoute.isPresent());
    }
}