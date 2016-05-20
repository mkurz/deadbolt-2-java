package be.objectify.deadbolt.java.test.security;

import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import play.mvc.Http;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Dedicated handler to look for people with the same name as my wife.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public class NiceNameDynamicResourceHandler implements DynamicResourceHandler
{
    @Override
    public CompletionStage<Boolean> isAllowed(final String name,
                                              final Optional<String> meta,
                                              final DeadboltHandler deadboltHandler,
                                              final Http.Context ctx)
    {
        return deadboltHandler.getSubject(ctx)
                              .thenApply(option -> option.isPresent() && option.get().getIdentifier()
                                                                               .contains("greet"));
    }

    @Override
    public CompletionStage<Boolean> checkPermission(final String permissionValue,
                                                    final Optional<String> meta,
                                                    final DeadboltHandler deadboltHandler,
                                                    final Http.Context ctx)
    {
        return CompletableFuture.completedFuture(false);
    }
}
