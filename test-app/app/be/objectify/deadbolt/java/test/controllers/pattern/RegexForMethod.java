package be.objectify.deadbolt.java.test.controllers.pattern;

import be.objectify.deadbolt.core.PatternType;
import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.Pattern;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class RegexForMethod extends Controller
{
    @Pattern(value = "killer.undead.zombie", patternType = PatternType.REGEX)
    public static Result zombieKillersOnly()
    {
        return ok("Content accessible");
    }

    @Pattern(value = "killer.undead.*", patternType = PatternType.REGEX)
    public static Result anyKillersOfTheUndeadWelcome()
    {
        return ok("Content accessible");
    }
}
