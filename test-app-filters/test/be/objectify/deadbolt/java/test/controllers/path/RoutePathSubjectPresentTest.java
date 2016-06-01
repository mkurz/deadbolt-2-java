package be.objectify.deadbolt.java.test.controllers.path;

import be.objectify.deadbolt.java.test.controllers.SubjectPresentTest;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class RoutePathSubjectPresentTest extends SubjectPresentTest
{
    @Override
    public String pathComponent()
    {
        return "rp";
    }
}
