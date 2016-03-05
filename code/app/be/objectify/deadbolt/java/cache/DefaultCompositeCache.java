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
