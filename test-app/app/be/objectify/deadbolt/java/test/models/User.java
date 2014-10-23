package be.objectify.deadbolt.java.test.models;

import be.objectify.deadbolt.core.models.Permission;
import be.objectify.deadbolt.core.models.Role;
import be.objectify.deadbolt.core.models.Subject;
import play.db.ebean.Model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import java.util.List;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@Entity
public class User extends Model implements Subject
{
    private static final Finder<String, User> FIND = new Finder<String, User>(String.class,
                                                                              User.class);

    @Id
    public String userName;

    @ManyToMany
    public List<SecurityRole> roles;

    @ManyToMany
    public List<SecurityPermission> permissions;

    public User()
    {
        // no-op
    }

    private User(Builder builder)
    {
        userName = builder.userName;
        roles = builder.roles;
        permissions = builder.permissions;
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
        return userName;
    }

    public static User findByUserName(String userName)
    {
        return FIND.byId(userName);
    }


    public static final class Builder
    {
        private String userName;
        private List<SecurityRole> roles;
        private List<SecurityPermission> permissions;

        public Builder userName(String userName)
        {
            this.userName = userName;
            return this;
        }

        public Builder roles(List<SecurityRole> roles)
        {
            this.roles = roles;
            return this;
        }

        public Builder permissions(List<SecurityPermission> permissions)
        {
            this.permissions = permissions;
            return this;
        }

        public User build()
        {
            return new User(this);
        }
    }
}
