package be.objectify.deadbolt.java.test.modules;

import javax.inject.Singleton;
import be.objectify.deadbolt.java.DeadboltExecutionContextProvider;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.DefaultDeadboltExecutionContextProvider;
import be.objectify.deadbolt.java.TemplateFailureListener;
import be.objectify.deadbolt.java.cache.HandlerCache;
import be.objectify.deadbolt.java.filters.AuthorizedRoutes;
import be.objectify.deadbolt.java.test.security.CompositeConstraints;
import be.objectify.deadbolt.java.test.security.HandlerQualifiers;
import be.objectify.deadbolt.java.test.security.MyAuthorizedRoutes;
import be.objectify.deadbolt.java.test.security.MyCustomTemplateFailureListener;
import be.objectify.deadbolt.java.test.security.MyDeadboltHandler;
import be.objectify.deadbolt.java.test.security.MyHandlerCache;
import be.objectify.deadbolt.java.test.security.SomeOtherDeadboltHandler;
import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
import play.api.inject.Module;
import scala.collection.Seq;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class CustomDeadboltFilterHook extends Module
{
    @Override
    public Seq<Binding<?>> bindings(final Environment environment,
                                    final Configuration configuration)
    {
        return seq(bind(AuthorizedRoutes.class).to(MyAuthorizedRoutes.class).in(Singleton.class));
    }
}
