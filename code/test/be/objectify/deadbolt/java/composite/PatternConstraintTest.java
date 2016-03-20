package be.objectify.deadbolt.java.composite;

import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.models.PatternType;

import java.util.Optional;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class PatternConstraintTest extends AbstractPatternConstraintTest implements ConstraintLogicMixin
{
    @Override
    protected PatternConstraint constraint(final PatternType patternType,
                                           final DeadboltHandler handler)
    {
        final PatternConstraint constraint;
        switch (patternType)
        {
            case EQUALITY:
                constraint = new PatternConstraint("foo",
                                                   PatternType.EQUALITY,
                                                   Optional.empty(),
                                                   false,
                                                   Optional.empty(),
                                                   logic(handler));
                break;
            case REGEX:
                constraint = new PatternConstraint("[0-2]",
                                                   PatternType.REGEX,
                                                   Optional.empty(),
                                                   false,
                                                   Optional.empty(),
                                                   logic(handler));
                break;
            case CUSTOM:
                constraint = new PatternConstraint("blah",
                                                   PatternType.CUSTOM,
                                                   Optional.empty(),
                                                   false,
                                                   Optional.empty(),
                                                   logic(handler));
                break;
            default:
                throw new IllegalArgumentException("Unknown pattern type");
        }
        return constraint;
    }
}
