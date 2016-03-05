package be.objectify.deadbolt.java.composite;

import be.objectify.deadbolt.java.DeadboltAnalyzer;
import be.objectify.deadbolt.java.cache.DefaultPatternCache;
import be.objectify.deadbolt.java.models.PatternType;
import be.objectify.deadbolt.java.testsupport.FakeCache;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class PatternConstraintBuilderTest extends AbstractPatternConstraintTest
{
    @Override
    protected PatternConstraint constraint(final PatternType patternType)
    {
        final ConstraintBuilders builders = new ConstraintBuilders(new DeadboltAnalyzer(),
                                                                   new DefaultPatternCache(new FakeCache()));
        final PatternConstraint constraint;
        switch (patternType)
        {
            case EQUALITY:
                constraint = builders.pattern("foo",
                                              PatternType.EQUALITY);
                break;
            case REGEX:
                constraint = builders.pattern("[0-2]",
                                              PatternType.REGEX);
                break;
            case CUSTOM:
                constraint = builders.pattern("blah",
                                              PatternType.CUSTOM);
                break;
            default:
                throw new IllegalArgumentException("Unknown pattern type");
        }
        return constraint;
    }
}
