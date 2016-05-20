package be.objectify.deadbolt.java.test.controllers;

import play.Application;
import play.Mode;
import play.inject.guice.GuiceApplicationBuilder;

public class AbstractApplicationTest {

    public Application app() {
        return new GuiceApplicationBuilder().bindings(new DataLoaderModule())
//                                            .overrides(bind(CacheApi.class).to(FakeCache.class))
                                            .in(Mode.TEST)
                                            .build();
    }
}
