package be.objectify.deadbolt.java.test.models;

import be.objectify.deadbolt.java.models.Permission;
import com.avaje.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@Entity
public class SecurityPermission extends Model implements Permission
{
    @Id
    public Long id;

    @Column(nullable = false,
            unique = true)
    public String value;

    public SecurityPermission()
    {
        // no-op
    }

    private SecurityPermission(Builder builder)
    {
        value = builder.value;
    }

    @Override
    public String getValue()
    {
        return value;
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

        SecurityPermission that = (SecurityPermission) o;

        return !(value != null ? !value.equals(that.value) : that.value != null);

    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    public static final class Builder
    {
        private String value;

        public Builder value(String value)
        {
            this.value = value;
            return this;
        }

        public SecurityPermission build()
        {
            return new SecurityPermission(this);
        }
    }
}
