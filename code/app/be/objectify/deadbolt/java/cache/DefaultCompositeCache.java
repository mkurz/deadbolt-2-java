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
package be.objectify.deadbolt.java.cache;

import be.objectify.deadbolt.java.composite.Constraint;
import be.objectify.deadbolt.java.composite.ExceptionThrowingConstraint;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@Singleton
public class DefaultCompositeCache implements CompositeCache
{
    private static final String NAMESPACE = "composite.constraint.";

    private final Map<String, Constraint> constraints = new HashMap<>();

    @Override
    public Optional<Constraint> apply(final String name)
    {
        final Constraint constraint = constraints.get(NAMESPACE + name);
        return Optional.of(constraint != null ? constraint
                                              : new ExceptionThrowingConstraint(name));
    }

    @Override
    public void register(final String name,
                         final Constraint constraint)
    {
        constraints.put(NAMESPACE + name,
                        constraint);
    }
}
