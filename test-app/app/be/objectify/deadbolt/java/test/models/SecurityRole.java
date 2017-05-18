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
package be.objectify.deadbolt.java.test.models;

import java.util.Objects;
import be.objectify.deadbolt.java.models.Role;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class SecurityRole implements Role
{
    public final String roleName;

    public SecurityRole(final String roleName)
    {
        this.roleName = roleName;
    }

    @Override
    public String getName()
    {
        return roleName;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final SecurityRole that = (SecurityRole) o;
        return Objects.equals(roleName,
                              that.roleName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleName);
    }
}
