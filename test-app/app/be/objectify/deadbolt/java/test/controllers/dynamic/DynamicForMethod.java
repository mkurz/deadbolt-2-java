package be.objectify.deadbolt.java.test.controllers.dynamic;

import be.objectify.deadbolt.java.actions.Dynamic;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class DynamicForMethod extends Controller
{
    @Dynamic("niceName")
    public static F.Promise<Result> userMustHaveTheSameNameAsMyWife()
    {
        return F.Promise.promise(() -> ok("Content accessible"));
    }
}
