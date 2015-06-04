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
import play.mvc.Http;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Provides the entry point for view-level annotations.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
@Singleton
public class DeadboltViewSupport
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DeadboltViewSupport.class);

    private final JavaDeadboltAnalyzer analyzer;

    private final SubjectCache subjectCache;

    private final HandlerCache handlerCache;

    private final PatternCache patternCache;

    @Inject
    public DeadboltViewSupport(final JavaDeadboltAnalyzer analyzer,
                               final SubjectCache subjectCache,
                               final HandlerCache handlerCache,
                               final PatternCache patternCache)
    {
        this.analyzer = analyzer;
        this.subjectCache = subjectCache;
        this.handlerCache = handlerCache;
        this.patternCache = patternCache;
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
        return subjectCache.apply(handler == null ? handlerCache.get()
                                                  : handler,
                                  Http.Context.current())
                           .map(subjectOption -> testRoles.apply(subjectOption))
                           .get(timeoutInMillis,
                                TimeUnit.MILLISECONDS);

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
        return deadboltHandler.getDynamicResourceHandler(Http.Context.current())
                              .map(drhOption -> drhOption.orElseThrow(() -> new RuntimeException("A dynamic resource is specified but no dynamic resource handler is provided")))
                              .flatMap(drh -> drh.isAllowed(name,
                                                            meta,
                                                            deadboltHandler,
                                                            context))
                              .get(timeoutInMillis,
                                   TimeUnit.MILLISECONDS);
    }

    /**
     * Used for subjectPresent tags in the template.
     *
     * @return true if the view can be accessed, otherwise false
     */
    public boolean viewSubjectPresent(final DeadboltHandler handler,
                                      final long timeoutInMillis) throws Throwable
    {
        return subjectCache.apply(handler,
                                  Http.Context.current())
                           .get(timeoutInMillis,
                                TimeUnit.MILLISECONDS)
                           .isPresent();
    }

    /**
     * Used for subjectNotPresent tags in the template.
     *
     * @return true if the view can be accessed, otherwise false
     */
    public boolean viewSubjectNotPresent(final DeadboltHandler handler,
                                         final long timeoutInMillis) throws Throwable
    {
        return !subjectCache.apply(handler,
                                   Http.Context.current())
                            .get(timeoutInMillis,
                                 TimeUnit.MILLISECONDS)
                            .isPresent();
    }

    public boolean viewPattern(final String value,
                               final PatternType patternType,
                               final DeadboltHandler handler,
                               final long timeoutInMillis) throws Exception
    {
        final Http.Context context = Http.Context.current();
        final DeadboltHandler deadboltHandler = handler == null ? handlerCache.get()
                                                                : handler;

        final boolean allowed;
        switch (patternType)
        {
            case EQUALITY:
                allowed = subjectCache.apply(deadboltHandler, Http.Context.current())
                                      .map(subjectOption -> analyzer.checkPatternEquality(subjectOption,
                                                                                          Optional.ofNullable(value)))
                                      .get(timeoutInMillis,
                                           TimeUnit.MILLISECONDS);
                break;
            case REGEX:
                allowed = subjectCache.apply(deadboltHandler, Http.Context.current())
                                      .map(subjectOption -> analyzer.checkRegexPattern(subjectOption,
                                                                                       Optional.ofNullable(patternCache.apply(value))))
                                      .get(timeoutInMillis,
                                           TimeUnit.MILLISECONDS);
                break;
            case CUSTOM:
                allowed = analyzer.checkCustomPattern(deadboltHandler,
                                                      context,
                                                      value)
                                  .get(timeoutInMillis, TimeUnit.MILLISECONDS);
                break;
            default:
                allowed = false;
                LOGGER.error("Unknown pattern type [{}]",
                             patternType);
        }

        return allowed;
    }
}