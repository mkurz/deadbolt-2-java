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
package be.objectify.deadbolt.java.filters;

import com.typesafe.config.Config;
import play.Environment;
import play.inject.Binding;
import play.inject.Module;

import java.util.Arrays;
import java.util.List;

/**
 * Provides bindings for {@link DeadboltRoutePathFilter} and {@link FilterConstraints}.
 *
 * @author Steve Chaloner (steve@objectify.be)
 * @since 2.5.1
 */
public class DeadboltRoutePathFilterModule extends Module
{
    @Override
    public List<Binding<?>> bindings(final Environment environment,
                                     final Config config)
    {
        return Arrays.asList(bindClass(DeadboltRoutePathFilter.class).toSelf());
    }
}
