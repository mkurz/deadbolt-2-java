package be.objectify.deadbolt.java.test.controllers.pattern;

import be.objectify.deadbolt.core.PatternType;
import be.objectify.deadbolt.java.actions.Pattern;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class CustomForMethod extends Controller
{
    @Pattern(value = "i-do-not-like-ice-cream", patternType = PatternType.CUSTOM)
    public static F.Promise<Result> accessDependsOnTheCustomTest()
    {
        return F.Promise.promise(() -> ok("Content accessible"));
    }
}
