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
import be.objectify.deadbolt.java.testsupport.TestRole;
import be.objectify.deadbolt.java.testsupport.TestSubject;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import play.libs.F;
import play.mvc.Http;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class ConstraintBuildersTest extends AbstractCompositeTest
{
    @Test
    public void testAllOf() throws Exception
    {
        final SubjectCache subjectCache = Mockito.mock(SubjectCache.class);
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.Request.class)))
               .thenReturn(CompletableFuture.completedFuture(F.Tuple(Optional.of(new TestSubject.Builder().role(new TestRole("foo")).build()), new Http.RequestBuilder().build())));
        final ConstraintLogic logic = new ConstraintLogic(new DeadboltAnalyzer(),
                                                          subjectCache,
                                                          new DefaultPatternCache());
        final ConstraintBuilders builders = new ConstraintBuilders(logic);

        final String[] array = builders.allOf("foo",
                                              "bar");
        Assert.assertNotNull(array);
        Assert.assertEquals(2,
                            array.length);
        Assert.assertEquals("foo",
                            array[0]);
        Assert.assertEquals("bar",
                            array[1]);
    }

    @Test
    public void testAnyOf() throws Exception
    {
        final SubjectCache subjectCache = Mockito.mock(SubjectCache.class);
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.Request.class)))
               .thenReturn(CompletableFuture.completedFuture(F.Tuple(Optional.of(new TestSubject.Builder().role(new TestRole("foo")).build()), new Http.RequestBuilder().build())));
        final ConstraintLogic logic = new ConstraintLogic(new DeadboltAnalyzer(),
                                                          subjectCache,
                                                          new DefaultPatternCache());
        final ConstraintBuilders builders = new ConstraintBuilders(logic);

        final List<String[]> list = builders.anyOf(new String[]{"foo"},
                                                   new String[]{"bar"});
        Assert.assertNotNull(list);
        Assert.assertEquals(2,
                            list.size());
        Assert.assertArrayEquals(new String[]{"foo"},
                                 list.get(0));
        Assert.assertArrayEquals(new String[]{"bar"},
                                 list.get(1));
    }
}