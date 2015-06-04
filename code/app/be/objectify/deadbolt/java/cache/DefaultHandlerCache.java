package be.objectify.deadbolt.java.cache;

import be.objectify.deadbolt.java.ConfigKeys;
import be.objectify.deadbolt.java.DeadboltHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Configuration;
import play.Environment;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@Singleton
public class DefaultHandlerCache implements
                                 HandlerCache
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultHandlerCache.class);

    private final Map<String, DeadboltHandler> handlers = new HashMap<>();

    @Inject
    public DefaultHandlerCache(final Configuration configuration,
                               final Environment environment)
    {
        final Set<String> configurationKeys = configuration.keys();
        final ClassLoader classLoader = environment.classLoader();

        // Get the named handlers first to allow the global handler to override if necessary
        final Object object = configuration.getObject(ConfigKeys.NAMED_HANDLERS);
        if (object != null) {
            final Map<String, String> namedHandlers = (Map<String, String>)object;
            for (Map.Entry<String, String> entry : namedHandlers.entrySet())
            {
                final String key = entry.getKey();
                try
                {
                    handlers.put(key,
                                 (DeadboltHandler) Class.forName(entry.getValue(),
                                                                 true,
                                                                 classLoader).newInstance());
                }
                catch (Exception e)
                {
                    throw configuration.reportError(ConfigKeys.NAMED_HANDLERS,
                                                    "Error creating Deadbolt handler: " + key,
                                                    e);
                }
            }
        }

        if (configurationKeys.contains(ConfigKeys.DEADBOLT_HANDLER_KEY))
        {
            String deadboltHandlerName = null;
            try
            {
                deadboltHandlerName = configuration.getString(ConfigKeys.DEADBOLT_HANDLER_KEY);
                handlers.put(ConfigKeys.DEFAULT_HANDLER_KEY,
                             (DeadboltHandler) Class.forName(deadboltHandlerName,
                                                             true,
                                                             classLoader).newInstance());
            }
            catch (Exception e)
            {
                throw configuration.reportError(ConfigKeys.DEADBOLT_HANDLER_KEY,
                                                "Error creating Deadbolt handler: " + deadboltHandlerName,
                                                e);
            }
        }

        if (handlers.containsKey(ConfigKeys.DEFAULT_HANDLER_KEY))
        {
            LOGGER.warn("No default handler declared for Deadbolt");
        }

    }

    /**
     * Get the handler mapped to the given key.
     *
     * @param handlerKey the unique key of the handler
     * @return an option of the handler
     */
    @Override
    public DeadboltHandler apply(final String handlerKey)
    {
        final DeadboltHandler handler;
        if (handlers.containsKey(handlerKey))
        {
            handler = handlers.get(handlerKey);
            LOGGER.debug("Retrieved handler [{}] for key [{}]",
                         handler,
                         handlerKey);
        }
        else
        {
            LOGGER.error("Handler key [{}] is not defined.  You need to look at this urgently.");
            LOGGER.error("Falling back to default handler");
            handler = handlers.get(ConfigKeys.DEFAULT_HANDLER_KEY);
        }
        return handler;
    }

    /**
     * Get the default DeadboltHandler.
     */
    @Override
    public DeadboltHandler get()
    {
        return apply(ConfigKeys.DEFAULT_HANDLER_KEY);
    }
}