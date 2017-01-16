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
package be.objectify.deadbolt.java.utils;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class QuadFunctionTest
{
    @Test
    public void testAndThen() throws Exception
    {
        final QuadFunction<Integer, Integer, Integer, Integer, Integer> f1 = (a, b, c, d) -> a + b + c + d;
        final QuadFunction<Integer, Integer, Integer, Integer, Boolean> f2 = f1.andThen(i -> i % 2 == 0);
        Assert.assertTrue(f2.apply(1, 2, 3, 4));
        Assert.assertFalse(f2.apply(1, 1, 1, 2));
    }
}