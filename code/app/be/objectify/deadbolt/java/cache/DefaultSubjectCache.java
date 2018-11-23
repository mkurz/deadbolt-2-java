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

import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.models.Subject;
import com.typesafe.config.Config;
import play.libs.F;
import play.libs.typedmap.TypedKey;
import play.mvc.Http;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@Singleton
public class DefaultSubjectCache implements SubjectCache
{
    private final boolean cacheUserPerRequestEnabled;
    private final ConcurrentMap<Long, TypedKey<Subject>> typedKeyCache = new ConcurrentHashMap<>();

    @Inject
    public DefaultSubjectCache(final Config config)
    {
        this.cacheUserPerRequestEnabled = config.getBoolean("deadbolt.java.cache-user");
    }

    @Override
    public CompletionStage<F.Tuple<Optional<? extends Subject>, Http.RequestHeader>> apply(final DeadboltHandler deadboltHandler,
                                                                       final Http.RequestHeader requestHeader)
    {
        if (cacheUserPerRequestEnabled)
        {
            final TypedKey<Subject> deadboltHandlerCacheId = this.typedKeyCache.computeIfAbsent(deadboltHandler.getId(), k -> TypedKey.create("deadbolt.java.cache-user." + k));
            final Optional<? extends Subject> cachedUser = requestHeader.attrs().getOptional(deadboltHandlerCacheId);
            if (cachedUser.isPresent())
            {
                return CompletableFuture.completedFuture(F.Tuple(cachedUser, requestHeader));
            }
            else
            {
                return deadboltHandler.getSubject(requestHeader).thenApply(subjectOption -> subjectOption.map(s -> F.<Optional<? extends Subject>, Http.RequestHeader>Tuple(Optional.of(s), requestHeader.addAttr(deadboltHandlerCacheId, s))).orElseGet(() -> F.Tuple(Optional.empty(), requestHeader)));
            }
        }
        return deadboltHandler.getSubject(requestHeader).thenApply(subjectOption -> subjectOption.map(s -> F.<Optional<? extends Subject>, Http.RequestHeader>Tuple(Optional.of(s), requestHeader)).orElseGet(() -> F.Tuple(Optional.empty(), requestHeader)));
    }
}
