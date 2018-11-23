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
package be.objectify.deadbolt.java.filters;

import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;
import akka.stream.Materializer;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.cache.HandlerCache;
import be.objectify.deadbolt.java.models.PatternType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.routing.HandlerDef;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;
import play.routing.Router;

/**
 * Filters all incoming HTTP requests and applies constraints based on the route's comment.  If a comment is present, the constraint
 * for that route will be applied.  If access is allowed, the next filter in the chain is invoked; if access is not allowed,
 * {@link DeadboltHandler#onAuthFailure(Http.RequestHeader, Optional)} is invoked.
 * <p>
 * The format of the comment is deadbolt:constraintType:config.  Individual configurations have the form :label[value] - to omit an optional config,
 * remove :label[value],
 * <p>
 * <ul>
 * <li>deadbolt:subjectPresent:handler[handler name]
 * <ul>
 * <li>handler - optional.  The name of a handler in the HandlerCache</li>
 * </ul>
 * </li>
 * <li>deadbolt:subjectNotPresent:handler[handler name]
 * <ul>
 * <li>name - required.  This is the name passed to DeadboltHandler#isAllowed</li>
 * </ul>
 * </li>
 * <li>deadbolt:dynamic:name[constraint name]:handler[handler name]
 * <ul>
 * <li>name - required.  This is the name passed to DeadboltHandler#isAllowed</li>
 * <li>handler - optional.  The name of a handler in the HandlerCache</li>
 * </ul>
 * </li>
 * <li>deadbolt:pattern:value[constraint value]:type[EQUALITY|REGEX|CUSTOM]:invert[true|false]:handler[handler name]
 * <ul>
 * <li>value - required.  Used to test the permissions of a subject.</li>
 * <li>type - required.  The pattern type, case sensitive.</li>
 * <li>invert - optional.  Defines if the result should be flipped, i.e. a matching permission mean unauthorized.  Defaults to false.</li>
 * <li>handler - optional.  The name of a handler in the HandlerCache</li>
 * </ul>
 * </li>
 * <li>deadbolt:composite:name[constraint name]:handler[handler name]
 * <ul>
 * <li>name - required.  The name of a constraint in CompositeCache.</li>
 * <li>handler - optional.  The name of a handler in the HandlerCache</li>
 * </ul>
 * </li>
 * <li>deadbolt:restrict:name[constraint name]:handler[handler name]
 * <ul>
 * <li>name - required.  The name of a constraint in the CompositeCache</li>
 * <li>handler - optional.  The name of a handler in the HandlerCache</li>
 * </ul>
 * </li>
 * <li>deadbolt:rbp:name[role name]:handler[handler name]
 * <ul>
 * <li>name - required.  The name of a role, which will be used to resolve permissions from {@link DeadboltHandler#getPermissionsForRole(String)}</li>
 * <li>handler - optional.  The name of a handler in the HandlerCache</li>
 * </ul>
 * </li>
 * </ul>
 * <p>
 * Restrict is a tricky one, because the possible combinations of roles leads to a nightmare to parse.  Instead, define your role constraints within the
 * composite cache and use the named constraint instead.  deadbolt:restrict is actually a synonym for deadbolt:composite.
 *
 * @author Steve Chaloner (steve@objectify.be)
 * @since 2.5.1
 */
@Singleton
public class DeadboltRouteCommentFilter extends AbstractDeadboltFilter
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DeadboltRouteCommentFilter.class);

    final Pattern subjectPresentComment = Pattern.compile("deadbolt\\:(subjectPresent)(?:\\:content\\[(?<content>.+?)\\]){0,1}(?:\\:handler\\[(?<handler>.+?)\\]){0,1}");
    final Pattern subjectNotPresentComment = Pattern.compile("deadbolt\\:(subjectNotPresent)(?:\\:content\\[(?<content>.+?)\\]){0,1}(?:\\:handler\\[(?<handler>.+?)\\]){0,1}");
    final Pattern dynamicComment = Pattern.compile("deadbolt\\:(dynamic)\\:name\\[(?<name>.+?)\\](?:\\:meta\\[(?<meta>.+?)\\]){0,1}(?:\\:content\\[(?<content>.+?)\\]){0,1}(?:\\:handler\\[(?<handler>.+?)\\]){0,1}");
    final Pattern patternComment = Pattern.compile("deadbolt\\:(pattern)\\:value\\[(?<value>.+?)\\]\\:type\\[(?<type>EQUALITY|REGEX|CUSTOM)\\](?:\\:meta\\[(?<meta>.+?)\\]){0,1}(?:\\:invert\\[(?<invert>true|false)\\]){0,1}(?:\\:content\\[(?<content>.+?)\\]){0,1}(?:\\:handler\\[(?<handler>.+?)\\]){0,1}");
    final Pattern compositeComment = Pattern.compile("deadbolt\\:(composite)\\:name\\[(?<name>.+?)\\](?:\\:content\\[(?<content>.+?)\\]){0,1}(?:\\:handler\\[(?<handler>.+?)\\]){0,1}");
    final Pattern restrictComment = Pattern.compile("deadbolt\\:(restrict)\\:name\\[(?<name>.+?)\\](?:\\:content\\[(?<content>.+?)\\]){0,1}(?:\\:handler\\[(?<handler>.+?)\\]){0,1}");
    final Pattern roleBasedPermissionsComment = Pattern.compile("deadbolt\\:(rbp)\\:name\\[(?<name>.+?)\\](?:\\:content\\[(?<content>.+?)\\]){0,1}(?:\\:handler\\[(?<handler>.+?)\\]){0,1}");

    private final HandlerCache handlerCache;
    private final DeadboltHandler handler;
    private final FilterConstraints filterConstraints;

    private final F.Tuple<FilterFunction, DeadboltHandler> unknownDeadboltComment;

    @Inject
    public DeadboltRouteCommentFilter(final Materializer mat,
                                      final HandlerCache handlerCache,
                                      final FilterConstraints filterConstraints)
    {
        super(mat);
        this.handlerCache = handlerCache;
        this.handler = handlerCache.get();
        this.filterConstraints = filterConstraints;

        this.unknownDeadboltComment = new F.Tuple<>((requestHeader, dh, onSuccess) ->
                                                    {
                                                        LOGGER.error("Unknown Deadbolt route comment [{}], denying access with default handler",
                                                                     requestHeader.attrs().get(Router.Attrs.HANDLER_DEF).comments());
                                                        return dh.onAuthFailure(requestHeader, Optional.empty());
                                                    }, handler);
    }

    /**
     * If a constraint is defined for a given route, test that constraint before allowing the request to proceed.
     *
     * @param next          the next step in the filter chain
     * @param requestHeader the request header
     * @return a future for the result
     */
    @Override
    public CompletionStage<Result> apply(final Function<Http.RequestHeader, CompletionStage<Result>> next,
                                         final Http.RequestHeader requestHeader)
    {
        final HandlerDef handlerDef = requestHeader.attrs().get(Router.Attrs.HANDLER_DEF);
        final CompletionStage<Result> result;
        final String comment = handlerDef.comments();
        if (comment != null && comment.startsWith("deadbolt:"))
        {
            // this is horrible
            final F.Tuple<FilterFunction, DeadboltHandler> tuple = subjectPresent(comment).orElseGet(() -> subjectNotPresent(comment)
                    .orElseGet(() -> dynamic(comment)
                            .orElseGet(() -> composite(comment)
                                    .orElseGet(() -> restrict(comment)
                                            .orElseGet(() -> pattern(comment)
                                                    .orElseGet(() -> roleBasedPermissionsComment(comment)
                                                            .orElse(unknownDeadboltComment)))))));
            result = tuple._1.apply(requestHeader,
                                    tuple._2,
                                    next);
        }
        else
        {
            result = next.apply(requestHeader);
        }
        return result;
    }

    private Optional<F.Tuple<FilterFunction, DeadboltHandler>> subjectPresent(final String comment)
    {
        final Matcher matcher = subjectPresentComment.matcher(comment);
        return matcher.matches() ? Optional.of(new F.Tuple<>(filterConstraints.subjectPresent(Optional.ofNullable(matcher.group("content"))),
                                                             handler(matcher)))
                                 : Optional.empty();
    }

    private Optional<F.Tuple<FilterFunction, DeadboltHandler>> subjectNotPresent(final String comment)
    {
        final Matcher matcher = subjectNotPresentComment.matcher(comment);
        return matcher.matches() ? Optional.of(new F.Tuple<>(filterConstraints.subjectNotPresent(Optional.ofNullable(matcher.group("content"))),
                                                             handler(matcher)))
                                 : Optional.empty();
    }

    private Optional<F.Tuple<FilterFunction, DeadboltHandler>> dynamic(final String comment)
    {
        final Matcher matcher = dynamicComment.matcher(comment);
        return matcher.matches() ? Optional.of(new F.Tuple<>(filterConstraints.dynamic(matcher.group("name"),
                                                                                       Optional.ofNullable(matcher.group("meta")),
                                                                                       Optional.ofNullable(matcher.group("content"))),
                                                             handler(matcher)))
                                 : Optional.empty();
    }

    private Optional<F.Tuple<FilterFunction, DeadboltHandler>> composite(final String comment)
    {
        final Matcher matcher = compositeComment.matcher(comment);
        return matcher.matches() ? Optional.of(new F.Tuple<>(filterConstraints.composite(matcher.group("name"),
                                                                                         Optional.ofNullable(matcher.group("content"))),
                                                             handler(matcher)))
                                 : Optional.empty();
    }

    private Optional<F.Tuple<FilterFunction, DeadboltHandler>> restrict(final String comment)
    {
        final Matcher matcher = restrictComment.matcher(comment);
        return matcher.matches() ? Optional.of(new F.Tuple<>(filterConstraints.composite(matcher.group("name"),
                                                                                         Optional.ofNullable(matcher.group("content"))),
                                                             handler(matcher)))
                                 : Optional.empty();
    }

    private Optional<F.Tuple<FilterFunction, DeadboltHandler>> roleBasedPermissionsComment(final String comment)
    {
        final Matcher matcher = roleBasedPermissionsComment.matcher(comment);
        return matcher.matches() ? Optional.of(new F.Tuple<>(filterConstraints.roleBasedPermissions(matcher.group("name"),
                                                                                                    Optional.ofNullable(matcher.group("content"))),
                                                             handler(matcher)))
                                 : Optional.empty();
    }

    private Optional<F.Tuple<FilterFunction, DeadboltHandler>> pattern(final String comment)
    {
        final Matcher matcher = patternComment.matcher(comment);
        if (matcher.matches())
        {
            final String invertStr = matcher.group("invert");
            final boolean invert = invertStr != null && Boolean.parseBoolean(invertStr);
            return Optional.of(new F.Tuple<>(filterConstraints.pattern(matcher.group("value"),
                                                                       PatternType.valueOf(matcher.group("type")),
                                                                       Optional.ofNullable(matcher.group("meta")),
                                                                       invert,
                                                                       Optional.ofNullable(matcher.group("content"))),
                                             handler(matcher)));
        }
        else
        {
            return Optional.empty();
        }
    }

    private DeadboltHandler handler(final Matcher matcher)
    {
        final String namedHandler = matcher.group("handler");
        return namedHandler == null ? handler
                                    : handlerCache.apply(namedHandler);
    }
}
