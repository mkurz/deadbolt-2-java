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
public class InvertedCustomForMethod extends Controller
{
    @Pattern(value = "i-do-not-like-ice-cream", patternType = PatternType.CUSTOM, invert = true)
    public CompletionStage<Result> accessDependsOnTheCustomTest()
    {
        return CompletableFuture.supplyAsync(() -> ok("Content accessible"));
    }
}
