/*
 * Copyright 2012 Steve Chaloner
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

import java.util.List;

/**
 * A Subject represents an authorisable entity, typically a user, that will try to access the application.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public interface Subject
{
    /**
     * Get all {@link Role}s held by this subject.  Ordering is not important.
     *
     * @return a non-null list of roles
     */
    List<? extends Role> getRoles();

    /**
     * Get all {@link Permission}s held by this subject.  Ordering is not important.
     *
     * @return a non-null list of permissions
     */
    List<? extends Permission> getPermissions();

    /**
     * Gets a unique identifier for the subject, such as a user name.  This is never used by Deadbolt itself,
     * and is present to provide an easy way of getting a useful piece of user information in, for example,
     * dynamic checks without the need to cast the Subject.
     *
     * @return an identifier, such as a user name or UUID.  May be null.
     */
    String getIdentifier();
}
