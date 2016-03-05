package be.objectify.deadbolt.java.composite;

import be.objectify.deadbolt.java.DeadboltAnalyzer;

import java.util.List;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class RestrictConstraintTest extends AbstractRestrictConstraintTest
{
    @Override
    protected RestrictConstraint constraint(List<String[]> roleGroups)
    {
        return new RestrictConstraint(roleGroups,
                                      new DeadboltAnalyzer());
    }
}
