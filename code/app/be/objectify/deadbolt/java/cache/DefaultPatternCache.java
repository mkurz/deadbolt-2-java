package be.objectify.deadbolt.java.cache;

import play.cache.CacheApi;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.regex.Pattern;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@Singleton
public class DefaultPatternCache implements PatternCache
{
    private final CacheApi cache;

    @Inject
    public DefaultPatternCache(final CacheApi cache)
    {
        this.cache = cache;
    }

    @Override
    public Pattern apply(final String patternValue)
    {
        return cache.getOrElse("Deadbolt." + patternValue,
                               () -> Pattern.compile(patternValue));
    }
}
