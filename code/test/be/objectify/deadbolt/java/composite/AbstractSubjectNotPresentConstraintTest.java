package be.objectify.deadbolt.java.composite;

import be.objectify.deadbolt.java.DeadboltHandler;
import org.junit.Assert;
import org.junit.Test;
import play.libs.F;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.function.Function;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public abstract class AbstractSubjectNotPresentConstraintTest extends AbstractConstraintTest
{
    @Test
    public void testSubjectPresent() throws Exception
    {
        final Constraint constraint = constraint(withSubject(this::subject));
        final CompletionStage<Boolean> result = constraint.test(context,
                                                                withSubject(this::subject),
                                                                Executors.newSingleThreadExecutor());
        Assert.assertFalse(toBoolean(result));
    }

    @Test
    public void testSubjectNotPresent() throws Exception
    {
        final Constraint constraint = constraint(withSubject(() -> null));
        final CompletionStage<Boolean> result = constraint.test(context,
                                                                withSubject(() -> null),
                                                                Executors.newSingleThreadExecutor());
        Assert.assertTrue(toBoolean(result));
    }

    @Override
    protected F.Tuple<Constraint, Function<Constraint, CompletionStage<Boolean>>> satisfy()
    {
        return new F.Tuple<>(constraint(withSubject(() -> null)),
                             c -> c.test(context,
                                         withSubject(() -> null),
                                         Executors.newSingleThreadExecutor()));
    }

    protected abstract SubjectNotPresentConstraint constraint(final DeadboltHandler handler);
}
