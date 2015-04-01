package be.objectify.deadbolt.java.test.controllers.dynamic;

import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.SubjectPresent;
import be.objectify.deadbolt.java.actions.Unrestricted;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class DynamicForMethod extends Controller
{
    @Dynamic("niceName")
    public static Result userMustHaveTheSameNameAsMyWife()
    {
        return ok("Content accessible");
    }
}
