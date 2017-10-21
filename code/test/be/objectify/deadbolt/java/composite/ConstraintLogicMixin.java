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
package be.objectify.deadbolt.java.composite;

import be.objectify.deadbolt.java.ConstraintLogic;
import be.objectify.deadbolt.java.DeadboltAnalyzer;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.cache.DefaultPatternCache;
import be.objectify.deadbolt.java.cache.SubjectCache;
import be.objectify.deadbolt.java.testsupport.FakeCache;
import org.mockito.Mockito;
import play.mvc.Http;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public interface ConstraintLogicMixin
{
    default public ConstraintLogic logic(final DeadboltHandler deadboltHandler)
    {
        final SubjectCache subjectCache = Mockito.mock(SubjectCache.class);
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.Context.class)))
               .thenReturn(deadboltHandler.getSubject(Mockito.mock(Http.Context.class)));
        return new ConstraintLogic(new DeadboltAnalyzer(),
                                   subjectCache,
                                   new DefaultPatternCache(new FakeCache()));
    }
}
