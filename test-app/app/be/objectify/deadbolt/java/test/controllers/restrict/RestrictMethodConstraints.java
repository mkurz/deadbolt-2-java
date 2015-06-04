package be.objectify.deadbolt.java.test.controllers.restrict;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;

public class RestrictMethodConstraints extends Controller
{
    @Restrict(@Group({"foo", "bar"}))
    public static F.Promise<Result> restrictedToFooAndBar()
    {
        return F.Promise.promise(() -> ok("Content accessible"));
    }

    @Restrict({@Group("foo"), @Group("bar")})
    public static F.Promise<Result> restrictedToFooOrBar()
    {
        return F.Promise.promise(() -> ok("Content accessible"));
    }

    @Restrict(@Group({"foo", "!bar"}))
    public static F.Promise<Result> restrictedToFooAndNotBar()
    {
        return F.Promise.promise(() -> ok("Content accessible"));
    }

    @Restrict({@Group("foo"), @Group("!bar")})
    public static F.Promise<Result> restrictedToFooOrNotBar()
    {
        return F.Promise.promise(() -> ok("Content accessible"));
    }
}
