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

import be.objectify.deadbolt.java.models.Permission;
import be.objectify.deadbolt.java.models.Role;
import be.objectify.deadbolt.java.models.Subject;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This carries out static (i.e. non-dynamic) checks.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
@Singleton
public class DeadboltAnalyzer
{
    /**
     * Checks if the subject has all the role names.  In other words, this gives AND support.
     *
     * @param subjectOption an option for the subject
     * @param roleNames  the role names.  Any role name starting with ! will be negated.
     * @return true if the subject meets the restrictions (so access will be allowed), otherwise false
     */
    public boolean checkRole(final Optional<? extends Subject> subjectOption,
                             final String[] roleNames)
    {
        // this is legacy code, and can be refactored out at some point
        return hasAllRoles(subjectOption,
                           roleNames);
    }


    /**
     * Gets the role name of each role held.
     *
     * @param subjectOption an option for the subject
     * @return a non-null list containing all role names
     */
    public List<String> getRoleNames(final Optional<? extends Subject> subjectOption)
    {
        final List<String> roleNames = new ArrayList<>();
        subjectOption.ifPresent(subject -> {
            final List<? extends Role> roles = subject.getRoles();
            if (roles != null)
            {
                roleNames.addAll(roles.stream()
                                      .filter(role -> role != null)
                                      .map(Role::getName)
                                      .collect(Collectors.toList()));
            }
        });

        return roleNames;
    }

    /**
     * Check if the subject has the given role.
     *
     * @param subjectOption an option for the subject
     * @param roleName the name of the role
     * @return true iff the subject has the role represented by the role name
     */
    public boolean hasRole(final Optional<? extends Subject> subjectOption,
                           final String roleName)
    {
        return getRoleNames(subjectOption).contains(roleName);
    }

    /**
     * Check if the {@link Subject} has all the roles given in the roleNames array.  Note that while a Subject must
     * have all the roles, it may also have other roles.
     *
     * @param subjectOption an option for the subject
     * @param roleNames the names of the required roles
     * @return true iff the subject has all the roles
     */
    public boolean hasAllRoles(final Optional<? extends Subject> subjectOption,
                               final String[] roleNames)
    {
        final List<String> heldRoles = getRoleNames(subjectOption);

        boolean roleCheckResult = roleNames != null && roleNames.length > 0;
        for (int i = 0; roleCheckResult && i < roleNames.length; i++)
        {
            boolean invert = false;
            String roleName = roleNames[i];
            if (roleName.startsWith("!"))
            {
                invert = true;
                roleName = roleName.substring(1);
            }
            roleCheckResult = heldRoles.contains(roleName);

            if (invert)
            {
                roleCheckResult = !roleCheckResult;
            }
        }
        return roleCheckResult;
    }

    /**
     * Check the pattern for a match against the {@link Permission}s of the user.
     *
     * @param subjectOption an option for the subject
     * @param patternOption an option for the pattern
     * @return true iff the pattern matches at least one of the subject's permissions
     */
    public boolean checkRegexPattern(final Optional<? extends Subject> subjectOption,
                                     final Optional<Pattern> patternOption)
    {
        final boolean[] roleOk = {false};
        subjectOption.ifPresent(subject -> patternOption.ifPresent(pattern -> {
            final List<? extends Permission> permissions = subject.getPermissions();
            if (permissions != null)
            {
                for (Iterator<? extends Permission> iterator = permissions.iterator(); !roleOk[0] && iterator.hasNext(); )
                {
                    final Permission permission = iterator.next();
                    roleOk[0] = pattern.matcher(permission.getValue()).matches();
                }
            }
        }));

        return roleOk[0];
    }

    /**
     * Check the pattern for equality against the {@link Permission}s of the user.
     *
     * @param subjectOption an option for the subject
     * @param patternValueOption an option for the pattern value
     * @return true iff the pattern is equal to at least one of the subject's permissions
     */
    public boolean checkPatternEquality(final Optional<? extends Subject> subjectOption,
                                        final Optional<String> patternValueOption)
    {
        final boolean[] roleOk = {false};
        subjectOption.ifPresent(subject -> patternValueOption.ifPresent(patternValue -> {
            final List<? extends Permission> permissions = subject.getPermissions();
            if (permissions != null)
            {
                for (Iterator<? extends Permission> iterator = permissions.iterator(); !roleOk[0] && iterator.hasNext(); )
                {
                    final Permission permission = iterator.next();
                    roleOk[0] = patternValue.equals(permission.getValue());
                }
            }
        }));

        return roleOk[0];
    }
}
