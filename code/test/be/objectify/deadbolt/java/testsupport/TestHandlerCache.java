/*
 * Copyright 2010-2016 Steve Chaloner
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
package be.objectify.deadbolt.java.testsupport;

import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.cache.HandlerCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class TestHandlerCache implements HandlerCache
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TestHandlerCache.class);

    private final DeadboltHandler defaultHandler;
    private final Map<String, DeadboltHandler> handlers = new HashMap<>();

    public TestHandlerCache(final DeadboltHandler defaultHandler,
                            final Map<String, DeadboltHandler> handlers)
    {
        this.defaultHandler = defaultHandler;
        this.handlers.putAll(handlers);
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
            // don't do this in real life! Returning null is for forcing tests to fail if the key is wrong
            handler = null;
        }
        return handler;
    }

    /**
     * Get the default DeadboltHandler.
     */
    @Override
    public DeadboltHandler get()
    {
        return defaultHandler;
    }
}
