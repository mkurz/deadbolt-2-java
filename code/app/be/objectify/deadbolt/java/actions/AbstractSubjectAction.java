/*
 * Copyright 2015 Steve Chaloner
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

import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.ConfigKeys;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.ExecutionContextProvider;
import be.objectify.deadbolt.java.JavaAnalyzer;
import be.objectify.deadbolt.java.cache.HandlerCache;
import be.objectify.deadbolt.java.cache.SubjectCache;
import play.Configuration;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public abstract class AbstractSubjectAction<T>  extends AbstractDeadboltAction<T>
{
    private final F.Predicate<Optional<Subject>> predicate;

    AbstractSubjectAction(final JavaAnalyzer analyzer,
                          final SubjectCache subjectCache,
                          final HandlerCache handlerCache,
                          final F.Predicate<Optional<Subject>> predicate,
                          final Configuration config,
                          final ExecutionContextProvider ecProvider)
    {
        super(analyzer,
              subjectCache,
              handlerCache,
              config,
              ecProvider);
        this.predicate = predicate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public F.Promise<Result> execute(final Http.Context ctx) throws Throwable
    {
        F.Promise<Result> result;
        final Config config = config();
        if (isActionUnauthorised(ctx))
        {
            result = onAuthFailure(getDeadboltHandler(config.handlerKey),
                                   config.content,
                                   ctx);
        }
        else if (isActionAuthorised(ctx))
        {
            result = delegate.call(ctx);
        }
        else
        {
            final DeadboltHandler deadboltHandler = getDeadboltHandler(config.handlerKey);

            result = preAuth(config.forceBeforeAuthCheck,
                             ctx,
                             deadboltHandler)
                    .flatMap(preAuthResult -> new SubjectTest(ctx,
                                                              deadboltHandler,
                                                              config).apply(preAuthResult));
            if (blocking)
            {
                result = F.Promise.pure(result.get(blockingTimeout,
                                                   TimeUnit.MILLISECONDS));
            }
        }
        return result;
    }

    abstract Config config();

    private final class SubjectTest implements F.Function<Optional<Result>, F.Promise<Result>>
    {
        private final Http.Context ctx;
        private final DeadboltHandler deadboltHandler;
        private final Config config;

        private SubjectTest(final Http.Context ctx,
                            final DeadboltHandler deadboltHandler,
                            final Config config)
        {
            this.ctx = ctx;
            this.deadboltHandler = deadboltHandler;
            this.config = config;
        }

        @Override
        public F.Promise<Result> apply(final Optional<Result> preAuthResult) throws Throwable
        {
            return preAuthResult.map(F.Promise::pure)
                                .orElseGet(() -> getSubject(ctx,
                                                            deadboltHandler)
                                        .flatMap(subject -> {
                                            final F.Promise<Result> innerResult;
                                            if (predicate.test(subject))
                                            {
                                                markActionAsAuthorised(ctx);
                                                innerResult = delegate.call(ctx);
                                            }
                                            else
                                            {
                                                markActionAsUnauthorised(ctx);
                                                innerResult = onAuthFailure(deadboltHandler,
                                                                            config.content,
                                                                            ctx);
                                            }
                                            return innerResult;
                                        }));
        }
    }

    class Config
    {
        public final boolean forceBeforeAuthCheck;
        public final String handlerKey;
        public final String content;

        Config(final boolean forceBeforeAuthCheck,
               final String handlerKey,
               final String content)
        {
            this.forceBeforeAuthCheck = forceBeforeAuthCheck;
            this.handlerKey = handlerKey;
            this.content = content;
        }
    }
}
