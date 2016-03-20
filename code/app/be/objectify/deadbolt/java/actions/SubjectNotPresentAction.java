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

import be.objectify.deadbolt.java.ConstraintLogic;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.ExecutionContextProvider;
import be.objectify.deadbolt.java.cache.HandlerCache;
import play.Configuration;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * Implements the {@link SubjectNotPresent} functionality, i.e. the
 * {@link be.objectify.deadbolt.java.models.Subject} provided by the {@link DeadboltHandler}
 * must be null to have access to the resource.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public class SubjectNotPresentAction extends AbstractSubjectAction<SubjectNotPresent>
{
    @Inject
    public SubjectNotPresentAction(final HandlerCache handlerCache,
                                   final Configuration config,
                                   final ExecutionContextProvider ecProvider,
                                   final ConstraintLogic constraintLogic)
    {
        super(handlerCache,
              config,
              ecProvider,
              constraintLogic);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Config config()
    {
        return new Config(configuration.forceBeforeAuthCheck(),
                          configuration.handlerKey(),
                          configuration.content());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    CompletionStage<Result> present(final Http.Context context,
                                    final DeadboltHandler handler,
                                    final Optional<String> content)
    {
        return unauthorizeAndFail(context,
                                  handler,
                                  content);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    CompletionStage<Result> notPresent(final Http.Context context,
                                       final DeadboltHandler handler,
                                       final Optional<String> content)
    {
        return authorizeAndExecute(context);
    }
}
