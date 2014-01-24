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
package be.objectify.deadbolt.java.utils;

import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.core.PluginConfigKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Http;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class RequestUtils
{
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestUtils.class);

    private RequestUtils()
    {
        // no-op
    }

    /**
     * Get the {@link Subject} from somewhere.
     *
     * <p>If per-request subject caching is enabled, the request is checked first.
     * If no subject is present there, DeadboltHandler#getSubject is called.  The resulting subject, if any, is
     * cached in the request.</p>
     *
     * <p>If per-request subject caching is not enabled, DeadboltHandler#getSubject is called.</p>
     *
     * @param deadboltHandler the current Deadbolt handler
     * @param ctx the context
     * @return the current subject or null if one isn't present
     */
    public static Subject getSubject(DeadboltHandler deadboltHandler,
                                     Http.Context ctx)
    {
        Object cachedUser = ctx.args.get(PluginConfigKeys.CACHE_DEADBOLT_USER);
        Subject subject = null;
        try
        {
            if (PluginUtils.isUserCacheEnabled())
            {
                if (cachedUser != null)
                {
                    subject = (Subject) cachedUser;
                }
                else
                {
                    subject = deadboltHandler.getSubject(ctx);
                    ctx.args.put(PluginConfigKeys.CACHE_DEADBOLT_USER,
                                 subject);
                }
            }
            else
            {
                subject = deadboltHandler.getSubject(ctx);
            }
        }
        catch (Exception e)
        {
            LOGGER.error("Error getting subject: " + e.getMessage(),
                         e);
        }
        return subject;
    }
}
