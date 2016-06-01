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
import be.objectify.deadbolt.java.DeadboltExecutionContextProvider;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.ExecutionContextProvider;
import be.objectify.deadbolt.java.models.Subject;
import play.Configuration;
import play.libs.concurrent.HttpExecution;
import play.mvc.Http;
import scala.concurrent.ExecutionContext;
import scala.concurrent.ExecutionContextExecutor;

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
    private final DeadboltExecutionContextProvider executionContextProvider;

    @Inject
    public DefaultSubjectCache(final Configuration configuration,
                               final ExecutionContextProvider ecProvider)
    {
        this.cacheUserPerRequestEnabled = configuration.getBoolean(ConfigKeys.CACHE_DEADBOLT_USER_DEFAULT._1,
                                                                   ConfigKeys.CACHE_DEADBOLT_USER_DEFAULT._2);
        this.executionContextProvider = ecProvider.get();
    }

    @Override
    public CompletionStage<Optional<? extends Subject>> apply(final DeadboltHandler deadboltHandler,
                                                              final Http.Context context)
    {
        final CompletionStage<Optional<? extends Subject>> promise;
        if (cacheUserPerRequestEnabled)
        {
            final Optional<? extends Subject> cachedUser = Optional.ofNullable((Subject) context.args.get(ConfigKeys.CACHE_DEADBOLT_USER_DEFAULT._1));
            if (cachedUser.isPresent())
            {
                promise = CompletableFuture.completedFuture(cachedUser);
            }
            else
            {
                final ExecutionContext executionContext = executionContextProvider.get();
                final ExecutionContextExecutor executor = HttpExecution.fromThread(executionContext);
                promise = deadboltHandler.getSubject(context)
                                         .thenApplyAsync(subjectOption -> {
                                             subjectOption.ifPresent(subject -> context.args.put(ConfigKeys.CACHE_DEADBOLT_USER_DEFAULT._1,
                                                                                                 subject));
                                             return subjectOption;
                                         }, executor);
            }
        }
        else
        {
            promise = deadboltHandler.getSubject(context);
        }

        return promise;
    }
}
