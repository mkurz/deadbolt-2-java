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
package be.objectify.deadbolt.java;

import play.libs.F;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class ConfigKeys
{
    public static final String DEFAULT_HANDLER_KEY = "defaultHandler";
    public static final String CACHE_DEADBOLT_USER = "deadbolt.java.cache-user";
    public static final F.Tuple<String, Boolean> CACHE_DEADBOLT_USER_DEFAULT = new F.Tuple<>(CACHE_DEADBOLT_USER,
                                                                                             false);
    public static final String DEFAULT_VIEW_TIMEOUT = "deadbolt.java.view-timeout";
    public static final F.Tuple<String, Long> DEFAULT_VIEW_TIMEOUT_DEFAULT = new F.Tuple<>(DEFAULT_VIEW_TIMEOUT,
                                                                                           1000L);
    public static final String BLOCKING = "deadbolt.java.blocking";
    public static final F.Tuple<String, Boolean> BLOCKING_DEFAULT = new F.Tuple<>(BLOCKING,
                                                                                  false);
    public static final String DEFAULT_BLOCKING_TIMEOUT = "deadbolt.java.blocking-timeout";
    public static final F.Tuple<String, Long> DEFAULT_BLOCKING_TIMEOUT_DEFAULT = new F.Tuple<>(DEFAULT_BLOCKING_TIMEOUT,
                                                                                               1000L);
    public static final String CUSTOM_EC = "deadbolt.java.custom-execution-context.enable";
    public static final F.Tuple<String, Boolean> CUSTOM_EC_DEFAULT = new F.Tuple<>(CUSTOM_EC,
                                                                                   false);
    public static final String CONSTRAINT_MODE = "deadbolt.java.constraint-mode";
    public static final F.Tuple<String, String> CONSTRAINT_MODE_DEFAULT = new F.Tuple<>(CONSTRAINT_MODE,
            ConstraintMode.PROCESS_FIRST_CONSTRAINT_ONLY.toString());

    public static final String PATTERN_INVERT = "deadbolt.pattern.invert";

    private ConfigKeys()
    {
        // no-op
    }
}
