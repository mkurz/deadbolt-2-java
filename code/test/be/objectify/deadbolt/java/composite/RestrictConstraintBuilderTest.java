package be.objectify.deadbolt.java.composite;

import be.objectify.deadbolt.java.DeadboltAnalyzer;
import be.objectify.deadbolt.java.cache.DefaultPatternCache;
import be.objectify.deadbolt.java.testsupport.FakeCache;

import java.util.List;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class RestrictConstraintBuilderTest extends AbstractRestrictConstraintTest
{
    @Override
    protected RestrictConstraint constraint(List<String[]> roleGroups)
    {
        return new ConstraintBuilders(new DeadboltAnalyzer(),
                                      new DefaultPatternCache(new FakeCache())).restrict(roleGroups);
    }
}
