package be.objectify.deadbolt.java.test.controllers.comment;

import be.objectify.deadbolt.java.test.controllers.SubjectNotPresentTest;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class CommentSubjectNotPresentTest extends SubjectNotPresentTest
{
    @Override
    public String pathComponent()
    {
        return "c";
    }
}
