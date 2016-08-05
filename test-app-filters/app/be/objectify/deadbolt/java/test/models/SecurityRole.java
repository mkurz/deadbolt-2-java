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
package be.objectify.deadbolt.java.test.models;

import be.objectify.deadbolt.java.models.Role;
import com.avaje.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@Entity
public class SecurityRole extends Model implements Role
{
    @Id
    public Long id;

    @Column(nullable = false,
            unique = true)
    public String roleName;

    public SecurityRole()
    {
        // no-op
    }

    private SecurityRole(Builder builder)
    {
        roleName = builder.roleName;
    }

    @Override
    public String getName()
    {
        return roleName;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        if (!super.equals(o))
        {
            return false;
        }

        SecurityRole that = (SecurityRole) o;

        return !(roleName != null ? !roleName.equals(that.roleName) : that.roleName != null);

    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (roleName != null ? roleName.hashCode() : 0);
        return result;
    }

    public static final class Builder
    {
        private String roleName;

        public Builder roleName(String roleName)
        {
            this.roleName = roleName;
            return this;
        }

        public SecurityRole build()
        {
            return new SecurityRole(this);
        }
    }
}
