package be.objectify.deadbolt.java.test.modules;

import be.objectify.deadbolt.java.DeadboltExecutionContextProvider;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.DefaultDeadboltExecutionContextProvider;
import be.objectify.deadbolt.java.TemplateFailureListener;
import be.objectify.deadbolt.java.cache.HandlerCache;
import be.objectify.deadbolt.java.test.security.CompositeConstraints;
import be.objectify.deadbolt.java.test.security.MainHandler;
import be.objectify.deadbolt.java.test.security.MyCustomTemplateFailureListener;
import be.objectify.deadbolt.java.test.security.MyDeadboltHandler;
import be.objectify.deadbolt.java.test.security.MyHandlerCache;
import be.objectify.deadbolt.java.test.security.SomeOtherDeadboltHandler;
import be.objectify.deadbolt.java.test.security.SomeOtherHandler;
import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
import play.api.inject.Module;
import scala.collection.Seq;

import javax.inject.Singleton;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class CustomDeadboltHook extends Module
{
    @Override
    public Seq<Binding<?>> bindings(final Environment environment,
                                    final Configuration configuration)
    {
        return seq(bind(TemplateFailureListener.class).to(MyCustomTemplateFailureListener.class).in(Singleton.class),
                   // it's not necessary to make this execution context provider binding, this is just for testing
                   bind(DeadboltExecutionContextProvider.class).to(DefaultDeadboltExecutionContextProvider.class).in(Singleton.class),
                   bind(DeadboltHandler.class).qualifiedWith(MainHandler.class).to(MyDeadboltHandler.class).in(Singleton.class),
                   bind(DeadboltHandler.class).qualifiedWith(SomeOtherHandler.class).to(SomeOtherDeadboltHandler.class).in(Singleton.class),
                   bind(HandlerCache.class).to(MyHandlerCache.class).in(Singleton.class),
                   bind(CompositeConstraints.class).toSelf().eagerly());
    }
}
