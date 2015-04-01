package be.objectify.deadbolt.java.test.controllers.subject;

import be.objectify.deadbolt.java.actions.SubjectNotPresent;
import be.objectify.deadbolt.java.actions.Unrestricted;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@SubjectNotPresent
public class SubjectNotPresentForController extends Controller
{
    public static Result subjectMustNotBePresent()
    {
        return ok("Content accessible");
    }

    @Unrestricted
    public static Result unrestricted()
    {
        return ok("Content accessible");
    }
}
