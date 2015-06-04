package be.objectify.deadbolt.java.test.controllers.subject;

import be.objectify.deadbolt.java.actions.SubjectPresent;
import be.objectify.deadbolt.java.actions.Unrestricted;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@SubjectPresent
public class SubjectPresentForController extends Controller
{
    public static F.Promise<Result> subjectMustBePresent()
    {
        return F.Promise.promise(() -> ok("Content accessible"));
    }

    @Unrestricted
    public static F.Promise<Result> unrestricted()
    {
        return F.Promise.promise(() -> ok("Content accessible"));
    }
}
