package be.objectify.deadbolt.java.test.controllers.path;

import be.objectify.deadbolt.java.test.controllers.SubjectNotPresentTest;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class RoutePathSubjectNotPresentTest extends SubjectNotPresentTest
{
    @Override
    public String pathComponent()
    {
        return "rp";
    }
}
