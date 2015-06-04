package be.objectify.deadbolt.java.test.controllers.subject;

import be.objectify.deadbolt.java.actions.SubjectNotPresent;
import be.objectify.deadbolt.java.actions.Unrestricted;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@Unrestricted
public class ExplicitlyUnrestrictedControllerWithSubjectNotPresentForMethod extends Controller
{
    @SubjectNotPresent
    public static F.Promise<Result> subjectMustNotBePresent()
    {
        return F.Promise.promise(() -> ok("Content accessible"));
    }

}
