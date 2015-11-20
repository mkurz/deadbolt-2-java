package be.objectify.deadbolt.java;

import play.mvc.Http;
import play.mvc.Result;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public abstract class AbstractNoPreAuthDeadboltHandler extends AbstractDeadboltHandler
{
    @Override
    public CompletionStage<Optional<Result>> beforeAuthCheck(final Http.Context context)
    {
        return CompletableFuture.completedFuture(Optional.empty());
    }
}
