package be.objectify.deadbolt.java.test.controllers.subject;

import be.objectify.deadbolt.java.actions.SubjectPresent;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class SubjectPresentForMethod extends Controller
{
    @SubjectPresent
    public static F.Promise<Result> subjectMustBePresent()
    {
        return F.Promise.promise(() -> ok("Content accessible"));
    }
}
