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
public class CustomForMethod extends Controller
{
    @Pattern(value = "i-do-not-like-ice-cream", patternType = PatternType.CUSTOM)
    public CompletionStage<Result> accessDependsOnTheCustomTest()
    {
        return CompletableFuture.supplyAsync(() -> ok("Content accessible"));
    }
}
