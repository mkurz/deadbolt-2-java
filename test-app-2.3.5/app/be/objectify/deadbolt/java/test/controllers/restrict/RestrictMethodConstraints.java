package be.objectify.deadbolt.java.test.controllers.restrict;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import play.*;
import play.mvc.*;

import views.html.*;

public class RestrictMethodConstraints extends Controller
{
    @Restrict(@Group({"foo", "bar"}))
    public static Result restrictedToFooAndBar()
    {
        return ok("Content accessible");
    }

    @Restrict({@Group("foo"), @Group("bar")})
    public static Result restrictedToFooOrBar()
    {
        return ok("Content accessible");
    }

    @Restrict(@Group({"foo", "!bar"}))
    public static Result restrictedToFooAndNotBar()
    {
        return ok("Content accessible");
    }

    @Restrict({@Group("foo"), @Group("!bar")})
    public static Result restrictedToFooOrNotBar()
    {
        return ok("Content accessible");
    }
}
