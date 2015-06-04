package be.objectify.deadbolt.java.cache;

import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public interface PatternCache extends Function<String, Pattern>
{
}
