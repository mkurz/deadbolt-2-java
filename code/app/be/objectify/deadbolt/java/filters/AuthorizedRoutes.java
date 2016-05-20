/*
 * Copyright 2012-2016 Steve Chaloner
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

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import javax.inject.Provider;

/**
 * Matches an invoked route to a constraint.  If a constraint is present for that route, it
 * determines if the corresponding action should be invoked.
 *
 * @since 2.5.1
 * @author Steve Chaloner (steve@objectify.be)
 */
public abstract class AuthorizedRoutes implements BiFunction<String, String, Optional<AuthorizedRoute>> {

    public final FilterConstraints filterConstraints;

    public AuthorizedRoutes(final Provider<FilterConstraints> filterConstraints) {
        this.filterConstraints = filterConstraints.get();
    }

    @Override
    public Optional<AuthorizedRoute> apply(final String method,
                                           final String path) {
        return routes().stream()
                       .filter(authRoute -> authRoute.method()
                                                     .map(routeMethod -> routeMethod.equals(method) && authRoute.path().equals(path))
                                                     .orElseGet(() -> authRoute.path().equals(path)))
                       .findFirst();
    }

    public abstract List<AuthorizedRoute> routes();
}
