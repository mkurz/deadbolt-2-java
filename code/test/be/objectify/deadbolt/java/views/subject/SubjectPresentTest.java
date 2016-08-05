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
package be.objectify.deadbolt.java.views.subject;

import be.objectify.deadbolt.java.AbstractFakeApplicationTest;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.cache.HandlerCache;
import be.objectify.deadbolt.java.testsupport.TestHandlerCache;
import be.objectify.deadbolt.java.testsupport.TestSubject;
import org.junit.Assert;
import org.junit.Test;
import play.test.Helpers;
import play.twirl.api.Content;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class SubjectPresentTest extends AbstractFakeApplicationTest
{
    private final HandlerCache handlers = handlers();

    @Test
    public void testWithSubjectPresent()
    {
        final Content html = be.objectify.deadbolt.java.views.html.subject.subjectPresentContent.render(handlers.apply("present"));
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertTrue(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testWithNoSubjectPresent()
    {
        final Content html = be.objectify.deadbolt.java.views.html.subject.subjectPresentContent.render(handlers.apply("notPresent"));
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    public HandlerCache handlers()
    {
        final Map<String, DeadboltHandler> handlers = new HashMap<>();

        handlers.put("present", handler(() -> new TestSubject.Builder().build()));
        handlers.put("notPresent", handler(() -> null));

        return new TestHandlerCache(null,
                                    handlers);
    }
}
