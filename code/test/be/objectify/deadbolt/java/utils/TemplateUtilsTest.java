/*
 * Copyright 2013 Steve Chaloner
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
public class TemplateUtilsTest
{
    @Test
    public void testAllOf_noInput()
    {
        final String[] array = TemplateUtils.allOf();
        Assert.assertNotNull(array);
        Assert.assertEquals(0,
                            array.length);
    }

    @Test
    public void testAllOf_singleValueInput()
    {
        final String[] array = TemplateUtils.allOf("foo");
        Assert.assertNotNull(array);
        Assert.assertEquals(1,
                            array.length);
        Assert.assertEquals("foo",
                            array[0]);
    }

    @Test
    public void testAllOf_multipleValueInput()
    {
        final String[] array = TemplateUtils.allOf("foo", "bar");
        Assert.assertNotNull(array);
        Assert.assertEquals(2,
                            array.length);
        Assert.assertEquals("foo",
                            array[0]);
        Assert.assertEquals("bar",
                            array[1]);
    }

    @Test
    public void testAllOf_nullArrayInput()
    {
        final String[] array = TemplateUtils.allOf((String[])null);
        Assert.assertNotNull(array);
        Assert.assertEquals(0,
                            array.length);
    }

    @Test
    public void testAllOf_nullStringInput()
    {
        final String[] array = TemplateUtils.allOf((String)null);
        Assert.assertNotNull(array);
        Assert.assertEquals(1,
                            array.length);
        Assert.assertNull(array[0]);
    }

    @Test
    public void testAs_noInput()
    {
        final String[] array = TemplateUtils.as();
        Assert.assertNotNull(array);
        Assert.assertEquals(0,
                            array.length);
    }

    @Test
    public void testAs_singleValueInput()
    {
        final String[] array = TemplateUtils.as("foo");
        Assert.assertNotNull(array);
        Assert.assertEquals(1,
                            array.length);
        Assert.assertEquals("foo",
                            array[0]);
    }

    @Test
    public void testAs_multipleValueInput()
    {
        final String[] array = TemplateUtils.as("foo", "bar");
        Assert.assertNotNull(array);
        Assert.assertEquals(2,
                            array.length);
        Assert.assertEquals("foo",
                            array[0]);
        Assert.assertEquals("bar",
                            array[1]);
    }

    @Test
    public void testAs_nullArrayInput()
    {
        final String[] array = TemplateUtils.as((String[])null);
        Assert.assertNotNull(array);
        Assert.assertEquals(0,
                            array.length);
    }

    @Test
    public void testAs_nullStringInput()
    {
        final String[] array = TemplateUtils.as((String)null);
        Assert.assertNotNull(array);
        Assert.assertEquals(1,
                            array.length);
        Assert.assertNull(array[0]);
    }
}
