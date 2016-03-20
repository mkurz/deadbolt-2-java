package be.objectify.deadbolt.java.composite;

import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.models.PatternType;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class PatternConstraintBuilderTest extends AbstractPatternConstraintTest implements ConstraintLogicMixin
{
    @Override
    protected PatternConstraint constraint(final PatternType patternType,
                                           final DeadboltHandler handler)
    {
        final ConstraintBuilders builders = new ConstraintBuilders(logic(handler));
        final PatternConstraint constraint;
        switch (patternType)
        {
            case EQUALITY:
                constraint = builders.pattern("foo",
                                              PatternType.EQUALITY)
                                     .build();
                break;
            case REGEX:
                constraint = builders.pattern("[0-2]",
                                              PatternType.REGEX)
                                     .build();
                break;
            case CUSTOM:
                constraint = builders.pattern("blah",
                                              PatternType.CUSTOM)
                                     .build();
                break;
            default:
                throw new IllegalArgumentException("Unknown pattern type");
        }
        return constraint;
    }
}
