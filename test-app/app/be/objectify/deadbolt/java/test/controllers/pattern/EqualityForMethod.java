package be.objectify.deadbolt.java.test.controllers.pattern;

import be.objectify.deadbolt.core.PatternType;
import be.objectify.deadbolt.java.actions.Pattern;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class EqualityForMethod extends Controller
{
    @Pattern(value = "killer.undead.zombie", patternType = PatternType.EQUALITY)
    public CompletionStage<Result> zombieKillersOnly()
    {
        return CompletableFuture.supplyAsync(() -> ok("Content accessible"));
    }
}
