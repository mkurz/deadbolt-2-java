package be.objectify.deadbolt.java.test.controllers;

import play.mvc.Controller;
import play.mvc.Result;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class NoConstraintsApp extends Controller
{
    public CompletionStage<Result> index()
    {
        return CompletableFuture.supplyAsync(() -> ok("Content accessible"));
    }
}
