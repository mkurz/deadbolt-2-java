package be.objectify.deadbolt.java.composite;

import be.objectify.deadbolt.java.AbstractDynamicResourceHandler;
import be.objectify.deadbolt.java.DeadboltHandler;
import org.junit.Assert;
import org.junit.Test;
import play.libs.F;
import play.mvc.Http;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public abstract class AbstractDynamicConstraintTest extends AbstractConstraintTest
{
    @Test
    public void testPass() throws Exception
    {
        final DynamicConstraint constraint = constraint();
        final CompletionStage<Boolean> result = constraint.test(context,
                                                                withDrh(new AbstractDynamicResourceHandler()
                                                                {
                                                                    @Override
                                                                    public CompletionStage<Boolean> isAllowed(final String name,
                                                                                                              final String meta,
                                                                                                              final DeadboltHandler deadboltHandler,
                                                                                                              final Http.Context ctx)
                                                                    {
                                                                        return CompletableFuture.completedFuture(true);
                                                                    }
                                                                }),
                                                                Executors.newSingleThreadExecutor());
        Assert.assertTrue(toBoolean(result));
    }

    protected abstract DynamicConstraint constraint();

    @Test
    public void testFail() throws Exception
    {
        final DynamicConstraint constraint = constraint();
        final CompletionStage<Boolean> result = constraint.test(context,
                                                                withDrh(new AbstractDynamicResourceHandler()
                                                                {
                                                                    @Override
                                                                    public CompletionStage<Boolean> isAllowed(final String name,
                                                                                                              final String meta,
                                                                                                              final DeadboltHandler deadboltHandler,
                                                                                                              final Http.Context ctx)
                                                                    {
                                                                        return CompletableFuture.completedFuture(false);
                                                                    }
                                                                }),
                                                                Executors.newSingleThreadExecutor());
        Assert.assertFalse(toBoolean(result));
    }

    @Override
    protected F.Tuple<Constraint, Function<Constraint, CompletionStage<Boolean>>> satisfy()
    {
        return new F.Tuple<>(constraint(),
                             c -> c.test(context,
                                         withDrh(new AbstractDynamicResourceHandler()
                                         {
                                             @Override
                                             public CompletionStage<Boolean> isAllowed(final String name,
                                                                                       final String meta,
                                                                                       final DeadboltHandler deadboltHandler,
                                                                                       final Http.Context ctx)
                                             {
                                                 return CompletableFuture.completedFuture(true);
                                             }
                                         }),
                                         Executors.newSingleThreadExecutor()));
    }
}
