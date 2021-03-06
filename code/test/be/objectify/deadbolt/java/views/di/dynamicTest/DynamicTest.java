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
package be.objectify.deadbolt.java.views.di.dynamicTest;

import be.objectify.deadbolt.java.AbstractDynamicResourceHandler;
import be.objectify.deadbolt.java.AbstractFakeApplicationTest;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import be.objectify.deadbolt.java.NoPreAuthDeadboltHandler;
import be.objectify.deadbolt.java.cache.HandlerCache;
import be.objectify.deadbolt.java.testsupport.TestHandlerCache;
import be.objectify.deadbolt.java.views.html.di.dynamic;
import be.objectify.deadbolt.java.views.html.di.dynamicTest.dynamicContent;
import org.junit.Assert;
import org.junit.Test;
import play.mvc.Http;
import play.test.Helpers;
import play.twirl.api.Content;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class DynamicTest extends AbstractFakeApplicationTest
{
    @Test
    public void testValid()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler()
        {
            @Override
            public CompletionStage<Optional<DynamicResourceHandler>> getDynamicResourceHandler(final Http.RequestHeader requestHeader)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new AbstractDynamicResourceHandler()
                {
                    @Override
                    public CompletionStage<Boolean> isAllowed(final String name,
                                                              final Optional<String> meta,
                                                              final DeadboltHandler deadboltHandler,
                                                              final Http.RequestHeader rh)
                    {
                        return CompletableFuture.completedFuture(true);
                    }
                }));
            }
        };
        final Content html = dynamicContent().render("foo",
                                                     Optional.of("bar"),
                                                     deadboltHandler, new Http.RequestBuilder().build());
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertTrue(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testName()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler()
        {
            @Override
            public CompletionStage<Optional<DynamicResourceHandler>> getDynamicResourceHandler(final Http.RequestHeader requestHeader)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new AbstractDynamicResourceHandler()
                {
                    @Override
                    public CompletionStage<Boolean> isAllowed(final String name,
                                                              final Optional<String> meta,
                                                              final DeadboltHandler deadboltHandler,
                                                              final Http.RequestHeader rh)
                    {
                        return CompletableFuture.completedFuture("foo".equals(name));
                    }
                }));
            }
        };
        final Content html = dynamicContent().render("foo",
                                                     Optional.of("bar"),
                                                     deadboltHandler, new Http.RequestBuilder().build());
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertTrue(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testMeta()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler()
        {
            @Override
            public CompletionStage<Optional<DynamicResourceHandler>> getDynamicResourceHandler(final Http.RequestHeader requestHeader)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new AbstractDynamicResourceHandler()
                {
                    @Override
                    public CompletionStage<Boolean> isAllowed(final String name,
                                                              final Optional<String> meta,
                                                              final DeadboltHandler deadboltHandler,
                                                              final Http.RequestHeader rh)
                    {
                        return CompletableFuture.completedFuture(meta.map("bar"::equals).orElse(false));
                    }
                }));
            }
        };
        final Content html = dynamicContent().render("foo",
                                                     Optional.of("bar"),
                                                     deadboltHandler, new Http.RequestBuilder().build());
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertTrue(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testInvalid()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler()
        {
            @Override
            public CompletionStage<Optional<DynamicResourceHandler>> getDynamicResourceHandler(final Http.RequestHeader requestHeader)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new AbstractDynamicResourceHandler()
                {
                    @Override
                    public CompletionStage<Boolean> isAllowed(final String name,
                                                              final Optional<String> meta,
                                                              final DeadboltHandler deadboltHandler,
                                                              final Http.RequestHeader rh)
                    {
                        return CompletableFuture.completedFuture(false);
                    }
                }));
            }
        };
        final Content html = dynamicContent().render("foo",
                                                     Optional.of("bar"),
                                                     deadboltHandler, new Http.RequestBuilder().build());
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    private dynamicContent dynamicContent() {
        return new dynamicContent(new dynamic(viewSupport(),
                                              handlers()));
    }

    public HandlerCache handlers()
    {
        // using new instances of handlers in the test
        return new TestHandlerCache(null,
                                    new HashMap<>());
    }
}