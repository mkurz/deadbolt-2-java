package be.objectify.deadbolt.java.test.controllers.restrict;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.Unrestricted;
import play.mvc.Controller;
import play.mvc.Result;

@Restrict(@Group({"foo", "bar"}))
public class RestrictedToFooAndBar extends Controller
{
    public static Result index()
    {
        return ok("Content accessible");
    }

    @Unrestricted
    public static Result unrestricted()
    {
        return ok("Content accessible");
    }
}
