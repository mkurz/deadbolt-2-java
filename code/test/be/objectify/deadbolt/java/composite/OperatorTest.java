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

import org.junit.Test;
import org.mockito.Mockito;

public class OperatorTest
{
    @Test
    public void testAnd()
    {
        final Constraint c1 = Mockito.mock(Constraint.class);
        final Constraint c2 = Mockito.mock(Constraint.class);
        Operator.AND.apply(c1,
                           c2);
        Mockito.verify(c1, Mockito.times(1))
               .and(c2);
        Mockito.verifyNoMoreInteractions(c1,
                                         c2);
    }

    @Test
    public void testOr()
    {
        final Constraint c1 = Mockito.mock(Constraint.class);
        final Constraint c2 = Mockito.mock(Constraint.class);
        Operator.OR.apply(c1,
                           c2);
        Mockito.verify(c1, Mockito.times(1))
               .or(c2);
        Mockito.verifyNoMoreInteractions(c1,
                                         c2);
    }
}