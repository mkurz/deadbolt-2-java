package be.objectify.deadbolt.java.composite;

import be.objectify.deadbolt.java.ConstraintLogic;
import be.objectify.deadbolt.java.DeadboltAnalyzer;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.DefaultDeadboltExecutionContextProvider;
import be.objectify.deadbolt.java.ExecutionContextProvider;
import be.objectify.deadbolt.java.cache.DefaultPatternCache;
import be.objectify.deadbolt.java.cache.SubjectCache;
import be.objectify.deadbolt.java.testsupport.FakeCache;
import org.mockito.Mockito;
import play.mvc.Http;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public interface ConstraintLogicMixin
{
    default public ConstraintLogic logic(final DeadboltHandler deadboltHandler)
    {
        final ExecutionContextProvider ecProvider = Mockito.mock(ExecutionContextProvider.class);
        Mockito.when(ecProvider.get()).thenReturn(new DefaultDeadboltExecutionContextProvider());
        final SubjectCache subjectCache = Mockito.mock(SubjectCache.class);
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.Context.class)))
               .thenReturn(deadboltHandler.getSubject(Mockito.mock(Http.Context.class)));
        return new ConstraintLogic(new DeadboltAnalyzer(),
                                   subjectCache,
                                   new DefaultPatternCache(new FakeCache()),
                                   ecProvider);
    }
}
