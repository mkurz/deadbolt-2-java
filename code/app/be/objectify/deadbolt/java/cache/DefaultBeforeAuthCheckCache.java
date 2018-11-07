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
import play.libs.F;
import play.libs.typedmap.TypedKey;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Matthias Kurz (m.kurz@irregular.at)
 */
@Singleton
public class DefaultBeforeAuthCheckCache implements BeforeAuthCheckCache
{
    private final boolean cacheBeforeAuthCheckPerRequestEnabled;
    private final ConcurrentMap<Long, TypedKey<Boolean>> typedKeyCache = new ConcurrentHashMap<>();

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
    public CompletionStage<F.Tuple<Optional<Result>, Http.RequestHeader>> apply(final DeadboltHandler deadboltHandler,
                                                                          final Http.RequestHeader requestHeader,
                                                                          final Optional<String> content)
    {
        final CompletionStage<Optional<Result>> promise;
        if (cacheBeforeAuthCheckPerRequestEnabled)
        {
            final TypedKey<Boolean> deadboltHandlerCacheId = this.typedKeyCache.computeIfAbsent(deadboltHandler.getId(), k -> TypedKey.create(ConfigKeys.CACHE_BEFORE_AUTH_CHECK_DEFAULT._1 + "." + k)); // results into "deadbolt.java.cache-before-auth-check.0"
            if (requestHeader.attrs().containsKey(deadboltHandlerCacheId))
            {
                return CompletableFuture.completedFuture(F.Tuple(Optional.empty(), requestHeader));
            }
            else
            {
                return deadboltHandler.beforeAuthCheck(requestHeader, content)
                                         .thenApply(beforeAuthCheckOption -> beforeAuthCheckOption.map(r -> F.Tuple(Optional.of(r), requestHeader)).orElse(F.Tuple(Optional.empty(), requestHeader.addAttr(deadboltHandlerCacheId, true))));
            }
        }
        return deadboltHandler.beforeAuthCheck(requestHeader, content).thenApply(beforeAuthCheckOption -> beforeAuthCheckOption.map(r -> F.Tuple(Optional.of(r), requestHeader)).orElseGet(() -> F.Tuple(Optional.empty(), requestHeader)));
    }
}
