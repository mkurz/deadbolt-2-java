package be.objectify.deadbolt.java.filters;

import java.util.concurrent.Executors;
import be.objectify.deadbolt.java.ConstraintLogic;
import be.objectify.deadbolt.java.DeadboltAnalyzer;
import be.objectify.deadbolt.java.DefaultDeadboltExecutionContextProvider;
import be.objectify.deadbolt.java.ExecutionContextProvider;
import be.objectify.deadbolt.java.cache.CompositeCache;
import be.objectify.deadbolt.java.cache.DefaultPatternCache;
import be.objectify.deadbolt.java.cache.SubjectCache;
import org.junit.After;
import org.junit.Before;
import org.mockito.Mockito;
import play.core.j.HttpExecutionContext;
import scala.concurrent.ExecutionContext;

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
        final ExecutionContextProvider ecProvider = Mockito.mock(ExecutionContextProvider.class);
        Mockito.when(ecProvider.get())
               .thenReturn(new DefaultDeadboltExecutionContextProvider(HttpExecutionContext.fromThread(Executors.newSingleThreadExecutor())));

        subjectCache = Mockito.mock(SubjectCache.class);

        final ConstraintLogic constraintLogic = new ConstraintLogic(analyzer,
                                                                    subjectCache,
                                                                    new DefaultPatternCache(),
                                                                    ecProvider);
        filterConstraints = new FilterConstraints(constraintLogic,
                                                  ecProvider,
                                                  Mockito.mock(CompositeCache.class));
    }

    @After
    public void tearDown()
    {
        filterConstraints = null;
        subjectCache = null;
    }
}
