/*
 * Copyright 2010-2017 Steve Chaloner
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

import be.objectify.deadbolt.java.cache.PatternCache;
import be.objectify.deadbolt.java.cache.SubjectCache;
import be.objectify.deadbolt.java.models.PatternType;
import be.objectify.deadbolt.java.models.Subject;
import be.objectify.deadbolt.java.utils.TriFunction;
import play.mvc.Http;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The logic behind the constraints.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
@Singleton
public class ConstraintLogic
{
    private final DeadboltAnalyzer analyzer;

    private final SubjectCache subjectCache;

    private final PatternCache patternCache;

    @Inject
    public ConstraintLogic(final DeadboltAnalyzer analyzer,
                           final SubjectCache subjectCache,
                           final PatternCache patternCache)
    {
        this.analyzer = analyzer;
        this.subjectCache = subjectCache;
        this.patternCache = patternCache;
    }

    public <T> CompletionStage<T> subjectPresent(final Http.Context ctx,
                                                 final DeadboltHandler deadboltHandler,
                                                 final Optional<String> content,
                                                 final TriFunction<Http.Context, DeadboltHandler, Optional<String>, CompletionStage<T>> present,
                                                 final TriFunction<Http.Context, DeadboltHandler, Optional<String>, CompletionStage<T>> notPresent,
                                                 final ConstraintPoint constraintPoint)
    {
        return subjectTest(ctx,
                           deadboltHandler,
                           content,
                           (context, handler, cnt) ->
                           {
                               handler.onAuthSuccess(context,
                                                     "subjectPresent",
                                                     constraintPoint);
                               return present.apply(context,
                                                    handler,
                                                    cnt);
                           },
                           notPresent);
    }

    public <T> CompletionStage<T> subjectNotPresent(final Http.Context ctx,
                                                    final DeadboltHandler deadboltHandler,
                                                    final Optional<String> content,
                                                    final TriFunction<Http.Context, DeadboltHandler, Optional<String>, CompletionStage<T>> present,
                                                    final TriFunction<Http.Context, DeadboltHandler, Optional<String>, CompletionStage<T>> notPresent,
                                                    final ConstraintPoint constraintPoint)
    {
        return subjectTest(ctx,
                           deadboltHandler,
                           content,
                           present,
                           (context, handler, cnt) ->
                           {
                               handler.onAuthSuccess(context,
                                                     "subjectNotPresent",
                                                     constraintPoint);
                               return notPresent.apply(context,
                                                    handler,
                                                    cnt);
                           });
    }

    private <T> CompletionStage<T> subjectTest(final Http.Context ctx,
                                               final DeadboltHandler deadboltHandler,
                                               final Optional<String> content,
                                               final TriFunction<Http.Context, DeadboltHandler, Optional<String>, CompletionStage<T>> present,
                                               final TriFunction<Http.Context, DeadboltHandler, Optional<String>, CompletionStage<T>> notPresent)
    {
        return getSubject(ctx,
                          deadboltHandler)
                .thenCompose(maybeSubject -> maybeSubject.map(subject -> present.apply(ctx,
                                                                                       deadboltHandler,
                                                                                       content))
                                                         .orElseGet(() -> notPresent.apply(ctx,
                                                                                           deadboltHandler,
                                                                                           content)));
    }

    public <T> CompletionStage<T> restrict(final Http.Context ctx,
                                           final DeadboltHandler deadboltHandler,
                                           final Optional<String> content,
                                           final Supplier<List<String[]>> roleGroupSupplier,
                                           final Function<Http.Context, CompletionStage<T>> pass,
                                           final TriFunction<Http.Context, DeadboltHandler, Optional<String>, CompletionStage<T>> fail,
                                           final ConstraintPoint constraintPoint)
    {
        return getSubject(ctx,
                          deadboltHandler)
                .thenCompose(subjectOption ->
                                  {
                                      boolean roleOk = false;
                                      if (subjectOption.isPresent())
                                      {
                                          final List<String[]> roleGroups = roleGroupSupplier.get();
                                          for (int i = 0; !roleOk && i < roleGroups.size(); i++)
                                          {
                                              roleOk = analyzer.checkRole(subjectOption,
                                                                          roleGroups.get(i));
                                          }
                                      }
                                      return roleOk ? pass(ctx,
                                                           deadboltHandler,
                                                           pass,
                                                           constraintPoint,
                                                           "restrict")
                                                    : fail.apply(ctx,
                                                                 deadboltHandler,
                                                                 content);
                                  });
    }

    public <T> CompletionStage<T> roleBasedPermissions(final Http.Context ctx,
                                                       final DeadboltHandler deadboltHandler,
                                                       final Optional<String> content,
                                                       final String roleName,
                                                       final Function<Http.Context, CompletionStage<T>> pass,
                                                       final TriFunction<Http.Context, DeadboltHandler, Optional<String>, CompletionStage<T>> fail,
                                                       final ConstraintPoint constraintPoint)
    {
        return getSubject(ctx,
                          deadboltHandler)
                .thenCompose(maybeSubject -> maybeSubject.isPresent() ? deadboltHandler.getPermissionsForRole(roleName)
                                                                                       .thenApply(permissions -> permissions.stream()
                                                                                                                            .map(permission -> Optional.ofNullable(patternCache.apply(permission.getValue())))
                                                                                                                            .map(maybePattern -> analyzer.checkRegexPattern(maybeSubject,
                                                                                                                                                                                      maybePattern))
                                                                                                                            .filter(matches -> matches)
                                                                                                                            .findFirst()
                                                                                                                            .isPresent())

                                                                      : CompletableFuture.completedFuture(false))
                .thenCompose(allowed -> allowed ? pass(ctx,
                                                            deadboltHandler,
                                                            pass,
                                                            constraintPoint,
                                                            "roleBasedPermissions")
                                                     : fail.apply(ctx,
                                                                  deadboltHandler,
                                                                  content));

    }

    public <T> CompletionStage<T> pattern(final Http.Context ctx,
                                          final DeadboltHandler deadboltHandler,
                                          final Optional<String> content,
                                          final String value,
                                          final PatternType patternType,
                                          final Optional<String> meta,
                                          final boolean invert,
                                          final Function<Http.Context, CompletionStage<T>> pass,
                                          final TriFunction<Http.Context, DeadboltHandler, Optional<String>, CompletionStage<T>> fail,
                                          final ConstraintPoint constraintPoint)
    {
        final CompletionStage<T> result;

        switch (patternType)
        {
            case EQUALITY:
                result = equality(ctx,
                                  deadboltHandler,
                                  content,
                                  value,
                                  invert,
                                  pass,
                                  fail,
                                  constraintPoint);
                break;
            case REGEX:
                result = regex(ctx,
                               deadboltHandler,
                               content,
                               value,
                               invert,
                               pass,
                               fail,
                               constraintPoint);
                break;
            case CUSTOM:
                result = custom(ctx,
                                deadboltHandler,
                                content,
                                value,
                                meta,
                                invert,
                                pass,
                                fail,
                                constraintPoint);
                break;
            default:
                throw new RuntimeException("Unknown pattern type: " + patternType);
        }

        return result;
    }

    public <T> CompletionStage<T> dynamic(final Http.Context ctx,
                                          final DeadboltHandler deadboltHandler,
                                          final Optional<String> content,
                                          final String name,
                                          final Optional<String> meta,
                                          final Function<Http.Context, CompletionStage<T>> pass,
                                          final TriFunction<Http.Context, DeadboltHandler, Optional<String>, CompletionStage<T>> fail,
                                          final ConstraintPoint constraintPoint)
    {
        return deadboltHandler.getDynamicResourceHandler(ctx)
                              .thenApply(option -> option.orElseGet(() -> ExceptionThrowingDynamicResourceHandler.INSTANCE))
                              .thenCompose(drh -> drh.isAllowed(name,
                                                                meta,
                                                                deadboltHandler,
                                                                ctx))
                              .thenCompose(allowed -> allowed ? pass(ctx,
                                                                          deadboltHandler,
                                                                          pass,
                                                                          constraintPoint,
                                                                          "dynamic")
                                                                   : fail.apply(ctx,
                                                                                deadboltHandler,
                                                                                content));
    }

    private <T> CompletionStage<T> custom(final Http.Context ctx,
                                          final DeadboltHandler deadboltHandler,
                                          final Optional<String> content,
                                          final String value,
                                          final Optional<String> meta,
                                          final boolean invert,
                                          final Function<Http.Context, CompletionStage<T>> pass,
                                          final TriFunction<Http.Context, DeadboltHandler, Optional<String>, CompletionStage<T>> fail,
                                          final ConstraintPoint constraintPoint)
    {
        ctx.args.put(ConfigKeys.PATTERN_INVERT,
                     invert);
        return deadboltHandler.getDynamicResourceHandler(ctx)
                              .thenApply(option -> option.orElseGet(() -> ExceptionThrowingDynamicResourceHandler.INSTANCE))
                              .thenCompose(resourceHandler -> resourceHandler.checkPermission(value,
                                                                                              meta,
                                                                                              deadboltHandler,
                                                                                              ctx))
                              .thenCompose(allowed -> (invert ? !allowed : allowed) ? pass(ctx,
                                                                                                deadboltHandler,
                                                                                                pass,
                                                                                                constraintPoint,
                                                                                                "pattern - custom")
                                                                                         : fail.apply(ctx,
                                                                                                      deadboltHandler,
                                                                                                      content));
    }

    private <T> CompletionStage<T> equality(final Http.Context ctx,
                                            final DeadboltHandler deadboltHandler,
                                            final Optional<String> content,
                                            final String value,
                                            final boolean invert,
                                            final Function<Http.Context, CompletionStage<T>> pass,
                                            final TriFunction<Http.Context, DeadboltHandler, Optional<String>, CompletionStage<T>> fail,
                                            final ConstraintPoint constraintPoint)
    {
        return getSubject(ctx,
                          deadboltHandler)
                .thenCompose(subject -> {
                                      final boolean equal = subject.isPresent() ? analyzer.checkPatternEquality(subject,
                                                                                                                Optional.ofNullable(value))
                                                                                : invert; // this is a little clumsy - it means no subject + invert is still denied
                                      return (invert ? !equal : equal) ? pass(ctx,
                                                                              deadboltHandler,
                                                                              pass,
                                                                              constraintPoint,
                                                                              "pattern - equality")
                                                                       : fail.apply(ctx,
                                                                                    deadboltHandler,
                                                                                    content);
                });
    }

    protected CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context ctx,
                                                                      final DeadboltHandler deadboltHandler)
    {
        return subjectCache.apply(deadboltHandler,
                                  ctx);
    }

    /**
     * Checks access to the resource based on the regex
     *
     * @param ctx             the HTTP context
     * @param deadboltHandler the Deadbolt handler
     * @param invert          if true, invert the application of the constraint
     * @return the necessary result
     */
    private <T> CompletionStage<T> regex(final Http.Context ctx,
                                         final DeadboltHandler deadboltHandler,
                                         final Optional<String> content,
                                         final String value,
                                         final boolean invert,
                                         final Function<Http.Context, CompletionStage<T>> pass,
                                         final TriFunction<Http.Context, DeadboltHandler, Optional<String>, CompletionStage<T>> fail,
                                         final ConstraintPoint constraintPoint)
    {
        return CompletableFuture.completedFuture(patternCache.apply(value))
                                .thenCombine(getSubject(ctx,
                                                             deadboltHandler),
                                             (patternValue, subject) ->
                                                     subject.isPresent() ? analyzer.checkRegexPattern(subject,
                                                                                                      Optional.ofNullable(patternValue))
                                                                         : invert) // this is a little clumsy - it means no subject + invert is still denied

                                .thenCompose(hasPassed -> (invert ? !hasPassed : hasPassed) ? pass(ctx,
                                                                                                   deadboltHandler,
                                                                                                   pass,
                                                                                                   constraintPoint,
                                                                                                   "pattern - regex")
                                                                                            : fail.apply(ctx,
                                                                                                         deadboltHandler,
                                                                                                         content));
    }

    private <T> CompletionStage<T> pass(final Http.Context context,
                                        final DeadboltHandler handler,
                                        final Function<Http.Context, CompletionStage<T>> pass,
                                        final ConstraintPoint constraintPoint,
                                        final String constraintType)
    {
        handler.onAuthSuccess(context,
                              constraintType,
                              constraintPoint);
        return pass.apply(context);
    }
}
