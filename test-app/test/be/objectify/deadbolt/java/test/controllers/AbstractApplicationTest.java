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

import java.util.Map;

import be.objectify.deadbolt.java.test.dao.TestUserDao;
import be.objectify.deadbolt.java.test.dao.UserDao;
import play.Application;
import play.Mode;
import play.inject.Bindings;
import play.inject.guice.GuiceApplicationBuilder;
import com.google.common.collect.ImmutableMap;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public abstract class AbstractApplicationTest
{
    public Application app(final Map<String, Object> conf)
    {
        return new GuiceApplicationBuilder().in(Mode.TEST)
                                            .overrides(Bindings.bind(UserDao.class).to(TestUserDao.class))
                                            .configure(conf)
                                            .build();
    }

    public Application app(final String key, final Object value)
    {
        if(key != null && value != null) {
            return app(ImmutableMap.of(key, value));
        }
        return app();
    }

    public Application app()
    {
        return app(ImmutableMap.of());
    }
}
