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

import be.objectify.deadbolt.java.ConstraintLogic;
import be.objectify.deadbolt.java.ConstraintPoint;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.cache.BeforeAuthCheckCache;
import be.objectify.deadbolt.java.cache.CompositeCache;
import be.objectify.deadbolt.java.composite.Constraint;
import be.objectify.deadbolt.java.models.PatternType;
import be.objectify.deadbolt.java.utils.TriFunction;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Steve Chaloner (steve@objectify.be)
 * @since 2.5.1
 */
@Singleton
public class FilterConstraints
{
    private final ConstraintLogic constraintLogic;
    private final CompositeCache compositeCache;
    private final BeforeAuthCheckCache beforeAuthCheckCache;

    @Inject
    public FilterConstraints(final ConstraintLogic constraintLogic,
                             final CompositeCache compositeCache,
                             final BeforeAuthCheckCache beforeAuthCheckCache)
    {
        this.constraintLogic = constraintLogic;
        this.compositeCache = compositeCache;
        this.beforeAuthCheckCache = beforeAuthCheckCache;
    }

    /**
     * A constraint that requires a subject to be present.
     *
     * @return a function that wraps the constraint
     * @see ConstraintLogic#subjectPresent(Http.RequestHeader, DeadboltHandler, Optional, TriFunction, TriFunction, ConstraintPoint)
     */
    public FilterFunction subjectPresent()
    {
        return subjectPresent(Optional.empty());
    }

    /**
     * A constraint that requires a subject to be present.
     *
     * @param content is passed to {@link DeadboltHandler#onAuthFailure(Http.RequestHeader, Optional)} if the authorization fails
     * @return a function that wraps the constraint
     * @see ConstraintLogic#subjectPresent(Http.RequestHeader, DeadboltHandler, Optional, TriFunction, TriFunction, ConstraintPoint)
     */
    public FilterFunction subjectPresent(final Optional<String> content)
    {
        return (Http.RequestHeader requestHeader,
                DeadboltHandler handler,
                Function<Http.RequestHeader, CompletionStage<Result>> next) ->
                beforeAuthCheckCache.apply(handler, requestHeader, content)
                       .thenCompose(maybePreAuth -> maybePreAuth.map(preAuthResult -> (CompletionStage<Result>) CompletableFuture.completedFuture(preAuthResult))
                                                                .orElseGet(() -> constraintLogic.subjectPresent(context,
                                                                                                                handler,
                                                                                                                content,
                                                                                                                (ctx, hdlr, cntent) -> next.apply(requestHeader),
                                                                                                                (ctx, hdlr, cntent) -> hdlr.onAuthFailure(ctx,
                                                                                                                                                          cntent),
                                                                                                                ConstraintPoint.FILTER)));
    }

    /**
     * A constraint that requires a subject to not be present.
     *
     * @return a function that wraps the constraint
     * @see ConstraintLogic#subjectPresent(Http.RequestHeader, DeadboltHandler, Optional, TriFunction, TriFunction, ConstraintPoint)
     */
    public FilterFunction subjectNotPresent()
    {
        return subjectNotPresent(Optional.empty());
    }

    /**
     * A constraint that requires a subject to not be present.
     *
     * @param content is passed to {@link DeadboltHandler#onAuthFailure(Http.RequestHeader, Optional)} if the authorization fails
     * @return a function that wraps the constraint
     * @see ConstraintLogic#subjectPresent(Http.RequestHeader, DeadboltHandler, Optional, TriFunction, TriFunction, ConstraintPoint)
     */
    public FilterFunction subjectNotPresent(final Optional<String> content)
    {
        return (Http.RequestHeader requestHeader,
                DeadboltHandler handler,
                Function<Http.RequestHeader, CompletionStage<Result>> next) ->
                beforeAuthCheckCache.apply(handler, requestHeader, content)
                       .thenCompose(maybePreAuth -> maybePreAuth.map(preAuthResult -> (CompletionStage<Result>) CompletableFuture.completedFuture(preAuthResult))
                                                                .orElseGet(() -> constraintLogic.subjectNotPresent(context,
                                                                                                                   handler,
                                                                                                                   content,
                                                                                                                   (ctx, hdlr, cntent) -> hdlr.onAuthFailure(context,
                                                                                                                                                             cntent),
                                                                                                                   (ctx, hdlr, cntent) -> next.apply(requestHeader),
                                                                                                                   ConstraintPoint.FILTER)));
    }

    /**
     * A constraint that requires the subject to hold certain roles.
     *
     * @param roleGroups
     * @return a function that wraps the constraint
     * @see ConstraintLogic#restrict(Http.RequestHeader, DeadboltHandler, Optional, Supplier, Function, TriFunction, ConstraintPoint)
     */
    public FilterFunction restrict(final List<String[]> roleGroups)
    {
        return restrict(roleGroups,
                        Optional.empty());
    }

    /**
     * A constraint that requires the subject to hold certain roles.
     *
     * @param roleGroups
     * @param content    is passed to {@link DeadboltHandler#onAuthFailure(Http.RequestHeader, Optional)} if the authorization fails
     * @return a function that wraps the constraint
     * @see ConstraintLogic#restrict(Http.RequestHeader, DeadboltHandler, Optional, Supplier, Function, TriFunction, ConstraintPoint)
     */
    public FilterFunction restrict(final List<String[]> roleGroups,
                                   final Optional<String> content)
    {
        return (Http.RequestHeader requestHeader,
                DeadboltHandler handler,
                Function<Http.RequestHeader, CompletionStage<Result>> next) ->
                beforeAuthCheckCache.apply(handler, requestHeader, content)
                       .thenCompose(maybePreAuth -> maybePreAuth.map(preAuthResult -> (CompletionStage<Result>) CompletableFuture.completedFuture(preAuthResult))
                                                                .orElseGet(() -> constraintLogic.restrict(context,
                                                                                                          handler,
                                                                                                          content,
                                                                                                          () -> roleGroups,
                                                                                                          ctx -> next.apply(requestHeader),
                                                                                                          (ctx, hdlr, cntent) -> hdlr.onAuthFailure(ctx,
                                                                                                                                                    cntent),
                                                                                                          ConstraintPoint.FILTER)));
    }

    /**
     * A constraint that checks the permissions of a subject (if using {@link PatternType#EQUALITY} or {@link PatternType#REGEX}) or
     * {@link be.objectify.deadbolt.java.DynamicResourceHandler#checkPermission(String, Optional, DeadboltHandler, Http.RequestHeader)} (if
     * using {@link PatternType#CUSTOM}).
     *
     * @param value       the constraint value
     * @param patternType the type of pattern matching
     * @return a function that wraps the constraint
     * @see ConstraintLogic#pattern(Http.RequestHeader, DeadboltHandler, Optional, String, PatternType, Optional, boolean, Function, TriFunction, ConstraintPoint)
     */
    public FilterFunction pattern(final String value,
                                  final PatternType patternType)
    {
        return pattern(value,
                       patternType,
                       Optional.empty());
    }

    /**
     * A constraint that checks the permissions of a subject (if using {@link PatternType#EQUALITY} or {@link PatternType#REGEX}) or
     * {@link be.objectify.deadbolt.java.DynamicResourceHandler#checkPermission(String, Optional, DeadboltHandler, Http.RequestHeader)} (if
     * using {@link PatternType#CUSTOM}).
     *
     * @param value       the constraint value
     * @param patternType the type of pattern matching
     * @param meta        additional information passed to {@link be.objectify.deadbolt.java.DynamicResourceHandler#checkPermission(String, Optional, DeadboltHandler, Http.RequestHeader)}
     * @return a function that wraps the constraint
     * @see ConstraintLogic#pattern(Http.RequestHeader, DeadboltHandler, Optional, String, PatternType, Optional, boolean, Function, TriFunction, ConstraintPoint)
     */
    public FilterFunction pattern(final String value,
                                  final PatternType patternType,
                                  final Optional<String> meta)
    {
        return pattern(value,
                       patternType,
                       meta,
                       false,
                       Optional.empty());
    }

    /**
     * A constraint that checks the permissions of a subject (if using {@link PatternType#EQUALITY} or {@link PatternType#REGEX}) or
     * {@link be.objectify.deadbolt.java.DynamicResourceHandler#checkPermission(String, Optional, DeadboltHandler, Http.RequestHeader)} (if
     * using {@link PatternType#CUSTOM}).
     *
     * @param value       the constraint value
     * @param patternType the type of pattern matching
     * @param invert      invert the meaning of the constraint, where a successful match results in authorization failing
     * @return a function that wraps the constraint
     * @see ConstraintLogic#pattern(Http.RequestHeader, DeadboltHandler, Optional, String, PatternType, Optional, boolean, Function, TriFunction, ConstraintPoint)
     */
    public FilterFunction pattern(final String value,
                                  final PatternType patternType,
                                  final boolean invert)
    {
        return pattern(value,
                       patternType,
                       Optional.empty(),
                       invert,
                       Optional.empty());
    }

    /**
     * A constraint that checks the permissions of a subject (if using {@link PatternType#EQUALITY} or {@link PatternType#REGEX}) or
     * {@link be.objectify.deadbolt.java.DynamicResourceHandler#checkPermission(String, Optional, DeadboltHandler, Http.RequestHeader)} (if
     * using {@link PatternType#CUSTOM}).
     *
     * @param value       the constraint value
     * @param patternType the type of pattern matching
     * @param meta        additional information passed to {@link be.objectify.deadbolt.java.DynamicResourceHandler#checkPermission(String, Optional, DeadboltHandler, Http.RequestHeader)}
     * @param invert      invert the meaning of the constraint, where a successful match results in authorization failing
     * @param content     is passed to {@link DeadboltHandler#onAuthFailure(Http.RequestHeader, Optional)} if the authorization fails
     * @return a function that wraps the constraint
     * @see ConstraintLogic#pattern(Http.RequestHeader, DeadboltHandler, Optional, String, PatternType, Optional, boolean, Function, TriFunction, ConstraintPoint)
     */
    public FilterFunction pattern(final String value,
                                  final PatternType patternType,
                                  final Optional<String> meta,
                                  final boolean invert,
                                  final Optional<String> content)
    {
        return (Http.RequestHeader requestHeader,
                DeadboltHandler handler,
                Function<Http.RequestHeader, CompletionStage<Result>> next) ->
                beforeAuthCheckCache.apply(handler, requestHeader, content)
                       .thenCompose(maybePreAuth -> maybePreAuth.map(preAuthResult -> (CompletionStage<Result>) CompletableFuture.completedFuture(preAuthResult))
                                                                .orElseGet(() -> constraintLogic.pattern(context,
                                                                                                         handler,
                                                                                                         content,
                                                                                                         value,
                                                                                                         patternType,
                                                                                                         meta,
                                                                                                         invert,
                                                                                                         ctx -> next.apply(requestHeader),
                                                                                                         (ctx, hdlr, cntent) -> hdlr.onAuthFailure(ctx,
                                                                                                                                                   cntent),
                                                                                                         ConstraintPoint.FILTER)));
    }

    /**
     * An arbitrary constraint that uses {@link be.objectify.deadbolt.java.DynamicResourceHandler#isAllowed(String, Optional, DeadboltHandler, Http.RequestHeader)}
     * to determine access.
     *
     * @param name the name of the constraint
     * @return a function that wraps the constraint
     * @see ConstraintLogic#dynamic(Http.RequestHeader, DeadboltHandler, Optional, String, Optional, Function, TriFunction, ConstraintPoint)
     */
    public FilterFunction dynamic(final String name)
    {
        return dynamic(name,
                       Optional.empty());
    }

    /**
     * An arbitrary constraint that uses {@link be.objectify.deadbolt.java.DynamicResourceHandler#isAllowed(String, Optional, DeadboltHandler, Http.RequestHeader)}
     * to determine access.
     *
     * @param name the name of the constraint
     * @param meta additional information passed to {@link be.objectify.deadbolt.java.DynamicResourceHandler#isAllowed(String, Optional, DeadboltHandler, Http.RequestHeader)}
     * @return a function that wraps the constraint
     * @see ConstraintLogic#dynamic(Http.RequestHeader, DeadboltHandler, Optional, String, Optional, Function, TriFunction, ConstraintPoint)
     */
    public FilterFunction dynamic(final String name,
                                  final Optional<String> meta)
    {
        return dynamic(name,
                       meta,
                       Optional.empty());
    }

    /**
     * An arbitrary constraint that uses {@link be.objectify.deadbolt.java.DynamicResourceHandler#isAllowed(String, Optional, DeadboltHandler, Http.RequestHeader)}
     * to determine access.
     *
     * @param name    the name of the constraint
     * @param meta    additional information passed to {@link be.objectify.deadbolt.java.DynamicResourceHandler#isAllowed(String, Optional, DeadboltHandler, Http.RequestHeader)}
     * @param content is passed to {@link DeadboltHandler#onAuthFailure(Http.RequestHeader, Optional)} if the authorization fails
     * @return a function that wraps the constraint
     * @see ConstraintLogic#dynamic(Http.RequestHeader, DeadboltHandler, Optional, String, Optional, Function, TriFunction, ConstraintPoint)
     */
    public FilterFunction dynamic(final String name,
                                  final Optional<String> meta,
                                  final Optional<String> content)
    {
        return (Http.RequestHeader requestHeader,
                DeadboltHandler handler,
                Function<Http.RequestHeader, CompletionStage<Result>> next) ->
                beforeAuthCheckCache.apply(handler, requestHeader, content)
                       .thenCompose(maybePreAuth -> maybePreAuth.map(preAuthResult -> (CompletionStage<Result>) CompletableFuture.completedFuture(preAuthResult))
                                                                .orElseGet(() -> constraintLogic.dynamic(context,
                                                                                                         handler,
                                                                                                         content,
                                                                                                         name,
                                                                                                         meta,
                                                                                                              ctx -> next.apply(requestHeader),
                                                                                                         (ctx, hdlr, cntent) -> hdlr.onAuthFailure(ctx,
                                                                                                                                                   cntent),
                                                                                                         ConstraintPoint.FILTER)));
    }

    /**
     * A constraint that uses a {@link Constraint} to determine access.  This may be an arbitrarily complex
     * tree of constraints.
     *
     * @param name the name of the composite constraint defined in {@link CompositeCache}.
     * @return a function that wraps the constraint
     * @throws IllegalStateException if no constraint with the given name is present in the composite cache
     */
    public FilterFunction composite(final String name)
    {
        return composite(name,
                         Optional.empty());
    }

    /**
     * A constraint that uses a {@link Constraint} to determine access.  This may be an arbitrarily complex
     * tree of constraints.
     *
     * @param name    the name of the composite constraint defined in {@link CompositeCache}.
     * @param content is passed to {@link DeadboltHandler#onAuthFailure(Http.RequestHeader, Optional)} if the authorization fails
     * @return a function that wraps the constraint
     * @throws IllegalStateException if no constraint with the given name is present in the composite cache
     */
    public FilterFunction composite(final String name,
                                    final Optional<String> content)
    {
        return compositeCache.apply(name).map(constraint -> composite(constraint,
                                                                      content))
                             .orElseThrow(() -> new IllegalStateException(String.format("No constraint with name [%s] found",
                                                                                        name)));
    }

    /**
     * A constraint that uses a {@link Constraint} to determine access.  This may be an arbitrarily complex
     * tree of constraints.
     *
     * @param constraint the composite constraint
     * @return a function that wraps the constraint
     */
    public FilterFunction composite(final Constraint constraint)
    {
        return composite(constraint,
                         Optional.empty());
    }

    /**
     * A constraint that uses a {@link Constraint} to determine access.  This may be an arbitrarily complex
     * tree of constraints.
     *
     * @param constraint the composite constraint
     * @param content    is passed to {@link DeadboltHandler#onAuthFailure(Http.RequestHeader, Optional)} if the authorization fails
     * @return a function that wraps the constraint
     */
    public FilterFunction composite(final Constraint constraint,
                                    final Optional<String> content)
    {
        return (Http.RequestHeader requestHeader,
                DeadboltHandler handler,
                Function<Http.RequestHeader, CompletionStage<Result>> next) ->
                beforeAuthCheckCache.apply(handler, requestHeader, content)
                       .thenCompose(maybePreAuth -> maybePreAuth.map(preAuthResult -> (CompletionStage<Result>) CompletableFuture.completedFuture(preAuthResult))
                                                                .orElseGet(() -> constraint.test(context,
                                                                                                 handler)
                                                                                           .thenCompose(allowed -> allowed ? ((Supplier<CompletionStage<Result>>) () -> {
                                                                                               handler.onAuthSuccess(context,
                                                                                                                     "composite",
                                                                                                                     ConstraintPoint.FILTER);
                                                                                               return next.apply(requestHeader);
                                                                                           }).get()
                                                                                                                           : handler.onAuthFailure(context,
                                                                                                                                                   content))));
    }

    public FilterFunction roleBasedPermissions(final String roleName)
    {
        return roleBasedPermissions(roleName,
                                    Optional.empty());
    }

    public FilterFunction roleBasedPermissions(final String roleName,
                                               final Optional<String> content)
    {
        return (Http.RequestHeader requestHeader,
                DeadboltHandler handler,
                Function<Http.RequestHeader, CompletionStage<Result>> next) ->
                beforeAuthCheckCache.apply(handler, requestHeader, content)
                       .thenCompose(maybePreAuth -> maybePreAuth.map(preAuthResult -> (CompletionStage<Result>) CompletableFuture.completedFuture(preAuthResult))
                                                                .orElseGet(() -> constraintLogic.roleBasedPermissions(context,
                                                                                                                      handler,
                                                                                                                      content,
                                                                                                                      roleName,
                                                                                                                      ctx -> next.apply(requestHeader),
                                                                                                                      (ctx, hdlr, cntent) -> hdlr.onAuthFailure(ctx,
                                                                                                                                                                cntent),
                                                                                                                      ConstraintPoint.FILTER)));
    }
}
