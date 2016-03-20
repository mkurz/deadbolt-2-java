package be.objectify.deadbolt.java.composite;

import be.objectify.deadbolt.java.DeadboltHandler;

import java.util.List;
import java.util.Optional;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class RestrictConstraintTest extends AbstractRestrictConstraintTest implements ConstraintLogicMixin
{
    @Override
    protected RestrictConstraint constraint(final DeadboltHandler handler,
                                            final List<String[]> roleGroups)
    {
        return new RestrictConstraint(roleGroups,
                                      Optional.empty(),
                                      logic(handler));
    }
}
