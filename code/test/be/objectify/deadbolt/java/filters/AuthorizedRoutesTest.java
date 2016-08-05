/*
 * Copyright 2010-2016 Steve Chaloner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.objectify.deadbolt.java.filters;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class AuthorizedRoutesTest
{

    @Test
    public void testMatchOnMethodAndPath_match()
    {
        final FilterConstraints constraints = Mockito.mock(FilterConstraints.class);
        final AuthorizedRoutes authRoutes = new AuthorizedRoutes(() -> constraints)
        {
            @Override
            public List<AuthorizedRoute> routes()
            {
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
    public void testMatchOnMethodAndPath_noMatchOnMethod()
    {
        final FilterConstraints constraints = Mockito.mock(FilterConstraints.class);
        final AuthorizedRoutes authRoutes = new AuthorizedRoutes(() -> constraints)
        {
            @Override
            public List<AuthorizedRoute> routes()
            {
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
    public void testMatchOnMethodAndPath_noMatchOnPath()
    {
        final FilterConstraints constraints = Mockito.mock(FilterConstraints.class);
        final AuthorizedRoutes authRoutes = new AuthorizedRoutes(() -> constraints)
        {
            @Override
            public List<AuthorizedRoute> routes()
            {
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
    public void testMatchOnPath_match()
    {
        final FilterConstraints constraints = Mockito.mock(FilterConstraints.class);
        final AuthorizedRoutes authRoutes = new AuthorizedRoutes(() -> constraints)
        {
            @Override
            public List<AuthorizedRoute> routes()
            {
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
    public void testMatchOnPath_noMatchOnPath()
    {
        final FilterConstraints constraints = Mockito.mock(FilterConstraints.class);
        final AuthorizedRoutes authRoutes = new AuthorizedRoutes(() -> constraints)
        {
            @Override
            public List<AuthorizedRoute> routes()
            {
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