package be.objectify.deadbolt.java.filters;

import be.objectify.deadbolt.java.ConstraintLogic;
import be.objectify.deadbolt.java.DeadboltAnalyzer;
import be.objectify.deadbolt.java.cache.CompositeCache;
import be.objectify.deadbolt.java.cache.DefaultPatternCache;
import be.objectify.deadbolt.java.cache.SubjectCache;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mockito;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public abstract class AbstractDeadboltFilterTest {

    final DeadboltAnalyzer analyzer = new DeadboltAnalyzer();
    FilterConstraints filterConstraints;
    SubjectCache subjectCache;

    @Before
    public void setUp()
    {
        subjectCache = Mockito.mock(SubjectCache.class);

        final ConstraintLogic constraintLogic = new ConstraintLogic(analyzer,
                                                                    subjectCache,
                                                                    new DefaultPatternCache());
        filterConstraints = new FilterConstraints(constraintLogic,
                                                  Mockito.mock(CompositeCache.class));
    }

    @After
    public void tearDown()
    {
        filterConstraints = null;
        subjectCache = null;
    }
}
