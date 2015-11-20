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
package be.objectify.deadbolt.java.actions;

import be.objectify.deadbolt.java.ConfigKeys;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.ExceptionThrowingDynamicResourceHandler;
import be.objectify.deadbolt.java.ExecutionContextProvider;
import be.objectify.deadbolt.java.JavaAnalyzer;
import be.objectify.deadbolt.java.cache.HandlerCache;
import be.objectify.deadbolt.java.cache.PatternCache;
import be.objectify.deadbolt.java.cache.SubjectCache;
import play.Configuration;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;
import scala.concurrent.ExecutionContext;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class PatternAction extends AbstractRestrictiveAction<Pattern>
{
    private final PatternCache patternCache;

    @Inject
    public PatternAction(final JavaAnalyzer analyzer,
                         final SubjectCache subjectCache,
                         final HandlerCache handlerCache,
                         final PatternCache patternCache,
                         final Configuration config,
                         final ExecutionContextProvider ecProvider)
    {
        super(analyzer,
              subjectCache,
              handlerCache,
              config,
              ecProvider);
        this.patternCache = patternCache;
    }

    public PatternAction(final JavaAnalyzer analyzer,
                         final SubjectCache subjectCache,
                         final HandlerCache handlerCache,
                         final PatternCache patternCache,
                         final Configuration config,
                         final Pattern configuration,
                         final Action<?> delegate,
                         final ExecutionContextProvider ecProvider)
    {
        this(analyzer,
             subjectCache,
             handlerCache,
             patternCache,
             config,
             ecProvider);
        this.configuration = configuration;
        this.delegate = delegate;
    }

    @Override
    public CompletionStage<Result> applyRestriction(final Http.Context ctx,
                                                    final DeadboltHandler deadboltHandler)
    {
        final CompletionStage<Result> result;

        switch (configuration.patternType())
        {
            case EQUALITY:
                result = equality(ctx,
                                  deadboltHandler,
                                  configuration.invert());
                break;
            case REGEX:
                result = regex(ctx,
                               deadboltHandler,
                               configuration.invert());
                break;
            case CUSTOM:
                result = custom(ctx,
                                deadboltHandler,
                                configuration.invert());
                break;
            default:
                throw new RuntimeException("Unknown pattern type: " + configuration.patternType());
        }

        return result;
    }

    private CompletionStage<Result> custom(final Http.Context ctx,
                                           final DeadboltHandler deadboltHandler,
                                           final boolean invert)
    {
        ctx.args.put(ConfigKeys.PATTERN_INVERT,
                     invert);
        return deadboltHandler.getDynamicResourceHandler(ctx)
                              .thenApply(option -> option.orElseGet(() -> ExceptionThrowingDynamicResourceHandler.INSTANCE))
                              .thenCompose(resourceHandler -> resourceHandler.checkPermission(getValue(),
                                                                                              deadboltHandler,
                                                                                              ctx))
                              .thenCompose(allowed -> {
                                  final CompletionStage<Result> innerResult;
                                  if (invert ? !allowed : allowed)
                                  {
                                      markActionAsAuthorised(ctx);
                                      innerResult = delegate.call(ctx);
                                  }
                                  else
                                  {
                                      markActionAsUnauthorised(ctx);
                                      innerResult = onAuthFailure(deadboltHandler,
                                                                  configuration.content(),
                                                                  ctx);
                                  }
                                  return innerResult;
                              });
    }

    public String getValue()
    {
        return configuration.value();
    }

    private CompletionStage<Result> equality(final Http.Context ctx,
                                             final DeadboltHandler deadboltHandler,
                                             final boolean invert)
    {
        final ExecutionContext executionContext = executionContextProvider.get();
        return CompletableFuture.supplyAsync(this::getValue)
                                .thenCombine(getSubject(ctx,
                                                        deadboltHandler),
                                             (patternValue, subject) -> subject.isPresent() ? analyzer.checkPatternEquality(subject,
                                                                                                                            Optional.ofNullable(patternValue))
                                                                                            : invert) // this is a little clumsy - it means no subject + invert is still denied
                                .thenCompose(equal -> {
                                    final CompletionStage<Result> result;
                                    if (invert ? !equal : equal)
                                    {
                                        markActionAsAuthorised(ctx);
                                        result = delegate.call(ctx);
                                    }
                                    else
                                    {
                                        markActionAsUnauthorised(ctx);
                                        result = onAuthFailure(deadboltHandler,
                                                               configuration.content(),
                                                               ctx);
                                    }

                                    return result;
                                });
    }

    /**
     * Checks access to the resource based on the regex
     *
     * @param ctx             the HTTP context
     * @param deadboltHandler the Deadbolt handler
     * @param invert          if true, invert the application of the constraint
     * @return the necessary result
     */
    private CompletionStage<Result> regex(final Http.Context ctx,
                                    final DeadboltHandler deadboltHandler,
                                    final boolean invert)
    {
        final ExecutionContext executionContext = executionContextProvider.get();
        return CompletableFuture.supplyAsync(this::getValue)
                                .thenApply(patternCache::apply)
                                .thenCombine(getSubject(ctx,
                                                        deadboltHandler),
                                             (patternValue, subject) -> subject.isPresent() ? analyzer.checkRegexPattern(subject,
                                                                                                                         Optional.ofNullable(patternValue))
                                                                                            : invert) // this is a little clumsy - it means no subject + invert is still denied
                                .thenCompose(applicable -> {
                                    final CompletionStage<Result> result;
                                    if (invert ? !applicable : applicable)
                                    {
                                        markActionAsAuthorised(ctx);
                                        result = delegate.call(ctx);
                                    }
                                    else
                                    {
                                        markActionAsUnauthorised(ctx);
                                        result = onAuthFailure(deadboltHandler,
                                                               configuration.content(),
                                                               ctx);
                                    }
                                    return result;
                                });
    }

    @Override
    public String getHandlerKey()
    {
        return configuration.handlerKey();
    }
}
