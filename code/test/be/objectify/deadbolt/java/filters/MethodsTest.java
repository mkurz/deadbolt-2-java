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
package be.objectify.deadbolt.java.filters;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class MethodsTest
{

    @Test
    public void testAny()
    {
        Assert.assertFalse(Methods.ANY.isPresent());
    }

    @Test
    public void testGet()
    {
        Assert.assertEquals("GET",
                            Methods.GET.orElse(null));
    }

    @Test
    public void testPost()
    {
        Assert.assertEquals("POST",
                            Methods.POST.orElse(null));
    }

    @Test
    public void testDelete()
    {
        Assert.assertEquals("DELETE",
                            Methods.DELETE.orElse(null));
    }

    @Test
    public void testPut()
    {
        Assert.assertEquals("PUT",
                            Methods.PUT.orElse(null));
    }

    @Test
    public void testPatch()
    {
        Assert.assertEquals("PATCH",
                            Methods.PATCH.orElse(null));
    }

    @Test
    public void testOptions()
    {
        Assert.assertEquals("OPTIONS",
                            Methods.OPTIONS.orElse(null));
    }

    @Test
    public void testHead()
    {
        Assert.assertEquals("HEAD",
                            Methods.HEAD.orElse(null));
    }
}