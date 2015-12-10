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

import be.objectify.deadbolt.core.PatternType;
import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.cache.HandlerCache;
import be.objectify.deadbolt.java.cache.PatternCache;
import be.objectify.deadbolt.java.cache.SubjectCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Configuration;
import play.libs.concurrent.HttpExecution;
import play.mvc.Http;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Provides the entry point for view-level annotations.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
@Singleton
public class ViewSupport
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ViewSupport.class);

    public final Supplier<Long> defaultTimeout;

    private final JavaAnalyzer analyzer;

    private final SubjectCache subjectCache;

    private final HandlerCache handlerCache;

    private final PatternCache patternCache;

    private final TemplateFailureListener failureListener;

    private final BiFunction<Long, TimeoutException, Boolean> timeoutHandler;

    @Inject
    public ViewSupport(final Configuration configuration,
                       final JavaAnalyzer analyzer,
                       final SubjectCache subjectCache,
                       final HandlerCache handlerCache,
                       final PatternCache patternCache,
                       final TemplateFailureListenerProvider failureListener)
    {
        this.analyzer = analyzer;
        this.subjectCache = subjectCache;
        this.handlerCache = handlerCache;
        this.patternCache = patternCache;
        this.failureListener = failureListener.get();


        final Long timeout = configuration.getLong(ConfigKeys.DEFAULT_VIEW_TIMEOUT_DEFAULT._1,
                                                   ConfigKeys.DEFAULT_VIEW_TIMEOUT_DEFAULT._2);
        LOGGER.info("Default timeout period for blocking views is [{}]ms",
                    timeout);
        this.defaultTimeout = () -> timeout;

        timeoutHandler = (timeoutInMillis, e) -> {
            LOGGER.error("Timeout when attempting to complete future within [{}]ms.  Denying access to resource.",
                         timeoutInMillis,
                         e);
            this.failureListener.failure("Error when checking view constraint: " + e.getMessage(),
                                         timeoutInMillis);
            return false;
        };
    }


    /**
     * Used for restrict tags in the template.
     *
     * @param roles a list of String arrays.  Within an array, the roles are ANDed.  The arrays in the list are OR'd.
     * @return true if the view can be accessed, otherwise false
     */
    public boolean viewRestrict(final List<String[]> roles,
                                final DeadboltHandler handler,
                                final long timeoutInMillis) throws Throwable
    {
        final Function<Optional<Subject>, Boolean> testRoles = subject -> {
            boolean roleOk = false;
            for (int i = 0; !roleOk && i < roles.size(); i++)
            {
                roleOk = analyzer.checkRole(subject,
                                            roles.get(i));
            }
            return roleOk;

        };

        boolean allowed;
        try
        {
            allowed = subjectCache.apply(handler == null ? handlerCache.get()
                                                         : handler,
                                         Http.Context.current())
                                  .thenApplyAsync(testRoles::apply, HttpExecution.defaultContext())
                                  .toCompletableFuture()
                                  .get(timeoutInMillis,
                                       TimeUnit.MILLISECONDS);

        }
        catch (TimeoutException e)
        {
            allowed = timeoutHandler.apply(timeoutInMillis,
                                           e);
        }
        return allowed;
    }

    /**
     * Used for dynamic tags in the template.
     *
     * @param name the name of the resource
     * @param meta meta information on the resource
     * @return true if the view can be accessed, otherwise false
     */
    public boolean viewDynamic(final String name,
                               final String meta,
                               final DeadboltHandler handler,
                               final long timeoutInMillis) throws Throwable
    {
        final Http.Context context = Http.Context.current();
        final DeadboltHandler deadboltHandler = handler == null ? handlerCache.get()
                                                                : handler;
        boolean allowed;
        try
        {
            allowed = deadboltHandler.getDynamicResourceHandler(Http.Context.current())
                                     .thenApplyAsync(drhOption -> drhOption.orElseGet(() -> ExceptionThrowingDynamicResourceHandler.INSTANCE), HttpExecution.defaultContext())
                                     .thenComposeAsync(drh -> drh.isAllowed(name,
                                                                       meta,
                                                                       deadboltHandler,
                                                                       context), HttpExecution.defaultContext())
                                     .toCompletableFuture()
                                     .get(timeoutInMillis,
                                          TimeUnit.MILLISECONDS);
        }
        catch (TimeoutException e)
        {
            allowed = timeoutHandler.apply(timeoutInMillis,
                                           e);
        }
        return allowed;
    }

    /**
     * Used for subjectPresent tags in the template.
     *
     * @return true if the view can be accessed, otherwise false
     */
    public boolean viewSubjectPresent(final DeadboltHandler handler,
                                      final long timeoutInMillis) throws Throwable
    {
        boolean allowed;
        try
        {
            allowed = subjectCache.apply(handler,
                                         Http.Context.current())
                                  .toCompletableFuture()
                                  .get(timeoutInMillis,
                                       TimeUnit.MILLISECONDS)
                                  .isPresent();
        }
        catch (TimeoutException e)
        {
            allowed = timeoutHandler.apply(timeoutInMillis,
                                           e);
        }
        return allowed;
    }

    /**
     * Used for subjectNotPresent tags in the template.
     *
     * @return true if the view can be accessed, otherwise false
     */
    public boolean viewSubjectNotPresent(final DeadboltHandler handler,
                                         final long timeoutInMillis) throws Throwable
    {
        boolean allowed;
        try
        {
            allowed = !subjectCache.apply(handler,
                                          Http.Context.current())
                                   .toCompletableFuture()
                                   .get(timeoutInMillis,
                                        TimeUnit.MILLISECONDS)
                                   .isPresent();
        }
        catch (TimeoutException e)
        {
            allowed = timeoutHandler.apply(timeoutInMillis,
                                           e);
        }
        return allowed;
    }

    public boolean viewPattern(final String value,
                               final PatternType patternType,
                               final DeadboltHandler handler,
                               final long timeoutInMillis) throws Exception
    {
        final Http.Context context = Http.Context.current();
        final DeadboltHandler deadboltHandler = handler == null ? handlerCache.get()
                                                                : handler;

        boolean allowed;
        try
        {
            switch (patternType)
            {
                case EQUALITY:
                    allowed = subjectCache.apply(deadboltHandler, Http.Context.current())
                                          .thenApplyAsync(subjectOption -> analyzer.checkPatternEquality(subjectOption,
                                                                                                    Optional.ofNullable(value)), HttpExecution.defaultContext())
                                          .toCompletableFuture()
                                          .get(timeoutInMillis,
                                               TimeUnit.MILLISECONDS);
                    break;
                case REGEX:
                    allowed = subjectCache.apply(deadboltHandler, Http.Context.current())
                                          .thenApplyAsync(subjectOption -> analyzer.checkRegexPattern(subjectOption,
                                                                                           Optional.ofNullable(patternCache.apply(value))), HttpExecution.defaultContext())
                                          .toCompletableFuture()
                                          .get(timeoutInMillis,
                                               TimeUnit.MILLISECONDS);
                    break;
                case CUSTOM:
                    allowed = analyzer.checkCustomPattern(deadboltHandler,
                                                          context,
                                                          value)
                                      .toCompletableFuture()
                                      .get(timeoutInMillis,
                                           TimeUnit.MILLISECONDS);
                    break;
                default:
                    allowed = false;
                    LOGGER.error("Unknown pattern type [{}]",
                                 patternType);
            }
        }
        catch (TimeoutException e)
        {
            allowed = timeoutHandler.apply(timeoutInMillis,
                                           e);
        }

        return allowed;
    }
}