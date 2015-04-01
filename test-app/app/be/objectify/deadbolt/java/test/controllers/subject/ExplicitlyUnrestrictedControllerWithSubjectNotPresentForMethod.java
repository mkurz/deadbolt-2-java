package be.objectify.deadbolt.java.test.controllers.subject;

import be.objectify.deadbolt.java.actions.SubjectNotPresent;
import be.objectify.deadbolt.java.actions.Unrestricted;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@Unrestricted
public class ExplicitlyUnrestrictedControllerWithSubjectNotPresentForMethod extends Controller
{
    @SubjectNotPresent
    public static Result subjectMustNotBePresent()
    {
        return ok("Content accessible");
    }

}
