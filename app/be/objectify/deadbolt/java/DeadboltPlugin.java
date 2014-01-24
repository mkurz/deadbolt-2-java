/*
 * Copyright 2012 Steve Chaloner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.objectify.deadbolt.java;

import be.objectify.deadbolt.core.PluginConfigKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Application;
import play.Configuration;
import play.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * A play plugin that provides authorization mechanism for defining access rights
 * to certain controller methods or parts of a view using a simple AND/OR/NOT syntax.
 */
public class DeadboltPlugin extends Plugin
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DeadboltPlugin.class);

    private static final String DEFAULT_HANDLER_KEY = "defaultHandler";
    private static final String NAMED_HANDLERS = "deadbolt.java.handlers";

    private boolean cacheUserPerRequestEnabled = false;
    private int beforeAuthCheckTimeout = 2000;
    private Map<String, DeadboltHandler> handlers;

    private final Application application;

    public DeadboltPlugin(Application application)
    {
        this.application = application;
    }

    /**
     * Reads the configuration file and initialize the {@link DeadboltHandler}
     */
    @Override
    public void onStart()
    {
        handlers = new HashMap<String, DeadboltHandler>();
        Configuration configuration = application.configuration();
        Set<String> configurationKeys = configuration.keys();
        ClassLoader classloader = application.classloader();

        // Get the named handlers first to allow the global handler to override if necessary
        Object object = configuration.getObject(NAMED_HANDLERS);
        if (object != null) {
            Map<String, String> namedHandlers = (Map<String, String>)object;
            for (Map.Entry<String, String> entry : namedHandlers.entrySet())
            {
                String key = entry.getKey();
                try
                {
                    handlers.put(key,
                                 (DeadboltHandler) Class.forName(entry.getValue(),
                                                                 true,
                                                                 classloader).newInstance());
                }
                catch (Exception e)
                {
                    throw configuration.reportError(NAMED_HANDLERS,
                                                    "Error creating Deadbolt handler: " + key,
                                                    e);
                }
            }
        }

        if (configurationKeys.contains(PluginConfigKeys.DEADBOLT_JAVA_HANDLER_KEY))
        {
            String deadboltHandlerName = null;
            try
            {
                deadboltHandlerName = configuration.getString(PluginConfigKeys.DEADBOLT_JAVA_HANDLER_KEY);
                handlers.put(DEFAULT_HANDLER_KEY,
                             (DeadboltHandler) Class.forName(deadboltHandlerName,
                                                             true,
                                     classloader).newInstance());
            }
            catch (Exception e)
            {
                throw configuration.reportError(PluginConfigKeys.DEADBOLT_JAVA_HANDLER_KEY,
                        "Error creating Deadbolt handler: " + deadboltHandlerName,
                        e);
            }
        }
        else
        {
            LOGGER.warn("No Java handler declared for Deadbolt");
        }

        if (configurationKeys.contains(PluginConfigKeys.BEFORE_AUTH_CHECK_TIMEOUT))
        {
            beforeAuthCheckTimeout = configuration.getInt(PluginConfigKeys.BEFORE_AUTH_CHECK_TIMEOUT);
        }

        cacheUserPerRequestEnabled = configuration.getBoolean(PluginConfigKeys.CACHE_DEADBOLT_USER, false);
    }

    /**
     * Getter for the cache-user configuration option
     *
     * @return boolean cache-user value
     */
    public boolean isCacheUserPerRequestEnabled()
    {
        return cacheUserPerRequestEnabled;
    }

    public int getBeforeAuthCheckTimeout()
    {
        return beforeAuthCheckTimeout;
    }

    /**
     * Getter for the default Deadbolt Handler
     *
     * @return the registered Deadbolt handler, or null if it's not defined
     */
    public DeadboltHandler getDeadboltHandler()
    {
        return handlers.get(DEFAULT_HANDLER_KEY);
    }

    /**
     * Getter for a named Deadbolt Handler
     *
     * @param handlerKey the key of the handler, as defined in the configuration
     * @return the named Deadbolt handler, or null if it's not defined
     */
    public DeadboltHandler getDeadboltHandler(String handlerKey)
    {
        return handlers.get(handlerKey);
    }
}
