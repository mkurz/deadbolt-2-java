package be.objectify.deadbolt.java.test.controllers.subject;

import be.objectify.deadbolt.java.actions.SubjectNotPresent;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class SubjectNotPresentForMethod extends Controller
{
    @SubjectNotPresent
    public CompletionStage<Result> subjectMustNotBePresent()
    {
        return CompletableFuture.supplyAsync(() -> ok("Content accessible"));
    }
}
