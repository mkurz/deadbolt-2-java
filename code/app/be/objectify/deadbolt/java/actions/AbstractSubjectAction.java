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
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.utils.RequestUtils;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public abstract class AbstractSubjectAction<T>  extends AbstractDeadboltAction<T>
{
    private final F.Predicate<Subject> predicate;

    AbstractSubjectAction(final F.Predicate<Subject> predicate)
    {
        this.predicate = predicate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public F.Promise<Result> execute(final Http.Context ctx) throws Throwable
    {
        final F.Promise<Result> result;
        final Config config = config();
        if (isActionUnauthorised(ctx))
        {
            result = onAuthFailure(getDeadboltHandler(config.handlerKey,
                                                      config.handler),
                                   config.content,
                                   ctx);
        }
        else if (isActionAuthorised(ctx))
        {
            result = delegate.call(ctx);
        }
        else
        {
            final DeadboltHandler deadboltHandler = getDeadboltHandler(config.handlerKey,
                                                                       config.handler);

            result = preAuth(config.forceBeforeAuthCheck,
                             ctx,
                             deadboltHandler)
                    .flatMap(new SubjectTest(ctx,
                                             deadboltHandler,
                                             config));
        }
        return result;
    }

    abstract Config config();

    private final class SubjectTest implements F.Function<Result, F.Promise<Result>>
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
        public F.Promise<Result> apply(final Result preAuthResult) throws Throwable
        {
            final F.Promise<Result> result;
            if (preAuthResult != null)
            {
                result = F.Promise.pure(preAuthResult);
            }
            else
            {
                result = F.Promise.promise(new F.Function0<Subject>()
                {
                    @Override
                    public Subject apply() throws Throwable
                    {
                        return getSubject(ctx,
                                          deadboltHandler);
                    }
                }).flatMap(new F.Function<Subject, F.Promise<Result>>()
                {
                    @Override
                    public F.Promise<Result> apply(final Subject subject) throws Throwable
                    {
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
                    }
                });
            }
            return result;
        }
    }

    class Config
    {
        public final boolean forceBeforeAuthCheck;
        public final String handlerKey;
        public final Class<? extends DeadboltHandler> handler;
        public final String content;

        Config(final boolean forceBeforeAuthCheck,
               final String handlerKey,
               final Class<? extends DeadboltHandler> handler,
               final String content)
        {
            this.forceBeforeAuthCheck = forceBeforeAuthCheck;
            this.handlerKey = handlerKey;
            this.handler = handler;
            this.content = content;
        }
    }
}
