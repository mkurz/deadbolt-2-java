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
