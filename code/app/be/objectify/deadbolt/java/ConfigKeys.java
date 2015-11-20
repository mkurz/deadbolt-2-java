package be.objectify.deadbolt.java;

import play.libs.F;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class ConfigKeys
{
    public static final String DEFAULT_HANDLER_KEY = "defaultHandler";
    public static final String CACHE_DEADBOLT_USER = "deadbolt.java.cache-user";
    public static final F.Tuple<String, Boolean> CACHE_DEADBOLT_USER_DEFAULT = new F.Tuple<>(CACHE_DEADBOLT_USER,
                                                                                             false);
    public static final String DEFAULT_VIEW_TIMEOUT = "deadbolt.java.view-timeout";
    public static final F.Tuple<String, Long> DEFAULT_VIEW_TIMEOUT_DEFAULT = new F.Tuple<>(DEFAULT_VIEW_TIMEOUT,
                                                                                           1000L);
    public static final String CUSTOM_EC = "deadbolt.java.custom-execution-context.enable";
    public static final F.Tuple<String, Boolean> CUSTOM_EC_DEFAULT = new F.Tuple<>(CUSTOM_EC,
                                                                                   false);

    public static final String PATTERN_INVERT = "deadbolt.pattern.invert";

    private ConfigKeys()
    {
        // no-op
    }
}
