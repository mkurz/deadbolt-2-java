/*
 * Copyright 2012-2015 Steve Chaloner
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
import be.objectify.deadbolt.java.models.Subject;
import play.Configuration;
import play.libs.concurrent.HttpExecution;
import play.mvc.Http;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@Singleton
public class DefaultSubjectCache implements SubjectCache
{
    private final boolean cacheUserPerRequestEnabled;

    @Inject
    public DefaultSubjectCache(final Configuration configuration)
    {
        this.cacheUserPerRequestEnabled = configuration.getBoolean(ConfigKeys.CACHE_DEADBOLT_USER_DEFAULT._1,
                                                                   ConfigKeys.CACHE_DEADBOLT_USER_DEFAULT._2);
    }

    @Override
    public CompletionStage<Optional<Subject>> apply(final DeadboltHandler deadboltHandler,
                                                    final Http.Context context)
    {
        final CompletionStage<Optional<Subject>> promise;
        if (cacheUserPerRequestEnabled)
        {
            final Optional<Subject> cachedUser = Optional.ofNullable((Subject) context.args.get(ConfigKeys.CACHE_DEADBOLT_USER_DEFAULT._1));
            if (cachedUser.isPresent())
            {
                promise = CompletableFuture.completedFuture(cachedUser);
            }
            else
            {
                promise = deadboltHandler.getSubject(context)
                                         .thenApplyAsync(subjectOption -> {
                                             subjectOption.ifPresent(subject -> context.args.put(ConfigKeys.CACHE_DEADBOLT_USER_DEFAULT._1,
                                                                                                 subject));
                                             return subjectOption;
                                         }, HttpExecution.defaultContext());
            }
        }
        else
        {
            promise = deadboltHandler.getSubject(context);
        }

        return promise;
    }
}
