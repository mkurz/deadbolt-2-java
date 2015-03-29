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
import be.objectify.deadbolt.java.utils.PluginUtils;
import be.objectify.deadbolt.java.utils.RequestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.cache.Cache;
import play.mvc.Http;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

/**
 * Provides the entry point for view-level annotations.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public class DeadboltViewSupport
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DeadboltViewSupport.class);

    private static final JavaDeadboltAnalyzer ANALYZER = new JavaDeadboltAnalyzer();

    /**
     * Used for restrict tags in the template.
     *
     * @param roles a list of String arrays.  Within an array, the roles are ANDed.  The arrays in the list are OR'd.
     * @return true if the view can be accessed, otherwise false
     */
    public static boolean viewRestrict(final List<String[]> roles,
                                       final DeadboltHandler handler) throws Throwable
    {
        final Subject subject = RequestUtils.getSubject(handler == null ? PluginUtils.getDeadboltHandler()
                                                                        : handler,
                                                        Http.Context.current());

        boolean roleOk = false;
        for (int i = 0; !roleOk && i < roles.size(); i++)
        {
            roleOk = ANALYZER.checkRole(subject,
                                        roles.get(i));
        }

        return roleOk;
    }

    /**
     * Used for dynamic tags in the template.
     *
     * @param name the name of the resource
     * @param meta meta information on the resource
     * @return true if the view can be accessed, otherwise false
     */
    public static boolean viewDynamic(final String name,
                                      final String meta,
                                      final DeadboltHandler handler) throws Throwable
    {
        final Http.Context context = Http.Context.current();
        final DeadboltHandler deadboltHandler = handler == null ? PluginUtils.getDeadboltHandler()
                                                                : handler;
        final DynamicResourceHandler resourceHandler = deadboltHandler.getDynamicResourceHandler(context);
        boolean allowed = false;
        if (resourceHandler == null)
        {
            throw new RuntimeException("A dynamic resource is specified but no dynamic resource handler is provided");
        }
        else
        {
            if (resourceHandler.isAllowed(name,
                                          meta,
                                          deadboltHandler,
                                          context))
            {
                allowed = true;
            }
        }

        return allowed;
    }

    /**
     * Used for subjectPresent tags in the template.
     *
     * @return true if the view can be accessed, otherwise false
     */
    public static boolean viewSubjectPresent(final DeadboltHandler handler) throws Throwable
    {
        return RequestUtils.getSubject(handler == null ? PluginUtils.getDeadboltHandler()
                                                       : handler,
                                       Http.Context.current()) != null;
    }

    /**
     * Used for subjectNotPresent tags in the template.
     *
     * @return true if the view can be accessed, otherwise false
     */
    public static boolean viewSubjectNotPresent(final DeadboltHandler handler) throws Throwable
    {
        return RequestUtils.getSubject(handler == null ? PluginUtils.getDeadboltHandler()
                                                       : handler,
                                       Http.Context.current()) == null;
    }

    public static boolean viewPattern(final String value,
                                      final PatternType patternType,
                                      final DeadboltHandler handler) throws Exception
    {
        final Http.Context context = Http.Context.current();
        final DeadboltHandler deadboltHandler = handler == null ? PluginUtils.getDeadboltHandler()
                                                                : handler;

        final Subject subject = RequestUtils.getSubject(deadboltHandler,
                                                        context);
        final boolean allowed;
        switch (patternType)
        {
            case EQUALITY:
                allowed = ANALYZER.checkPatternEquality(subject,
                                                        value);
                break;
            case REGEX:
                allowed = ANALYZER.checkRegexPattern(subject,
                                                     getPattern(value));
                break;
            case CUSTOM:
                allowed = ANALYZER.checkCustomPattern(deadboltHandler,
                                                      context,
                                                      value);
                break;
            default:
                allowed = false;
                LOGGER.error("Unknown pattern type [{}]",
                             patternType);
        }

        return allowed;
    }

    // todo - this should not be here, it's also used in PatternAction
    public static Pattern getPattern(final String patternValue) throws Exception
    {
        return Cache.getOrElse("Deadbolt." + patternValue,
                               new Callable<Pattern>()
                               {
                                   public Pattern call() throws Exception
                                   {
                                       return Pattern.compile(patternValue);
                                   }
                               },
                               0);
    }
}