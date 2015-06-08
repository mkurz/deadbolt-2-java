package be.objectify.deadbolt.java;

import be.objectify.deadbolt.java.cache.DefaultHandlerCache;
import be.objectify.deadbolt.java.cache.DefaultPatternCache;
import be.objectify.deadbolt.java.cache.DefaultSubjectCache;
import be.objectify.deadbolt.java.cache.HandlerCache;
import be.objectify.deadbolt.java.cache.PatternCache;
import be.objectify.deadbolt.java.cache.SubjectCache;
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
        return seq(bind(SubjectCache.class).to(DefaultSubjectCache.class).in(Singleton.class),
                   bind(PatternCache.class).to(DefaultPatternCache.class).in(Singleton.class),
                   bind(HandlerCache.class).to(DefaultHandlerCache.class).in(Singleton.class),
                   bind(JavaAnalyzer.class).toSelf().in(Singleton.class),
                   bind(ViewSupport.class).toSelf());
    }
}
