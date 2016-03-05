/*
 * Copyright 2012-2016 Steve Chaloner
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
package be.objectify.deadbolt.java.composite;

import be.objectify.deadbolt.java.DeadboltAnalyzer;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.cache.PatternCache;
import be.objectify.deadbolt.java.models.PatternType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Http;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class PatternConstraint implements Constraint
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PatternConstraint.class);

    private final String value;
    private final PatternType patternType;
    private final DeadboltAnalyzer analyzer;
    private final PatternCache patternCache;

    public PatternConstraint(final String value,
                             final PatternType patternType,
                             final DeadboltAnalyzer analyzer,
                             final PatternCache patternCache)
    {
        this.value = value;
        this.patternType = patternType;
        this.analyzer = analyzer;
        this.patternCache = patternCache;
    }

    @Override
    public CompletionStage<Boolean> test(final Http.Context context,
                                         final DeadboltHandler handler,
                                         final Executor executor)
    {
        final CompletionStage<Boolean> result;
        switch (patternType) {
            case EQUALITY:
                result = handler.getSubject(context)
                                .thenApplyAsync(maybeSubject -> analyzer.checkPatternEquality(maybeSubject,
                                                                                              Optional.ofNullable(value)));
                break;
            case REGEX:
                result = handler.getSubject(context)
                                .thenApplyAsync(maybeSubject -> analyzer.checkRegexPattern(maybeSubject,
                                                                                           Optional.ofNullable(patternCache.apply(value))));
                break;
            case CUSTOM:
                result = handler.getDynamicResourceHandler(context)
                                .thenComposeAsync(maybeDrh -> maybeDrh.map(drh -> drh.checkPermission(value,
                                                                                                    handler,
                                                                                                    context))
                                                                      .orElseGet(() -> {
                                                                          LOGGER.error("No dynamic resource handler found when checking custom pattern [{}]", value);
                                                                          return CompletableFuture.completedFuture(false);
                                                                      }),
                                                  executor);
                break;
            default:
                result = CompletableFuture.completedFuture(false);
        }
        return result;
    }
}
