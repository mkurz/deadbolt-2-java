package be.objectify.deadbolt.java;

import play.mvc.Http;
import play.mvc.Result;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public abstract class AbstractNoPreAuthDeadboltHandler extends AbstractDeadboltHandler
{
    public AbstractNoPreAuthDeadboltHandler(final ExecutionContextProvider ecProvider)
    {
        super(ecProvider);
    }

    @Override
    public CompletionStage<Optional<Result>> beforeAuthCheck(final Http.Context context)
    {
        return CompletableFuture.completedFuture(Optional.empty());
    }
}
