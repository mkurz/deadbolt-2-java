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

import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.models.PatternType;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class PatternConstraintBuilderTest extends AbstractPatternConstraintTest implements ConstraintLogicMixin
{
    @Override
    protected PatternConstraint constraint(final PatternType patternType,
                                           final DeadboltHandler handler)
    {
        final ConstraintBuilders builders = new ConstraintBuilders(logic(handler));
        final PatternConstraint constraint;
        switch (patternType)
        {
            case EQUALITY:
                constraint = builders.pattern("foo",
                                              PatternType.EQUALITY)
                                     .build();
                break;
            case REGEX:
                constraint = builders.pattern("[0-2]",
                                              PatternType.REGEX)
                                     .build();
                break;
            case CUSTOM:
                constraint = builders.pattern("blah",
                                              PatternType.CUSTOM)
                                     .build();
                break;
            default:
                throw new IllegalArgumentException("Unknown pattern type");
        }
        return constraint;
    }
}
