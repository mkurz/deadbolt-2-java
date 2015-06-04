package be.objectify.deadbolt.java.cache;

import play.cache.Cache;

import javax.inject.Singleton;
import java.util.regex.Pattern;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@Singleton
public class DefaultPatternCache implements PatternCache
{
    @Override
    public Pattern apply(final String patternValue)
    {
        return Cache.getOrElse("Deadbolt." + patternValue,
                               () -> Pattern.compile(patternValue),
                               0);
    }
}
