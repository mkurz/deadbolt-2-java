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
package be.objectify.deadbolt.java.views.patternTest;

import be.objectify.deadbolt.java.AbstractDynamicResourceHandler;
import be.objectify.deadbolt.java.AbstractFakeApplicationTest;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import be.objectify.deadbolt.java.NoPreAuthDeadboltHandler;
import be.objectify.deadbolt.java.cache.HandlerCache;
import be.objectify.deadbolt.java.models.PatternType;
import be.objectify.deadbolt.java.models.Subject;
import be.objectify.deadbolt.java.testsupport.TestHandlerCache;
import be.objectify.deadbolt.java.testsupport.TestPermission;
import be.objectify.deadbolt.java.testsupport.TestSubject;
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
public class PatternOrTest extends AbstractFakeApplicationTest
{
    @Test
    public void testEquality_hasEqualPermission()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new TestSubject.Builder().permission(new TestPermission("killer.undead.zombie"))
                                                                                                .build()));
            }

        };
        final Content html = be.objectify.deadbolt.java.views.html.patternTest.patternOrContent.render("killer.undead.zombie",
                                                                                                       PatternType.EQUALITY,
                                                                                                       deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertTrue(content.contains("This is protected by the constraint."));
        Assert.assertFalse(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testEquality_doesNotHaveEqualPermission()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new TestSubject.Builder().permission(new TestPermission("killer.undead.vampire"))
                                                                                                .build()));
            }

        };
        final Content html = be.objectify.deadbolt.java.views.html.patternTest.patternOrContent.render("killer.undead.zombie",
                                                                                                       PatternType.EQUALITY,
                                                                                                       deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testEquality_doesNotHavePermissions()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new TestSubject.Builder().build()));
            }

        };
        final Content html = be.objectify.deadbolt.java.views.html.patternTest.patternOrContent.render("killer.undead.zombie",
                                                                                                       PatternType.EQUALITY,
                                                                                                       deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testEquality_noSubject()
    {
        final Content html = be.objectify.deadbolt.java.views.html.patternTest.patternOrContent.render("killer.undead.zombie",
                                                                                                       PatternType.EQUALITY,
                                                                                                       new NoPreAuthDeadboltHandler(ecProvider())
                                                                                                       {
                                                                                                       });
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testRegex_hasMatch()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new TestSubject.Builder().permission(new TestPermission("killer.undead.zombie"))
                                                                                                .build()));
            }

        };
        final Content html = be.objectify.deadbolt.java.views.html.patternTest.patternOrContent.render("killer.undead.*",
                                                                                                       PatternType.REGEX,
                                                                                                       deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertTrue(content.contains("This is protected by the constraint."));
        Assert.assertFalse(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testRegex_hasTopLevelMatch()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new TestSubject.Builder().permission(new TestPermission("killer.undead.zombie"))
                                                                                                .build()));
            }

        };
        final Content html = be.objectify.deadbolt.java.views.html.patternTest.patternOrContent.render("killer.*",
                                                                                                       PatternType.REGEX,
                                                                                                       deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertTrue(content.contains("This is protected by the constraint."));
        Assert.assertFalse(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testRegex_doesNotHaveMatch()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new TestSubject.Builder().permission(new TestPermission("killer.undead.vampire"))
                                                                                                .build()));
            }

        };
        final Content html = be.objectify.deadbolt.java.views.html.patternTest.patternOrContent.render("killer.pixies.*",
                                                                                                       PatternType.REGEX,
                                                                                                       deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testRegex_doesNotHavePermissions()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new TestSubject.Builder().build()));
            }

        };
        final Content html = be.objectify.deadbolt.java.views.html.patternTest.patternOrContent.render("killer.undead.zombie",
                                                                                                       PatternType.REGEX,
                                                                                                       deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testRegex_noSubject()
    {
        final Content html = be.objectify.deadbolt.java.views.html.patternTest.patternOrContent.render("killer.undead.zombie",
                                                                                                       PatternType.REGEX,
                                                                                                       new NoPreAuthDeadboltHandler(ecProvider())
                                                                                                       {
                                                                                                       });
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testCustom_value()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<DynamicResourceHandler>> getDynamicResourceHandler(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new AbstractDynamicResourceHandler()
                {
                    @Override
                    public CompletionStage<Boolean> checkPermission(final String permissionValue,
                                                                    final Optional<String> meta,
                                                                    final DeadboltHandler deadboltHandler,
                                                                    final Http.Context ctx)
                    {
                        return CompletableFuture.completedFuture("killer.undead.zombie".equals(permissionValue));
                    }
                }));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.patternTest.patternOrContent.render("killer.undead.zombie",
                                                                                                       PatternType.CUSTOM,
                                                                                                       deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertTrue(content.contains("This is protected by the constraint."));
        Assert.assertFalse(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testCustom_hasPermission()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<DynamicResourceHandler>> getDynamicResourceHandler(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new AbstractDynamicResourceHandler()
                {
                    @Override
                    public CompletionStage<Boolean> checkPermission(final String permissionValue,
                                                                    final Optional<String> meta,
                                                                    final DeadboltHandler deadboltHandler,
                                                                    final Http.Context ctx)
                    {
                        return CompletableFuture.completedFuture(true);
                    }
                }));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.patternTest.patternOrContent.render("killer.undead.zombie",
                                                                                                       PatternType.CUSTOM,
                                                                                                       deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertTrue(content.contains("This is protected by the constraint."));
        Assert.assertFalse(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testCustom_hasNotPermission()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<DynamicResourceHandler>> getDynamicResourceHandler(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new AbstractDynamicResourceHandler()
                {
                    @Override
                    public CompletionStage<Boolean> checkPermission(final String permissionValue,
                                                                    final Optional<String> meta,
                                                                    final DeadboltHandler deadboltHandler,
                                                                    final Http.Context ctx)
                    {
                        return CompletableFuture.completedFuture(false);
                    }
                }));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.patternTest.patternOrContent.render("killer.undead.zombie",
                                                                                                       PatternType.CUSTOM,
                                                                                                       deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    public HandlerCache handlers()
    {
        // using new instances of handlers in the test
        return new TestHandlerCache(null,
                                    new HashMap<>());
    }
}