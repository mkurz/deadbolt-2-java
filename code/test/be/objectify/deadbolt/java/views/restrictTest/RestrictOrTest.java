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
package be.objectify.deadbolt.java.views.restrictTest;

import be.objectify.deadbolt.java.AbstractFakeApplicationTest;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.NoPreAuthDeadboltHandler;
import be.objectify.deadbolt.java.cache.HandlerCache;
import be.objectify.deadbolt.java.models.Subject;
import be.objectify.deadbolt.java.testsupport.TestHandlerCache;
import be.objectify.deadbolt.java.testsupport.TestRole;
import be.objectify.deadbolt.java.testsupport.TestSubject;
import org.junit.Assert;
import org.junit.Test;
import play.mvc.Http;
import play.test.Helpers;
import play.twirl.api.Content;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class RestrictOrTest extends AbstractFakeApplicationTest
{
    @Test
    public void testSingleRole_present()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new TestSubject.Builder().role(new TestRole("foo"))
                                                                                                .build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictOrContent.render(Collections.singletonList(new String[]{"foo"}),
                                                                                                         deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertTrue(content.contains("This is protected by the constraint."));
        Assert.assertFalse(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testSingleRole_notPresent()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new TestSubject.Builder().role(new TestRole("bar"))
                                                                                                .build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictOrContent.render(Collections.singletonList(new String[]{"foo"}),
                                                                                                         deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testSingleRole_noRolesPresent()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new TestSubject.Builder().build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictOrContent.render(Collections.singletonList(new String[]{"foo"}),
                                                                                                         deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testSingleRole_noSubject()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(Optional::empty);
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictOrContent.render(Collections.singletonList(new String[]{"foo"}),
                                                                                                         deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testOr_fooPresent()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new TestSubject.Builder().role(new TestRole("foo"))
                                                                                                .build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictOrContent.render(Arrays.asList(new String[]{"foo"},
                                                                                                                       new String[]{"bar"}),
                                                                                                         deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertTrue(content.contains("This is protected by the constraint."));
        Assert.assertFalse(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testOr_barPresent()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new TestSubject.Builder().role(new TestRole("bar"))
                                                                                                .build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictOrContent.render(Arrays.asList(new String[]{"foo"},
                                                                                                                       new String[]{"bar"}),
                                                                                                         deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertTrue(content.contains("This is protected by the constraint."));
        Assert.assertFalse(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testOr_bothPresent()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new TestSubject.Builder().role(new TestRole("foo"))
                                                                                                .role(new TestRole("bar"))
                                                                                                .build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictOrContent.render(Arrays.asList(new String[]{"foo"},
                                                                                                                       new String[]{"bar"}),
                                                                                                         deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertTrue(content.contains("This is protected by the constraint."));
        Assert.assertFalse(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testOr_neitherPresent()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new TestSubject.Builder().build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictOrContent.render(Arrays.asList(new String[]{"foo"},
                                                                                                                       new String[]{"bar"}),
                                                                                                         deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testOr_noSubject()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(Optional::empty);
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictOrContent.render(Collections.singletonList(new String[]{"foo"}),
                                                                                                         deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testAnd_fooPresent()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new TestSubject.Builder().role(new TestRole("foo"))
                                                                                                .build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictOrContent.render(Collections.singletonList(new String[]{"foo", "bar"}),
                                                                                                         deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testAnd_barPresent()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new TestSubject.Builder().role(new TestRole("bar"))
                                                                                                .build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictOrContent.render(Collections.singletonList(new String[]{"foo", "bar"}),
                                                                                                         deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testAnd_bothPresent()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new TestSubject.Builder().role(new TestRole("foo"))
                                                                                                .role(new TestRole("bar"))
                                                                                                .build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictOrContent.render(Collections.singletonList(new String[]{"foo", "bar"}),
                                                                                                         deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertTrue(content.contains("This is protected by the constraint."));
        Assert.assertFalse(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testAnd_neitherPresent()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new TestSubject.Builder().build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictOrContent.render(Collections.singletonList(new String[]{"foo", "bar"}),
                                                                                                         deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testAnd_noSubject()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(Optional::empty);
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictOrContent.render(Collections.singletonList(new String[]{"foo", "bar"}),
                                                                                                         deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testNegatedRole_subjectHasRole()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new TestSubject.Builder().role(new TestRole("foo"))
                                                                                                .build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictOrContent.render(Collections.singletonList(new String[]{"!foo"}),
                                                                                                         deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testNegatedRole_subjectDoesNotHaveRole()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new TestSubject.Builder().role(new TestRole("bar"))
                                                                                                .build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictOrContent.render(Collections.singletonList(new String[]{"!foo"}),
                                                                                                         deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertTrue(content.contains("This is protected by the constraint."));
        Assert.assertFalse(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testNegatedRole_subjectHasMultipleRole()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new TestSubject.Builder().role(new TestRole("foo"))
                                                                                                .role(new TestRole("bar"))
                                                                                                .build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictOrContent.render(Collections.singletonList(new String[]{"!foo"}),
                                                                                                         deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testNegatedRole_noRolesPresent()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new TestSubject.Builder().build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictOrContent.render(Collections.singletonList(new String[]{"!foo"}),
                                                                                                         deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertTrue(content.contains("This is protected by the constraint."));
        Assert.assertFalse(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testNegatedRole_noSubject()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(Optional::empty);
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictOrContent.render(Collections.singletonList(new String[]{"!foo"}),
                                                                                                         deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testOr_oneSideNegated_fooPresent()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new TestSubject.Builder().role(new TestRole("foo"))
                                                                                                .build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictOrContent.render(Arrays.asList(new String[]{"!foo"},
                                                                                                                       new String[]{"bar"}),
                                                                                                         deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testOr_oneSideNegated_barPresent()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new TestSubject.Builder().role(new TestRole("bar"))
                                                                                                .build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictOrContent.render(Arrays.asList(new String[]{"!foo"},
                                                                                                                       new String[]{"bar"}),
                                                                                                         deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertTrue(content.contains("This is protected by the constraint."));
        Assert.assertFalse(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testOr_oneSideNegated_bothPresent()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new TestSubject.Builder().role(new TestRole("foo"))
                                                                                                .role(new TestRole("bar"))
                                                                                                .build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictOrContent.render(Arrays.asList(new String[]{"!foo"},
                                                                                                                       new String[]{"bar"}),
                                                                                                         deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertTrue(content.contains("This is protected by the constraint."));
        Assert.assertFalse(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testOr_oneSideNegated_neitherPresent()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new TestSubject.Builder().build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictOrContent.render(Arrays.asList(new String[]{"!foo"},
                                                                                                                       new String[]{"bar"}),
                                                                                                         deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertTrue(content.contains("This is protected by the constraint."));
        Assert.assertFalse(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testOr_oneSideNegated_noSubject()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(Optional::empty);
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictOrContent.render(Collections.singletonList(new String[]{"!foo"}),
                                                                                                         deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testAnd_oneSideNegated_fooPresent()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new TestSubject.Builder().role(new TestRole("foo"))
                                                                                                .build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictOrContent.render(Collections.singletonList(new String[]{"!foo", "bar"}),
                                                                                                         deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testAnd_oneSideNegated_barPresent()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new TestSubject.Builder().role(new TestRole("bar"))
                                                                                                .build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictOrContent.render(Collections.singletonList(new String[]{"!foo", "bar"}),
                                                                                                         deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertTrue(content.contains("This is protected by the constraint."));
        Assert.assertFalse(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testAnd_oneSideNegated_bothPresent()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new TestSubject.Builder().role(new TestRole("foo"))
                                                                                                .role(new TestRole("bar"))
                                                                                                .build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictOrContent.render(Collections.singletonList(new String[]{"!foo", "bar"}),
                                                                                                         deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testAnd_oneSideNegated_neitherPresent()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new TestSubject.Builder().build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictOrContent.render(Collections.singletonList(new String[]{"!foo", "bar"}),
                                                                                                         deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testAnd_oneSideNegated_noSubject()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(Optional::empty);
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictOrContent.render(Collections.singletonList(new String[]{"!foo", "bar"}),
                                                                                                         deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    public HandlerCache handlers()
    {
        final Map<String, DeadboltHandler> handlers = new HashMap<>();

        handlers.put("foo", handler(() -> new TestSubject.Builder().role(new TestRole("foo"))
                                                                   .build()));
        handlers.put("bar", handler(() -> new TestSubject.Builder().role(new TestRole("bar"))
                                                                   .build()));
        handlers.put("fooBar", handler(() -> new TestSubject.Builder().role(new TestRole("foo"))
                                                                      .role(new TestRole("bar"))
                                                                      .build()));
        handlers.put("noRoles", handler(() -> new TestSubject.Builder().build()));
        handlers.put("noSubject", handler(() -> null));

        return new TestHandlerCache(null,
                                    handlers);
    }
}
