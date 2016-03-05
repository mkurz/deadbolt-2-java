package be.objectify.deadbolt.java.composite;

import be.objectify.deadbolt.java.DeadboltAnalyzer;
import be.objectify.deadbolt.java.cache.DefaultPatternCache;
import be.objectify.deadbolt.java.models.PatternType;
import be.objectify.deadbolt.java.testsupport.FakeCache;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class PatternConstraintTest extends AbstractPatternConstraintTest
{
    @Override
    protected PatternConstraint constraint(final PatternType patternType)
    {
        final PatternConstraint constraint;
        switch (patternType)
        {
            case EQUALITY:
                constraint = new PatternConstraint("foo",
                                                   PatternType.EQUALITY,
                                                   new DeadboltAnalyzer(),
                                                   new DefaultPatternCache(new FakeCache()));
                break;
            case REGEX:
                constraint = new PatternConstraint("[0-2]",
                                                   PatternType.REGEX,
                                                   new DeadboltAnalyzer(),
                                                   new DefaultPatternCache(new FakeCache()));
                break;
            case CUSTOM:
                constraint = new PatternConstraint("blah",
                                                   PatternType.CUSTOM,
                                                   new DeadboltAnalyzer(),
                                                   new DefaultPatternCache(new FakeCache()));
                break;
            default:
                throw new IllegalArgumentException("Unknown pattern type");
        }
        return constraint;
    }
}
