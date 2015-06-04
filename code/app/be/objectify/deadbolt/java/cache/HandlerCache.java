package be.objectify.deadbolt.java.cache;

import be.objectify.deadbolt.java.DeadboltHandler;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public interface HandlerCache extends Function<String, DeadboltHandler>,
                                      Supplier<DeadboltHandler>
{
}
