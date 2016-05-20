package be.objectify.deadbolt.java.test.controllers;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import play.mvc.Controller;
import play.mvc.Result;

public class Application extends Controller {

    public CompletionStage<Result> index()
    {
        return CompletableFuture.supplyAsync(() -> ok("Content accessible"));
    }
}
