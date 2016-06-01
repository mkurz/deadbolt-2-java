package be.objectify.deadbolt.java.test.controllers.comment;

import be.objectify.deadbolt.java.test.controllers.PatternRegexTest;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class CommentPatternRegexTest extends PatternRegexTest
{
    @Override
    public String pathComponent()
    {
        return "c";
    }
}
