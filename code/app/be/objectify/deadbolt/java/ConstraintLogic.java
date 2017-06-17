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
package be.objectify.deadbolt.java;

import be.objectify.deadbolt.java.cache.PatternCache;
import be.objectify.deadbolt.java.cache.SubjectCache;
import be.objectify.deadbolt.java.models.PatternType;
import be.objectify.deadbolt.java.models.Subject;
import be.objectify.deadbolt.java.utils.TriFunction;
import javax.inject.Singleton;
import play.libs.concurrent.HttpExecution;
import play.mvc.Http;
import scala.concurrent.ExecutionContext;
import scala.concurrent.ExecutionContextExecutor;

import javax.inject.Inject;
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

    private final DeadboltExecutionContextProvider executionContextProvider;

    @Inject
    public ConstraintLogic(final DeadboltAnalyzer analyzer,
                           final SubjectCache subjectCache,
                           final PatternCache patternCache,
                           final ExecutionContextProvider ecProvider)
    {
        this.analyzer = analyzer;
        this.subjectCache = subjectCache;
        this.patternCache = patternCache;
        this.executionContextProvider = ecProvider.get();
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
        final ExecutionContextExecutor executor = executor();
        return getSubject(ctx,
                          deadboltHandler)
                .thenComposeAsync(maybeSubject -> maybeSubject.map(subject -> present.apply(ctx,
                                                                                            deadboltHandler,
                                                                                            content))
                                                              .orElseGet(() -> notPresent.apply(ctx,
                                                                                                deadboltHandler,
                                                                                                content)),
                                  executor);
    }

    public <T> CompletionStage<T> restrict(final Http.Context ctx,
                                           final DeadboltHandler deadboltHandler,
                                           final Optional<String> content,
                                           final Supplier<List<String[]>> roleGroupSupplier,
                                           final Function<Http.Context, CompletionStage<T>> pass,
                                           final TriFunction<Http.Context, DeadboltHandler, Optional<String>, CompletionStage<T>> fail,
                                           final ConstraintPoint constraintPoint)
    {
        final ExecutionContextExecutor executor = executor();
        return getSubject(ctx,
                          deadboltHandler)
                .thenApplyAsync(subjectOption ->
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
                                    return roleOk;
                                },
                                executor)
                .thenComposeAsync(allowed -> allowed ? pass(ctx,
                                                            deadboltHandler,
                                                            pass,
                                                            constraintPoint,
                                                            "restrict")
                                                     : fail.apply(ctx,
                                                                  deadboltHandler,
                                                                  content),
                                  executor);

    }

    public <T> CompletionStage<T> roleBasedPermissions(final Http.Context ctx,
                                                       final DeadboltHandler deadboltHandler,
                                                       final Optional<String> content,
                                                       final String roleName,
                                                       final Function<Http.Context, CompletionStage<T>> pass,
                                                       final TriFunction<Http.Context, DeadboltHandler, Optional<String>, CompletionStage<T>> fail,
                                                       final ConstraintPoint constraintPoint)
    {
        final ExecutionContextExecutor executor = executor();
        return getSubject(ctx,
                          deadboltHandler)
                .thenComposeAsync(maybeSubject -> maybeSubject.isPresent() ? deadboltHandler.getPermissionsForRole(roleName)
                                                                                            .thenApplyAsync(permissions -> permissions.stream()
                                                                                                                                      .map(permission -> Optional.ofNullable(patternCache.apply(permission.getValue())))
                                                                                                                                      .map(maybePattern -> analyzer.checkRegexPattern(maybeSubject,
                                                                                                                                                                                      maybePattern))
                                                                                                                                      .filter(matches -> matches)
                                                                                                                                      .findFirst()
                                                                                                                                      .isPresent(),
                                                                                                            executor)

                                                                           : CompletableFuture.completedFuture(false),
                                  executor)
                .thenComposeAsync(allowed -> allowed ? pass(ctx,
                                                            deadboltHandler,
                                                            pass,
                                                            constraintPoint,
                                                            "roleBasedPermissions")
                                                     : fail.apply(ctx,
                                                                  deadboltHandler,
                                                                  content),
                                  executor);

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
        final ExecutionContextExecutor executor = executor();
        return deadboltHandler.getDynamicResourceHandler(ctx)
                              .thenApplyAsync(option -> option.orElseGet(() -> ExceptionThrowingDynamicResourceHandler.INSTANCE),
                                              executor)
                              .thenComposeAsync(drh -> drh.isAllowed(name,
                                                                     meta,
                                                                     deadboltHandler,
                                                                     ctx),
                                                executor)
                              .thenComposeAsync(allowed -> allowed ? pass(ctx,
                                                                          deadboltHandler,
                                                                          pass,
                                                                          constraintPoint,
                                                                          "dynamic")
                                                                   : fail.apply(ctx,
                                                                                deadboltHandler,
                                                                                content),
                                                executor);
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
        final ExecutionContextExecutor executor = executor();
        return deadboltHandler.getDynamicResourceHandler(ctx)
                              .thenApplyAsync(option -> option.orElseGet(() -> ExceptionThrowingDynamicResourceHandler.INSTANCE),
                                              executor)
                              .thenComposeAsync(resourceHandler -> resourceHandler.checkPermission(value,
                                                                                                   meta,
                                                                                                   deadboltHandler,
                                                                                                   ctx),
                                                executor)
                              .thenComposeAsync(allowed -> (invert ? !allowed : allowed) ? pass(ctx,
                                                                                                deadboltHandler,
                                                                                                pass,
                                                                                                constraintPoint,
                                                                                                "pattern - custom")
                                                                                         : fail.apply(ctx,
                                                                                                      deadboltHandler,
                                                                                                      content),
                                                executor);
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
        final ExecutionContextExecutor executor = executor();
        return getSubject(ctx,
                          deadboltHandler)
                .thenApplyAsync(subject -> subject.isPresent() ? analyzer.checkPatternEquality(subject,
                                                                                               Optional.ofNullable(value))
                                                               : invert, // this is a little clumsy - it means no subject + invert is still denied
                                executor)
                .thenComposeAsync(equal -> (invert ? !equal : equal) ? pass(ctx,
                                                                            deadboltHandler,
                                                                            pass,
                                                                            constraintPoint,
                                                                            "pattern - equality")
                                                                     : fail.apply(ctx,
                                                                                  deadboltHandler,
                                                                                  content), executor);
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
        final ExecutionContextExecutor executor = executor();
        return CompletableFuture.supplyAsync(() -> patternCache.apply(value),
                                             executor)
                                .thenCombineAsync(getSubject(ctx,
                                                             deadboltHandler),
                                                  (patternValue, subject) ->
                                                          subject.isPresent() ? analyzer.checkRegexPattern(subject,
                                                                                                           Optional.ofNullable(patternValue))
                                                                              : invert, // this is a little clumsy - it means no subject + invert is still denied
                                                  executor)

                                .thenComposeAsync(hasPassed -> (invert ? !hasPassed : hasPassed) ? pass(ctx,
                                                                                                        deadboltHandler,
                                                                                                        pass,
                                                                                                        constraintPoint,
                                                                                                        "pattern - regex")
                                                                                                 : fail.apply(ctx,
                                                                                                              deadboltHandler,
                                                                                                              content),
                                                  executor);
    }


    protected ExecutionContextExecutor executor()
    {
        final ExecutionContext executionContext = executionContextProvider.get();
        return HttpExecution.fromThread(executionContext);
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
