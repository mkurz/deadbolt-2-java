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

import be.objectify.deadbolt.java.JavaDeadboltAnalyzer;
import be.objectify.deadbolt.java.cache.HandlerCache;
import be.objectify.deadbolt.java.cache.SubjectCache;

import javax.inject.Inject;
import java.util.Optional;

/**
 * Implements the {@link SubjectPresent} functionality, i.e. a {@link be.objectify.deadbolt.core.models.Subject} must be provided by the
 * {@link be.objectify.deadbolt.java.DeadboltHandler} to have access to the resource, but no role checks are performed.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public class SubjectPresentAction extends AbstractSubjectAction<SubjectPresent>
{
    @Inject
    public SubjectPresentAction(final JavaDeadboltAnalyzer analyzer,
                                final SubjectCache subjectCache,
                                final HandlerCache handlerCache)
    {
        super(analyzer,
              subjectCache,
              handlerCache,
              Optional::isPresent);
    }

    @Override
    Config config()
    {
        return new Config(configuration.forceBeforeAuthCheck(),
                          configuration.handlerKey(),
                          configuration.content());
    }
}
