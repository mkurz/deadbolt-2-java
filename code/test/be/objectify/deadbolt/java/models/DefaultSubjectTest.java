/*
 * Copyright 2013 Steve Chaloner
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

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import be.objectify.deadbolt.java.models.DefaultSubject;
import be.objectify.deadbolt.java.models.Permission;
import be.objectify.deadbolt.java.models.Role;
import be.objectify.deadbolt.java.models.Subject;

/**
 * @author Matthias Kurz (m.kurz@irregular.at)
 */
public class DefaultSubjectTest
{
    private final static List<String> ROLES = Arrays.asList("admin", "support");
    private final static List<String> PERMISSIONS = Arrays.asList("createuser", "dropuser", "createdocument", "dropdocument");

    private Subject testSubject;

    @Before
    public void setUp()
    {
        this.testSubject = new DefaultSubject("9876", ROLES, PERMISSIONS);
    }

    @Test
    public void testDefaultSubjectInstance()
    {
        Assert.assertTrue(this.testSubject instanceof DefaultSubject);
        Assert.assertTrue(this.testSubject instanceof Serializable);
        Assert.assertTrue(this.testSubject.getIdentifier().equals("9876"));
    }

    @Test
    public void testRoleInstances()
    {
        Assert.assertTrue(this.testSubject.getRoles().size() == 2);
        final List<String> rolesFromSubject = this.testSubject.getRoles().stream().peek(role -> Assert.assertTrue(role instanceof Role && role instanceof Serializable))
            .map(role -> role.getName()).collect(Collectors.toList());
        ROLES.stream().forEach(role -> Assert.assertTrue(rolesFromSubject.contains(role)));
    }

    @Test
    public void testPermissionsInstances()
    {
        Assert.assertTrue(this.testSubject.getPermissions().size() == 4);
        final List<String> permissionsFromSubject = this.testSubject.getPermissions().stream().peek(permission -> Assert.assertTrue(permission instanceof Permission && permission instanceof Serializable))
            .map(permission -> permission.getValue()).collect(Collectors.toList());
        PERMISSIONS.stream().forEach(permission -> Assert.assertTrue(permissionsFromSubject.contains(permission)));
    }

}
