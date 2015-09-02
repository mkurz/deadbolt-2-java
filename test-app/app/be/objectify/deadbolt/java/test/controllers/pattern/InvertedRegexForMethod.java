package be.objectify.deadbolt.java.test.controllers.pattern;

import be.objectify.deadbolt.core.PatternType;
import be.objectify.deadbolt.java.actions.Pattern;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class InvertedRegexForMethod extends Controller
{
    @Pattern(value = "killer.undead.zombie", patternType = PatternType.REGEX, invert = true)
    public static F.Promise<Result> zombieKillersOnly()
    {
        return F.Promise.promise(() -> ok("Content accessible"));
    }

    @Pattern(value = "killer.undead.*", patternType = PatternType.REGEX, invert = true)
    public static F.Promise<Result> anyKillersOfTheUndeadWelcome()
    {
        return F.Promise.promise(() -> ok("Content accessible"));
    }
}
