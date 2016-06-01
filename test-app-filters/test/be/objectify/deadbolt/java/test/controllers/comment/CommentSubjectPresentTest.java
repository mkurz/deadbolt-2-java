package be.objectify.deadbolt.java.test.controllers.comment;

import be.objectify.deadbolt.java.test.controllers.SubjectPresentTest;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class CommentSubjectPresentTest extends SubjectPresentTest
{
    @Override
    public String pathComponent()
    {
        return "c";
    }
}
