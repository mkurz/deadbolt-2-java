package be.objectify.deadbolt.java.testsupport;

import be.objectify.deadbolt.core.models.Permission;
import be.objectify.deadbolt.core.models.Role;
import be.objectify.deadbolt.core.models.Subject;

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
        private final List<Role> roles = new LinkedList<Role>();
        private final List<Permission> permissions = new LinkedList<Permission>();
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