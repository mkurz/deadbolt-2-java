package be.objectify.deadbolt.java.composite;

import be.objectify.deadbolt.java.DeadboltHandler;

import java.util.List;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class RestrictConstraintBuilderTest extends AbstractRestrictConstraintTest implements ConstraintLogicMixin
{
    @Override
    protected RestrictConstraint constraint(final DeadboltHandler handler,
                                            List<String[]> roleGroups)
    {
        return new ConstraintBuilders(logic(handler)).restrict(roleGroups)
                                                     .build();
    }
}
