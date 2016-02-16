package be.objectify.deadbolt.java.testsupport;

import be.objectify.deadbolt.java.models.Role;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class TestRole implements Role
{
    private final String name;

    public TestRole(final String name)
    {
        this.name = name;
    }

    @Override
    public String getName()
    {
        return name;
    }
}