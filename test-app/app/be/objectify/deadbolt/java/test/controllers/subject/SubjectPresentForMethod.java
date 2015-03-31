package be.objectify.deadbolt.java.test.controllers.subject;

import be.objectify.deadbolt.java.actions.SubjectPresent;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class SubjectPresentForMethod extends Controller
{
    @SubjectPresent
    public static Result subjectMustBePresent()
    {
        return ok("Content accessible");
    }
}
