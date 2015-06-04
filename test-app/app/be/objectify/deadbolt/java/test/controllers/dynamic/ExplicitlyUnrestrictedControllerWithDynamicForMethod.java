package be.objectify.deadbolt.java.test.controllers.dynamic;

import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.Unrestricted;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@Unrestricted
public class ExplicitlyUnrestrictedControllerWithDynamicForMethod extends Controller
{
    @Dynamic("niceName")
    public static F.Promise<Result> userMustHaveTheSameNameAsMyWife()
    {
        return F.Promise.promise(() -> ok("Content accessible"));
    }
}
