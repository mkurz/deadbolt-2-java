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
package be.objectify.deadbolt.java.cache;

import be.objectify.deadbolt.java.ConfigKeys;
import be.objectify.deadbolt.java.DeadboltHandler;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author Matthias Kurz (m.kurz@irregular.at)
 */
@Singleton
public class DefaultBeforeAuthCheckCache implements BeforeAuthCheckCache
{
    private final boolean cacheBeforeAuthCheckPerRequestEnabled;

    @Inject
    public DefaultBeforeAuthCheckCache(final Config config)
    {
        final HashMap<String, Object> defaults = new HashMap<>();
        defaults.put(ConfigKeys.CACHE_BEFORE_AUTH_CHECK_DEFAULT._1,
                     ConfigKeys.CACHE_BEFORE_AUTH_CHECK_DEFAULT._2);
        final Config configWithFallback = config.withFallback(ConfigFactory.parseMap(defaults));
        this.cacheBeforeAuthCheckPerRequestEnabled = configWithFallback.getBoolean(ConfigKeys.CACHE_BEFORE_AUTH_CHECK_DEFAULT._1);
    }

    @Override
    public CompletionStage<Optional<Result>> apply(final DeadboltHandler deadboltHandler,
                                                              final Http.Context context,
                                                              final Optional<String> content)
    {
        final CompletionStage<Optional<Result>> promise;
        if (cacheBeforeAuthCheckPerRequestEnabled)
        {
            final String deadboltHandlerCacheId = ConfigKeys.CACHE_BEFORE_AUTH_CHECK_DEFAULT._1 + "." + deadboltHandler.getId(); // results into "deadbolt.java.cache-before-auth-check.0"
            if (context.args.containsKey(deadboltHandlerCacheId))
            {
                promise = CompletableFuture.completedFuture(Optional.empty());
            }
            else
            {
                promise = deadboltHandler.beforeAuthCheck(context, content)
                                         .thenApply(beforeAuthCheckOption ->
                                                         {
                                                             if(!beforeAuthCheckOption.isPresent())
                                                             {
                                                                 context.args.put(deadboltHandlerCacheId, true);
                                                             }
                                                             return beforeAuthCheckOption;
                                                         });
            }
        }
        else
        {
            promise = deadboltHandler.beforeAuthCheck(context, content);
        }

        return promise;
    }
}
