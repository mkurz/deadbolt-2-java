package be.objectify.deadbolt.java;

import play.mvc.Http;
import play.mvc.Result;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class NoPreAuthDeadboltHandler extends AbstractDeadboltHandler
{
    public NoPreAuthDeadboltHandler(final ExecutionContextProvider ecProvider)
    {
        super(ecProvider);
    }

    @Override
    public CompletionStage<Optional<Result>> beforeAuthCheck(final Http.Context context)
    {
        return CompletableFuture.completedFuture(Optional.empty());
    }

    @Override
    public String handlerName()
    {
        return ConfigKeys.DEFAULT_HANDLER_KEY;
    }
}
