package be.objectify.deadbolt.java.test.controllers.subject;

import be.objectify.deadbolt.java.actions.SubjectNotPresent;
import be.objectify.deadbolt.java.actions.Unrestricted;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@SubjectNotPresent
public class SubjectNotPresentForController extends Controller
{
    public CompletionStage<Result> subjectMustNotBePresent()
    {
        return CompletableFuture.supplyAsync(() -> ok("Content accessible"));
    }

    @Unrestricted
    public CompletionStage<Result> unrestricted()
    {
        return CompletableFuture.supplyAsync(() -> ok("Content accessible"));
    }
}
