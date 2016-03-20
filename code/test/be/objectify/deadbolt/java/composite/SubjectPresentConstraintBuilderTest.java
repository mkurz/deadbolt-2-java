package be.objectify.deadbolt.java.composite;

import be.objectify.deadbolt.java.DeadboltHandler;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class SubjectPresentConstraintBuilderTest extends AbstractSubjectPresentConstraintTest implements ConstraintLogicMixin
{
    @Override
    public SubjectPresentConstraint constraint(final DeadboltHandler handler)
    {
        return new ConstraintBuilders(logic(handler)).subjectPresent()
                                                     .build();
    }
}
