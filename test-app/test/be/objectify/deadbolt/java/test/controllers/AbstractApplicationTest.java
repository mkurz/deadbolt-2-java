/*
 * Copyright 2010-2016 Steve Chaloner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.objectify.deadbolt.java.test.controllers;

import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.test.security.HandlerQualifiers;
import play.Application;
import play.Mode;
import play.api.inject.Module;
import play.inject.Bindings;
import play.inject.guice.GuiceApplicationBuilder;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public abstract class AbstractApplicationTest
{

    public Application app()
    {
        return new GuiceApplicationBuilder().bindings(new DataLoaderModule())
                                            .in(Mode.TEST)
                                            .build();
    }

    public Application app(final Module... modules)
    {
        return new GuiceApplicationBuilder().bindings(modules)
                                            .in(Mode.TEST)
                                            .build();
    }

    public Application app(final Class<? extends DeadboltHandler> handlerClass,
                           final Module... modules)
    {
        return new GuiceApplicationBuilder().bindings(modules)
                                            .overrides(Bindings.bind(DeadboltHandler.class).qualifiedWith(HandlerQualifiers.MainHandler.class).to(handlerClass))
                                            .in(Mode.TEST)
                                            .build();
    }

    public Application app(final Class<? extends DeadboltHandler> handlerClass)
    {
        return new GuiceApplicationBuilder().overrides(Bindings.bind(DeadboltHandler.class).qualifiedWith(HandlerQualifiers.MainHandler.class).to(handlerClass))
                                            .in(Mode.TEST)
                                            .build();
    }
}
