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

import be.objectify.deadbolt.java.DeadboltHandler;

import java.util.Optional;

/**
 * Defines an authorization constraint for a given route.  The route path is the compiled path - you can get
 * a list of these by running your Play app visiting an unmapped URI in development mode.
 * <p>
 * Your routes file may contain routes that are differentiated by method, e.g.
 * <pre>
 *     GET   /foo/bar    controllers.Application.hurdy()
 *     POST  /foo/bar    controllers.Application.gurdy()
 * </pre>
 * You can define two separate constraints for these routes using GET and POST to distinguish between them, or use a single constraint
 * using the ANY method.
 * </p>
 *
 * @author Steve Chaloner (steve@objectify.be)
 * @see FilterConstraints
 * @see Methods
 * @since 2.5.1
 */
public class AuthorizedRoute
{
    private final Optional<String> method;
    private final String path;
    private final FilterFunction constraint;
    private final Optional<DeadboltHandler> handler;

    /**
     * Define a route constraint that uses the default {@link DeadboltHandler}.  All standard Deadbolt constraints can be
     * created using {@link FilterConstraints}, or you can use a completely arbitrary {@link FilterFunction} implementation.
     *
     * @param method     the HTTP method to match.  Use {@link Methods#ANY} if you want all methods for a given path to have the constraint.
     * @param path       the route path
     * @param constraint the constraint to apply to the route
     * @see Methods
     */
    public AuthorizedRoute(final Optional<String> method,
                           final String path,
                           final FilterFunction constraint)
    {
        this(method,
             path,
             constraint,
             Optional.empty());
    }

    /**
     * Define a route constraint that uses a specific {@link DeadboltHandler}.  All standard Deadbolt constraints can be
     * created using {@link FilterConstraints}, or you can use a completely arbitrary {@link FilterFunction} implementation.
     *
     * @param method     the HTTP method to match.  Use {@link Methods#ANY} if you want all methods for a given path to have the constraint.
     * @param path       the route path
     * @param constraint the constraint to apply to the route
     * @param handler    the handler to use for this constraint
     * @see Methods
     */
    public AuthorizedRoute(final Optional<String> method,
                           final String path,
                           final FilterFunction constraint,
                           final Optional<DeadboltHandler> handler)
    {
        this.method = method;
        this.path = path;
        this.constraint = constraint;
        this.handler = handler;
    }

    public FilterFunction constraint()
    {
        return constraint;
    }

    public Optional<DeadboltHandler> handler()
    {
        return handler;
    }

    public Optional<String> method()
    {
        return method;
    }

    public String path()
    {
        return path;
    }
}
