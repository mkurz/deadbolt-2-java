package be.objectify.deadbolt.java.test.controllers.subject;

import be.objectify.deadbolt.java.actions.SubjectNotPresent;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class SubjectNotPresentForMethod extends Controller
{
    @SubjectNotPresent
    public static Result subjectMustNotBePresent()
    {
        return ok("Content accessible");
    }
}
