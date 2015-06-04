package be.objectify.deadbolt.java.test.controllers.dynamic;

import be.objectify.deadbolt.java.actions.Dynamic;
import be.objectify.deadbolt.java.actions.Unrestricted;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@Dynamic("niceName")
public class DynamicForController extends Controller
{
    public static F.Promise<Result> protectedByControllerLevelDynamic()
    {
        return F.Promise.promise(() -> ok("Content accessible"));
    }

    @Unrestricted
    public static F.Promise<Result> unrestricted()
    {
        return F.Promise.promise(() -> ok("Content accessible"));
    }
}
