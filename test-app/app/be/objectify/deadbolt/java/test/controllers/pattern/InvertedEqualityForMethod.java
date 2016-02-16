package be.objectify.deadbolt.java.test.controllers.pattern;

import be.objectify.deadbolt.java.models.PatternType;
import be.objectify.deadbolt.java.actions.Pattern;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class InvertedEqualityForMethod extends Controller
{
    @Pattern(value = "killer.undead.zombie", patternType = PatternType.EQUALITY, invert = true)
    public CompletionStage<Result> zombieKillersOnly()
    {
        return CompletableFuture.supplyAsync(() -> ok("Content accessible"));
    }
}
