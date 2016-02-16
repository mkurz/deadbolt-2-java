package be.objectify.deadbolt.java.test.controllers.pattern;

import be.objectify.deadbolt.java.models.PatternType;
import be.objectify.deadbolt.java.actions.Pattern;
import be.objectify.deadbolt.java.actions.Unrestricted;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@Pattern(value = "killer.undead.zombie", patternType = PatternType.EQUALITY, invert = true)
public class InvertedEqualityForController extends Controller
{
    public CompletionStage<Result> protectedByControllerLevelEquality()
    {
        return CompletableFuture.supplyAsync(() -> ok("Content accessible"));
    }

    @Unrestricted
    public CompletionStage<Result> unrestricted()
    {
        return CompletableFuture.supplyAsync(() -> ok("Content accessible"));
    }
}
