package be.objectify.deadbolt.java.test.controllers;

import play.api.Configuration;
import play.api.Environment;
import play.api.inject.Binding;
import play.api.inject.Module;
import scala.collection.Seq;

public class DataLoaderModule extends Module {

    @Override
    public Seq<Binding<?>> bindings(final Environment environment,
                                    final Configuration configuration) {
        return seq(bind(DataLoader.class).toSelf().eagerly());
    }
}
