package be.objectify.deadbolt.java.test.controllers.restrict;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import play.mvc.Controller;
import play.mvc.Result;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class RestrictMethodConstraints extends Controller
{
    @Restrict(@Group({"foo", "bar"}))
    public CompletionStage<Result> restrictedToFooAndBar()
    {
        return CompletableFuture.supplyAsync(() -> ok("Content accessible"));
    }

    @Restrict({@Group("foo"), @Group("bar")})
    public CompletionStage<Result> restrictedToFooOrBar()
    {
        return CompletableFuture.supplyAsync(() -> ok("Content accessible"));
    }

    @Restrict(@Group({"foo", "!bar"}))
    public CompletionStage<Result> restrictedToFooAndNotBar()
    {
        return CompletableFuture.supplyAsync(() -> ok("Content accessible"));
    }

    @Restrict({@Group("foo"), @Group("!bar")})
    public CompletionStage<Result> restrictedToFooOrNotBar()
    {
        return CompletableFuture.supplyAsync(() -> ok("Content accessible"));
    }
}
