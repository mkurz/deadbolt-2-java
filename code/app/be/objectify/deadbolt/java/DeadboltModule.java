package be.objectify.deadbolt.java;

import be.objectify.deadbolt.java.cache.CompositeCache;
import be.objectify.deadbolt.java.cache.DefaultCompositeCache;
import be.objectify.deadbolt.java.cache.DefaultPatternCache;
import be.objectify.deadbolt.java.cache.DefaultSubjectCache;
import be.objectify.deadbolt.java.cache.PatternCache;
import be.objectify.deadbolt.java.cache.SubjectCache;
import be.objectify.deadbolt.java.composite.ConstraintBuilders;
import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
import play.api.inject.Module;
import scala.collection.Seq;

import javax.inject.Singleton;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class DeadboltModule extends Module
{
    @Override
    public Seq<Binding<?>> bindings(final Environment environment,
                                    final Configuration configuration)
    {
        return seq(subjectCache(),
                   patternCache(),
                   analyzer(),
                   viewSupport(),
                   templateFailureListenerProvider(),
                   executionContextProvider(),
                   compositeCache(),
                   constraintBuilders());
    }

    /**
     * Create a binding for {@link TemplateFailureListenerProvider}.
     *
     * @return the binding
     */
    public Binding<TemplateFailureListenerProvider> templateFailureListenerProvider()
    {
        return bind(TemplateFailureListenerProvider.class).toSelf().in(Singleton.class);
    }

    /**
     * Create a binding for {@link ExecutionContextProvider}.
     *
     * @return the binding
     */
    public Binding<ExecutionContextProvider> executionContextProvider()
    {
        return bind(ExecutionContextProvider.class).toSelf().in(Singleton.class);
    }

    /**
     * Create a binding for {@link ViewSupport}.
     *
     * @return the binding
     */
    public Binding<ViewSupport> viewSupport()
    {
        return bind(ViewSupport.class).toSelf().in(Singleton.class);
    }

    /**
     * Create a binding for {@link DeadboltAnalyzer}.
     *
     * @return the binding
     */
    public Binding<DeadboltAnalyzer> analyzer()
    {
        return bind(DeadboltAnalyzer.class).toSelf().in(Singleton.class);
    }

    /**
     * Create a binding for {@link ConstraintBuilders}.
     *
     * @return the binding
     */
    public Binding<ConstraintBuilders> constraintBuilders()
    {
        return bind(ConstraintBuilders.class).toSelf().in(Singleton.class);
    }

    /**
     * Create a binding for {@link PatternCache}.
     *
     * @return the binding
     */
    public Binding<PatternCache> patternCache()
    {
        return bind(PatternCache.class).to(DefaultPatternCache.class).in(Singleton.class);
    }

    /**
     * Create a binding for {@link CompositeCache}.
     *
     * @return the binding
     */
    public Binding<CompositeCache> compositeCache()
    {
        return bind(CompositeCache.class).to(DefaultCompositeCache.class).in(Singleton.class);
    }

    /**
     * Create a binding for {@link SubjectCache}.
     *
     * @return the binding
     */
    public Binding<SubjectCache> subjectCache()
    {
        return bind(SubjectCache.class).to(DefaultSubjectCache.class).in(Singleton.class);
    }
}
