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
                                                   builders.subjectNotPresent(),
                                                   builders.pattern("curator.museum.*",
                                                                    PatternType.REGEX)));
    }
}
