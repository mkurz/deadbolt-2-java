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

import akka.stream.Materializer;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.cache.HandlerCache;
import play.core.j.JavaContextComponents;
import play.libs.streams.Accumulator;
import play.mvc.EssentialAction;
import play.mvc.StatusHeader;
import play.routing.Router;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Applies constraints before the action is invoked, allowing constraints to be defined outside of controllers.
 *
 * @author Steve Chaloner (steve@objectify.be)
 * @since 2.5.1
 */
public class DeadboltRoutePathFilter extends AbstractDeadboltFilter
{
    private final Materializer mat;
    private final DeadboltHandler handler;
    private final AuthorizedRoutes authorizedRoutes;

    @Inject
    public DeadboltRoutePathFilter(final Materializer mat,
                                   final JavaContextComponents javaContextComponents,
                                   final HandlerCache handlerCache,
                                   final Provider<AuthorizedRoutes> authorizedRoutes)
    {
        super(javaContextComponents);
        this.mat = mat;
        this.handler = handlerCache.get();
        this.authorizedRoutes = authorizedRoutes.get();
    }

    /**
     * If a constraint is defined for a given route, test that constraint before allowing the request to proceed.
     *
     * @param next          the next step in the filter chain
     * @param requestHeader the request header
     * @return a future for the result
     */
    @Override
    public EssentialAction apply(final EssentialAction next)
    {
        return EssentialAction.of(request -> {
            final int fakeHttpStatusHeader = 9999999;
            final Optional<AuthorizedRoute> maybeAuthRoute = authorizedRoutes.apply(request.method(),
                                                                                request.attrs().get(Router.Attrs.HANDLER_DEF).path());
            return maybeAuthRoute.map(authRoute -> Accumulator.flatten(authRoute.constraint().apply(context(request),
                                                                            request,
                                                                            authRoute.handler().orElse(handler),
                                                                            header -> CompletableFuture.completedFuture(new StatusHeader(fakeHttpStatusHeader))).thenApply(myResult -> {
                                                                                if(myResult.status() == fakeHttpStatusHeader) {
                                                                                    // success
                                                                                    return next.apply(request);
                                                                                } else {
                                                                                    return Accumulator.done(myResult);
                                                                                }
                                                                            }), this.mat)
            ).orElseGet(() -> next.apply(request));
        });
    }
}
