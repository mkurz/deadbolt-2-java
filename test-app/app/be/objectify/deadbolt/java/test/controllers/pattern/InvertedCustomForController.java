package be.objectify.deadbolt.java.test.controllers.pattern;

import be.objectify.deadbolt.core.PatternType;
import be.objectify.deadbolt.java.actions.Pattern;
import be.objectify.deadbolt.java.actions.Unrestricted;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@Pattern(value = "shoot-the-brain", patternType = PatternType.CUSTOM, invert = true)
public class InvertedCustomForController extends Controller
{
    public static F.Promise<Result> protectedByControllerLevelCustom()
    {
        return F.Promise.promise(() -> ok("Content accessible"));
    }

    @Unrestricted
    public static F.Promise<Result> unrestricted()
    {
        return F.Promise.promise(() -> ok("Content accessible"));
    }
}
