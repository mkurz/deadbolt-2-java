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

import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.DefaultJavaDeadboltAnalyzer;
import be.objectify.deadbolt.java.cache.DefaultHandlerCache;
import be.objectify.deadbolt.java.cache.DefaultSubjectCache;
import play.libs.F;
import play.mvc.Http;

import javax.inject.Inject;
import java.util.Optional;

/**
 * Implements the {@link SubjectNotPresent} functionality, i.e. the
 * {@link be.objectify.deadbolt.core.models.Subject} provided by the {@link be.objectify.deadbolt.java.DeadboltHandler}
 * must be null to have access to the resource.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public class SubjectNotPresentAction extends AbstractSubjectAction<SubjectNotPresent>
{
    @Inject
    public SubjectNotPresentAction(final DefaultJavaDeadboltAnalyzer analyzer,
                                   final DefaultSubjectCache subjectCache,
                                   final DefaultHandlerCache handlerCache)
    {
        super(analyzer,
              subjectCache,
              handlerCache,
              subjectOption -> !subjectOption.isPresent());
    }

    @Override
    Config config()
    {
        return new Config(configuration.forceBeforeAuthCheck(),
                          configuration.handlerKey(),
                          configuration.content());
    }

    /**
     * Gets the {@link be.objectify.deadbolt.core.models.Subject} from the {@link DeadboltHandler}.
     *
     * @param ctx             the request context
     * @param deadboltHandler the Deadbolt handler
     * @return the Subject, if any
     */
    @Override
    protected F.Promise<Optional<Subject>> getSubject(final Http.Context ctx,
                                                      final DeadboltHandler deadboltHandler)
    {
        // Bypass the additional - and in this case, incorrect - logging of the overridden method
        return subjectCache.apply(deadboltHandler,
                                   ctx);
    }
}
