package be.objectify.deadbolt.java.test.controllers.pattern;

import be.objectify.deadbolt.core.PatternType;
import be.objectify.deadbolt.java.actions.Pattern;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class InvertedEqualityForMethod extends Controller
{
    @Pattern(value = "killer.undead.zombie", patternType = PatternType.EQUALITY, invert = true)
    public static F.Promise<Result> zombieKillersOnly()
    {
        return F.Promise.promise(() -> ok("Content accessible"));
    }
}
