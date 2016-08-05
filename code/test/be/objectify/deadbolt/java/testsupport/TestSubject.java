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
package be.objectify.deadbolt.java.testsupport;

import be.objectify.deadbolt.java.models.Permission;
import be.objectify.deadbolt.java.models.Role;
import be.objectify.deadbolt.java.models.Subject;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class TestSubject implements Subject
{
    private final List<? extends Role> roles;
    private final List<? extends Permission> permissions;
    private final String identifier;

    private TestSubject(final Builder builder)
    {
        roles = builder.roles;
        permissions = builder.permissions;
        identifier = builder.identifier;
    }

    @Override
    public List<? extends Role> getRoles()
    {
        return roles;
    }

    @Override
    public List<? extends Permission> getPermissions()
    {
        return permissions;
    }

    @Override
    public String getIdentifier()
    {
        return identifier;
    }

    public static final class Builder
    {
        private final List<Role> roles = new LinkedList<>();
        private final List<Permission> permissions = new LinkedList<>();
        private String identifier;

        public Builder roles(final List<? extends Role> roles)
        {
            this.roles.addAll(roles);
            return this;
        }

        public Builder role(Role role)
        {
            this.roles.add(role);
            return this;
        }

        public Builder permissions(final List<? extends Permission> permissions)
        {
            this.permissions.addAll(permissions);
            return this;
        }

        public Builder permission(final Permission permission)
        {
            this.permissions.add(permission);
            return this;
        }

        public Builder identifier(final String identifier)
        {
            this.identifier = identifier;
            return this;
        }

        public TestSubject build()
        {
            return new TestSubject(this);
        }
    }
}