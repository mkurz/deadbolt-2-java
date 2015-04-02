package be.objectify.deadbolt.java.views;

import be.objectify.deadbolt.core.models.Permission;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class TestPermission implements Permission
{
    private final String value;

    public TestPermission(final String value)
    {
        this.value = value;
    }

    @Override
    public String getValue()
    {
        return value;
    }
}