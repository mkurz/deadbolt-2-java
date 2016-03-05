package be.objectify.deadbolt.java.composite;

import be.objectify.deadbolt.java.DeadboltHandler;
import play.mvc.Http;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class ExceptionThrowingConstraint implements Constraint
{
    private final String name;

    public ExceptionThrowingConstraint(String name)
    {
        this.name = name;
    }

    @Override
    public CompletionStage<Boolean> test(Http.Context context, DeadboltHandler handler, Executor executor)
    {
        throw new RuntimeException(String.format("A composite constraint with name [%s] is specified but is not registered",
                                                 name));
    }
}
