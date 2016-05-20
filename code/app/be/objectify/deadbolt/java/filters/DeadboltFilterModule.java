package be.objectify.deadbolt.java.filters;

import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
import play.api.inject.Module;
import scala.collection.Seq;

/**
 * Provides bindings for {@link DeadboltFilter} and {@link FilterConstraints}.
 *
 * @since 2.5.1
 * @author Steve Chaloner (steve@objectify.be)
 */
public class DeadboltFilterModule extends Module
{
    @Override
    public Seq<Binding<?>> bindings(final Environment environment,
                                    final Configuration configuration)
    {
        return seq(bind(DeadboltFilter.class).toSelf(),
                   bind(FilterConstraints.class).toSelf());
    }
}
