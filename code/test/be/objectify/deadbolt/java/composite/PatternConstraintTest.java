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

import java.util.Optional;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class PatternConstraintTest extends AbstractPatternConstraintTest implements ConstraintLogicMixin
{
    @Override
    protected PatternConstraint constraint(final PatternType patternType,
                                           final DeadboltHandler handler)
    {
        final PatternConstraint constraint;
        switch (patternType)
        {
            case EQUALITY:
                constraint = new PatternConstraint("foo",
                                                   PatternType.EQUALITY,
                                                   Optional.empty(),
                                                   false,
                                                   Optional.empty(),
                                                   logic(handler));
                break;
            case REGEX:
                constraint = new PatternConstraint("[0-2]",
                                                   PatternType.REGEX,
                                                   Optional.empty(),
                                                   false,
                                                   Optional.empty(),
                                                   logic(handler));
                break;
            case CUSTOM:
                constraint = new PatternConstraint("blah",
                                                   PatternType.CUSTOM,
                                                   Optional.empty(),
                                                   false,
                                                   Optional.empty(),
                                                   logic(handler));
                break;
            default:
                throw new IllegalArgumentException("Unknown pattern type");
        }
        return constraint;
    }
}
