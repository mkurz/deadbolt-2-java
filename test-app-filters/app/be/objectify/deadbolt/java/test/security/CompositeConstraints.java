/*
 * Copyright 2013 Steve Chaloner
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
package be.objectify.deadbolt.java.test.security;

import be.objectify.deadbolt.java.cache.CompositeCache;
import be.objectify.deadbolt.java.composite.ConstraintBuilders;
import be.objectify.deadbolt.java.composite.ConstraintTree;
import be.objectify.deadbolt.java.composite.Operator;
import be.objectify.deadbolt.java.models.PatternType;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@Singleton
public class CompositeConstraints
{
    @Inject
    public CompositeConstraints(final CompositeCache compositeCache,
                                final ConstraintBuilders builders)
    {
        compositeCache.register("curatorOrSubjectNotPresent",
                                new ConstraintTree(Operator.OR,
                                                   builders.subjectNotPresent().build(),
                                                   builders.pattern("curator.museum.*",
                                                                    PatternType.REGEX).build()));

        compositeCache.register("fooAndBar",
                                builders.restrict(builders.anyOf(builders.allOf("foo", "bar"))).build());
        compositeCache.register("fooOrBar",
                                builders.restrict(builders.anyOf(builders.allOf("foo"), builders.allOf("bar"))).build());
        compositeCache.register("fooAndNotBar",
                                builders.restrict(builders.anyOf(builders.allOf("foo", "!bar"))).build());
        compositeCache.register("fooOrNotBar",
                                builders.restrict(builders.anyOf(builders.allOf("foo"), builders.allOf("!bar"))).build());
    }
}
