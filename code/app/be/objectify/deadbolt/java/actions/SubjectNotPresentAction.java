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

import be.objectify.deadbolt.java.DeadboltAnalyzer;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.ExecutionContextProvider;
import be.objectify.deadbolt.java.cache.HandlerCache;
import be.objectify.deadbolt.java.cache.SubjectCache;
import be.objectify.deadbolt.java.models.Subject;
import play.Configuration;
import play.mvc.Http;

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
    public SubjectNotPresentAction(final DeadboltAnalyzer analyzer,
                                   final SubjectCache subjectCache,
                                   final HandlerCache handlerCache,
                                   final Configuration config,
                                   final ExecutionContextProvider ecProvider)
    {
        super(analyzer,
              subjectCache,
              handlerCache,
              subjectOption -> !subjectOption.isPresent(),
              config,
              ecProvider);
    }

    @Override
    Config config()
    {
        return new Config(configuration.forceBeforeAuthCheck(),
                          configuration.handlerKey(),
                          configuration.content());
    }

    /**
     * Gets the {@link be.objectify.deadbolt.java.models.Subject} from the {@link DeadboltHandler}.
     *
     * @param ctx             the request context
     * @param deadboltHandler the Deadbolt handler
     * @return the Subject, if any
     */
    @Override
    protected CompletionStage<Optional<Subject>> getSubject(final Http.Context ctx,
                                                            final DeadboltHandler deadboltHandler)
    {
        // Bypass the additional - and in this case, incorrect - logging of the overridden method
        return subjectCache.apply(deadboltHandler,
                                   ctx);
    }
}
