package be.objectify.deadbolt.java.test.controllers.restrict;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.Unrestricted;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Restrict({@Group("foo"), @Group("!bar")})
public class RestrictedToFooOrNotBar extends Controller
{
    public CompletionStage<Result> index()
    {
        return CompletableFuture.supplyAsync(() -> ok("Content accessible"));
    }

    @Unrestricted
    public CompletionStage<Result> unrestricted()
    {
        return CompletableFuture.supplyAsync(() -> ok("Content accessible"));
    }
}
