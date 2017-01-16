/*
 * Copyright 2010-2017 Steve Chaloner
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

import java.util.Optional;
import java.util.concurrent.Executor;
import be.objectify.deadbolt.java.DeadboltHandler;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import play.mvc.Http;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class ExceptionThrowingConstraintTest
{
    @Test
    public void testTest() throws Exception
    {
        try
        {
            new ExceptionThrowingConstraint("testConstraint")
                    .test(Mockito.mock(Http.Context.class),
                          Mockito.mock(DeadboltHandler.class),
                          Mockito.mock(Executor.class),
                          Optional.empty(),
                          (md1, md2) -> md1);
            Assert.fail();
        }
        catch (RuntimeException e)
        {
            Assert.assertEquals("A composite constraint with name [testConstraint] is specified but is not registered",
                                e.getMessage());
        }
    }
}