package be.objectify.deadbolt.java.test.controllers.pattern;

import be.objectify.deadbolt.core.PatternType;
import be.objectify.deadbolt.java.actions.Pattern;
import be.objectify.deadbolt.java.actions.Unrestricted;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@Pattern(value = "shoot-the-brain", patternType = PatternType.CUSTOM)
public class CustomForController extends Controller
{
    public static Result protectedByControllerLevelCustom()
    {
        return ok("Content accessible");
    }

    @Unrestricted
    public static Result unrestricted()
    {
        return ok("Content accessible");
    }
}
