package be.objectify.deadbolt.java.test.modules;

import be.objectify.deadbolt.java.filters.AuthorizedRoutes;
import be.objectify.deadbolt.java.test.security.MyAuthorizedRoutes;
import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
import play.api.inject.Module;
import scala.collection.Seq;

import javax.inject.Singleton;

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
