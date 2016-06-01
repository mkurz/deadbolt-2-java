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

import java.util.Optional;

/**
 * HTTP methods used when defining {@link AuthorizedRoute}s.
 * <p>
 * To apply a constraint to all methods for a given path, use ANY.
 *
 * @author Steve Chaloner (steve@objectify.be)
 * @since 2.5.1
 */
public final class Methods
{
    private Methods()
    {
        // no-op
    }

    public static final Optional<String> ANY = Optional.empty();
    public static final Optional<String> GET = Optional.of("GET");
    public static final Optional<String> POST = Optional.of("POST");
    public static final Optional<String> DELETE = Optional.of("DELETE");
    public static final Optional<String> PUT = Optional.of("PUT");
    public static final Optional<String> PATCH = Optional.of("PATCH");
    public static final Optional<String> OPTIONS = Optional.of("OPTIONS");
    public static final Optional<String> HEAD = Optional.of("HEAD");
}
