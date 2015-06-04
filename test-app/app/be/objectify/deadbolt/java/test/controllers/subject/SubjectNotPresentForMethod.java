package be.objectify.deadbolt.java.test.controllers.subject;

import be.objectify.deadbolt.java.actions.SubjectNotPresent;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class SubjectNotPresentForMethod extends Controller
{
    @SubjectNotPresent
    public static F.Promise<Result> subjectMustNotBePresent()
    {
        return F.Promise.promise(() -> ok("Content accessible"));
    }
}
