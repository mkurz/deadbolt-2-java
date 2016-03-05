package be.objectify.deadbolt.java.composite;

import be.objectify.deadbolt.java.DeadboltAnalyzer;
import be.objectify.deadbolt.java.cache.DefaultPatternCache;
import be.objectify.deadbolt.java.testsupport.FakeCache;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class SubjectPresentConstraintBuilderTest extends AbstractSubjectPresentConstraintTest
{
    @Override
    public SubjectPresentConstraint constraint()
    {
        return new ConstraintBuilders(new DeadboltAnalyzer(),
                                      new DefaultPatternCache(new FakeCache())).subjectPresent();
    }
}
