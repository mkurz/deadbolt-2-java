package be.objectify.deadbolt.java.test.controllers.comment;

import be.objectify.deadbolt.java.test.controllers.RestrictTest;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class CommentRestrictTest extends RestrictTest
{
    @Override
    public String pathComponent()
    {
        return "c";
    }
}
