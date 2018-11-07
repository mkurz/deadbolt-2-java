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
import play.libs.F;
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
        return pattern(ctx, deadboltHandler, content, new String[] {value}, null, patternType, meta, invert, pass, fail, constraintPoint);
    }

    public <T> CompletionStage<T> pattern(final Http.Context ctx,
                                          final DeadboltHandler deadboltHandler,
                                          final Optional<String> content,
                                          final String[] values,
                                          final ConstraintMode mode,
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
                                  values,
                                  0,
                                  mode,
                                  invert,
                                  pass,
                                  fail,
                                  constraintPoint);
                break;
            case REGEX:
                result = regex(ctx,
                               deadboltHandler,
                               content,
                               values,
                               0,
                               mode,
                               invert,
                               pass,
                               fail,
                               constraintPoint);
                break;
            case CUSTOM:
                result = custom(ctx,
                                deadboltHandler,
                                content,
                                values,
                                0,
                                mode,
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
                                          final String[] values,
                                          final int valueIndex,
                                          final ConstraintMode mode,
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
                              .thenCompose(resourceHandler -> resourceHandler.checkPermission(values[valueIndex],
                                                                                              meta,
                                                                                              deadboltHandler,
                                                                                              ctx))
                              .thenCompose(allowed -> (invert ? !allowed : allowed) ? (successCallAgain(mode, values, valueIndex) ? custom(ctx,
                                                                                                                                           deadboltHandler,
                                                                                                                                           content,
                                                                                                                                           values,
                                                                                                                                           valueIndex + 1,
                                                                                                                                           mode,
                                                                                                                                           meta,
                                                                                                                                           invert,
                                                                                                                                           pass,
                                                                                                                                           fail,
                                                                                                                                           constraintPoint)
                                                                                                                                  : pass(ctx,
                                                                                                                                         deadboltHandler,
                                                                                                                                         pass,
                                                                                                                                         constraintPoint,
                                                                                                                                         "pattern - custom"))
                                                                                    : (failCallAgain(mode, values, valueIndex) ? custom(ctx,
                                                                                                                                        deadboltHandler,
                                                                                                                                        content,
                                                                                                                                        values,
                                                                                                                                        valueIndex + 1,
                                                                                                                                        mode,
                                                                                                                                        meta,
                                                                                                                                        invert,
                                                                                                                                        pass,
                                                                                                                                        fail,
                                                                                                                                        constraintPoint)
                                                                                                                               : fail.apply(ctx,
                                                                                                                                            deadboltHandler,
                                                                                                                                            content)));
    }

    private <T> CompletionStage<T> equality(final Http.Context ctx,
                                            final DeadboltHandler deadboltHandler,
                                            final Optional<String> content,
                                            final String[] values,
                                            final int valueIndex,
                                            final ConstraintMode mode,
                                            final boolean invert,
                                            final Function<Http.Context, CompletionStage<T>> pass,
                                            final TriFunction<Http.Context, DeadboltHandler, Optional<String>, CompletionStage<T>> fail,
                                            final ConstraintPoint constraintPoint)
    {
        return getSubject(ctx,
                          deadboltHandler)
                .thenCompose(subject -> {
                                      final boolean equal = subject.isPresent() ? analyzer.checkPatternEquality(subject,
                                                                                                                Optional.ofNullable(values[valueIndex]))
                                                                                : invert; // this is a little clumsy - it means no subject + invert is still denied
                                      return (invert ? !equal : equal) ? (successCallAgain(mode, values, valueIndex) ? equality(ctx,
                                                                                                                                deadboltHandler,
                                                                                                                                content,
                                                                                                                                values,
                                                                                                                                valueIndex + 1,
                                                                                                                                mode,
                                                                                                                                invert,
                                                                                                                                pass,
                                                                                                                                fail,
                                                                                                                                constraintPoint)
                                                                                                                     : pass(ctx,
                                                                                                                            deadboltHandler,
                                                                                                                            pass,
                                                                                                                            constraintPoint,
                                                                                                                            "pattern - equality"))
                                                                       : (failCallAgain(mode, values, valueIndex) ? equality(ctx,
                                                                                                                             deadboltHandler,
                                                                                                                             content,
                                                                                                                             values,
                                                                                                                             valueIndex + 1,
                                                                                                                             mode,
                                                                                                                             invert,
                                                                                                                             pass,
                                                                                                                             fail,
                                                                                                                             constraintPoint)
                                                                                                                  : fail.apply(ctx,
                                                                                                                               deadboltHandler,
                                                                                                                               content));
                });
    }

    protected CompletionStage<F.Tuple<Optional<? extends Subject>, Http.RequestHeader>> getSubject(final Http.RequestHeader requestHeader,
                                                                                                   final DeadboltHandler deadboltHandler)
    {
        return subjectCache.apply(deadboltHandler,
                                  requestHeader);
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
                                         final String[] values,
                                         final int valueIndex,
                                         final ConstraintMode mode,
                                         final boolean invert,
                                         final Function<Http.Context, CompletionStage<T>> pass,
                                         final TriFunction<Http.Context, DeadboltHandler, Optional<String>, CompletionStage<T>> fail,
                                         final ConstraintPoint constraintPoint)
    {
        return CompletableFuture.completedFuture(patternCache.apply(values[valueIndex]))
                                .thenCombine(getSubject(ctx,
                                                             deadboltHandler),
                                             (patternValue, subject) ->
                                                     subject.isPresent() ? analyzer.checkRegexPattern(subject,
                                                                                                      Optional.ofNullable(patternValue))
                                                                         : invert) // this is a little clumsy - it means no subject + invert is still denied

                                .thenCompose(hasPassed -> (invert ? !hasPassed : hasPassed) ? (successCallAgain(mode, values, valueIndex) ? regex(ctx,
                                                                                                                                                  deadboltHandler,
                                                                                                                                                  content,
                                                                                                                                                  values,
                                                                                                                                                  valueIndex + 1,
                                                                                                                                                  mode,
                                                                                                                                                  invert,
                                                                                                                                                  pass,
                                                                                                                                                  fail,
                                                                                                                                                  constraintPoint)
                                                                                                                                          : pass(ctx,
                                                                                                                                                 deadboltHandler,
                                                                                                                                                 pass,
                                                                                                                                                 constraintPoint,
                                                                                                                                                 "pattern - regex"))
                                                                                            : (failCallAgain(mode, values, valueIndex) ? regex(ctx,
                                                                                                                                               deadboltHandler,
                                                                                                                                               content,
                                                                                                                                               values,
                                                                                                                                               valueIndex + 1,
                                                                                                                                               mode,
                                                                                                                                               invert,
                                                                                                                                               pass,
                                                                                                                                               fail,
                                                                                                                                               constraintPoint)
                                                                                                                                       : fail.apply(ctx,
                                                                                                                                                    deadboltHandler,
                                                                                                                                                    content)));
    }

    private static boolean successCallAgain(final ConstraintMode mode, final String values[], final int valueIndex) {
        if(mode == null || ConstraintMode.AND.equals(mode)) { // null check because AND is the default mode
            if(values.length > (valueIndex + 1)) {
                // there is at least another check left - that one has to pass too in AND mode
                return true;
            }
        }
        return false;
    }

    private static boolean failCallAgain(final ConstraintMode mode, final String values[], final int valueIndex) {
        if(ConstraintMode.OR.equals(mode)) {
            if(values.length > (valueIndex + 1)) {
                // there is at least another check left - give that one the chance to pass
                return true;
            }
        }
        return false;
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
