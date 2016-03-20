package be.objectify.deadbolt.java.composite;

import be.objectify.deadbolt.java.AbstractDynamicResourceHandler;
import be.objectify.deadbolt.java.DeadboltHandler;
import org.junit.Assert;
import org.junit.Test;
import play.libs.F;
import play.mvc.Http;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public abstract class AbstractDynamicConstraintTest extends AbstractConstraintTest
{
    protected abstract DynamicConstraint constraint(final DeadboltHandler handler);

    @Test
    public void testPass() throws Exception
    {
        final DeadboltHandler handler = withDrh(new AbstractDynamicResourceHandler()
        {
            @Override
            public CompletionStage<Boolean> isAllowed(final String name,
                                                      final Optional<String> meta,
                                                      final DeadboltHandler deadboltHandler,
                                                      final Http.Context ctx)
            {
                return CompletableFuture.completedFuture(true);
            }
        });
        final DynamicConstraint constraint = constraint(handler);
        final CompletionStage<Boolean> result = constraint.test(context,
                                                                handler,
                                                                Executors.newSingleThreadExecutor());
        Assert.assertTrue(toBoolean(result));
    }

    @Test
    public void testFail() throws Exception
    {
        final DeadboltHandler handler = withDrh(new AbstractDynamicResourceHandler()
        {
            @Override
            public CompletionStage<Boolean> isAllowed(final String name,
                                                      final Optional<String> meta,
                                                      final DeadboltHandler deadboltHandler,
                                                      final Http.Context ctx)
            {
                return CompletableFuture.completedFuture(false);
            }
        });
        final DynamicConstraint constraint = constraint(handler);
        final CompletionStage<Boolean> result = constraint.test(context,
                                                                handler,
                                                                Executors.newSingleThreadExecutor());
        Assert.assertFalse(toBoolean(result));
    }

    @Override
    protected F.Tuple<Constraint, Function<Constraint, CompletionStage<Boolean>>> satisfy()
    {
        final DeadboltHandler handler = withDrh(new AbstractDynamicResourceHandler()
        {
            @Override
            public CompletionStage<Boolean> isAllowed(final String name,
                                                      final Optional<String> meta,
                                                      final DeadboltHandler deadboltHandler,
                                                      final Http.Context ctx)
            {
                return CompletableFuture.completedFuture(true);
            }
        });
        return new F.Tuple<>(constraint(handler),
                             c -> c.test(context,
                                         handler,
                                         Executors.newSingleThreadExecutor()));
    }
}
