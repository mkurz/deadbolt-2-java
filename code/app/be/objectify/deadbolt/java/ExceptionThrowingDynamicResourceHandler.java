package be.objectify.deadbolt.java;

import play.mvc.Http;

import java.util.concurrent.CompletionStage;

/**
 * Throws a runtime exception when a required {@link DynamicResourceHandler} is not found.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public class ExceptionThrowingDynamicResourceHandler implements DynamicResourceHandler
{
    public static final DynamicResourceHandler INSTANCE = new ExceptionThrowingDynamicResourceHandler();

    private ExceptionThrowingDynamicResourceHandler()
    {
        // no-op
    }

    @Override
    public CompletionStage<Boolean> isAllowed(final String name,
                                              final String meta,
                                              final DeadboltHandler deadboltHandler,
                                              final Http.Context ctx)
    {
        throw new RuntimeException(String.format("A dynamic resource with name [%s] is specified but no dynamic resource handler is provided",
                                                 name));
    }

    @Override
    public CompletionStage<Boolean> checkPermission(final String permissionValue,
                                                    final DeadboltHandler deadboltHandler,
                                                    final Http.Context ctx)
    {
        throw new RuntimeException(String.format("A custom permission type is specified for value [%s] but no dynamic resource handler is provided",
                                                 permissionValue));
    }
}
