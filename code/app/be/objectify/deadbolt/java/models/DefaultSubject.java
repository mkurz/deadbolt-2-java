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
package be.objectify.deadbolt.java.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Default implementation of a Subject.
 *
 * @author Matthias Kurz (m.kurz@irregular.at)
 */
public class DefaultSubject implements Subject, Serializable
{

    private static final long serialVersionUID = 4340914517470685171L;

    private final String identifier;
    private final List<? extends Role> roles;
    private final List<? extends Permission> permissions;

    public DefaultSubject(final String identifier, final List<String> roles, final List<String> permissions)
    {
        this.identifier = identifier;
        this.roles = (roles != null) ? Collections.unmodifiableList(roles.stream().<Role>map(role -> () -> role).collect(Collectors.toCollection(() -> new ArrayList<>()))) : null;
        this.permissions = (permissions != null) ? Collections.unmodifiableList(permissions.stream().<Permission>map(permission -> () -> permission).collect(Collectors.toCollection(() -> new ArrayList<>()))) : null;
    }

    @Override
    public String getIdentifier()
    {
        return this.identifier;
    }

    @Override
    public List<? extends Role> getRoles()
    {
        return this.roles;
    }

    @Override
    public List<? extends Permission> getPermissions()
    {
        return this.permissions;
    }
}
