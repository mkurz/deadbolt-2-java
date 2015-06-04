package be.objectify.deadbolt.java.views.restrictTest;

import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.AbstractFakeApplicationTest;
import be.objectify.deadbolt.java.AbstractNoPreAuthDeadboltHandler;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.testsupport.TestRole;
import be.objectify.deadbolt.java.testsupport.TestSubject;
import org.junit.Assert;
import org.junit.Test;
import play.libs.F;
import play.mvc.Http;
import play.test.Helpers;
import play.twirl.api.Content;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class RestrictTest extends AbstractFakeApplicationTest
{
    @Test
    public void testSingleRole_present()
    {
        final DeadboltHandler deadboltHandler = new AbstractNoPreAuthDeadboltHandler()
        {
            @Override
            public F.Promise<Optional<Subject>> getSubject(final Http.Context context)
            {
                return F.Promise.promise(() -> Optional.of(new TestSubject.Builder().role(new TestRole("foo"))
                                                                                    .build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictContent.render(Collections.singletonList(new String[]{"foo"}),
                                                                                                       deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertTrue(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testSingleRole_notPresent()
    {
        final DeadboltHandler deadboltHandler = new AbstractNoPreAuthDeadboltHandler()
        {
            @Override
            public F.Promise<Optional<Subject>> getSubject(final Http.Context context)
            {
                return F.Promise.promise(() -> Optional.of(new TestSubject.Builder().role(new TestRole("bar"))
                                                                                    .build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictContent.render(Collections.singletonList(new String[]{"foo"}),
                                                                                                       deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testSingleRole_noRolesPresent()
    {
        final DeadboltHandler deadboltHandler = new AbstractNoPreAuthDeadboltHandler()
        {
            @Override
            public F.Promise<Optional<Subject>> getSubject(final Http.Context context)
            {
                return F.Promise.promise(() -> Optional.of(new TestSubject.Builder().build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictContent.render(Collections.singletonList(new String[]{"foo"}),
                                                                                                       deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testSingleRole_noSubject()
    {
        final DeadboltHandler deadboltHandler = new AbstractNoPreAuthDeadboltHandler()
        {
            @Override
            public F.Promise<Optional<Subject>> getSubject(final Http.Context context)
            {
                return F.Promise.promise(Optional::empty);
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictContent.render(Collections.singletonList(new String[]{"foo"}),
                                                                                                       deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testOr_fooPresent()
    {
        final DeadboltHandler deadboltHandler = new AbstractNoPreAuthDeadboltHandler()
        {
            @Override
            public F.Promise<Optional<Subject>> getSubject(final Http.Context context)
            {
                return F.Promise.promise(() -> Optional.of(new TestSubject.Builder().role(new TestRole("foo"))
                                                                                    .build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictContent.render(Arrays.asList(new String[]{"foo"},
                                                                                                                     new String[]{"bar"}),
                                                                                                       deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertTrue(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testOr_barPresent()
    {
        final DeadboltHandler deadboltHandler = new AbstractNoPreAuthDeadboltHandler()
        {
            @Override
            public F.Promise<Optional<Subject>> getSubject(final Http.Context context)
            {
                return F.Promise.promise(() -> Optional.of(new TestSubject.Builder().role(new TestRole("bar"))
                                                                                    .build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictContent.render(Arrays.asList(new String[]{"foo"},
                                                                                                                     new String[]{"bar"}),
                                                                                                       deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertTrue(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testOr_bothPresent()
    {
        final DeadboltHandler deadboltHandler = new AbstractNoPreAuthDeadboltHandler()
        {
            @Override
            public F.Promise<Optional<Subject>> getSubject(final Http.Context context)
            {
                return F.Promise.promise(() -> Optional.of(new TestSubject.Builder().role(new TestRole("foo"))
                                                                                    .role(new TestRole("bar"))
                                                                                    .build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictContent.render(Arrays.asList(new String[]{"foo"},
                                                                                                                     new String[]{"bar"}),
                                                                                                       deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertTrue(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testOr_neitherPresent()
    {
        final DeadboltHandler deadboltHandler = new AbstractNoPreAuthDeadboltHandler()
        {
            @Override
            public F.Promise<Optional<Subject>> getSubject(final Http.Context context)
            {
                return F.Promise.promise(() -> Optional.of(new TestSubject.Builder().build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictContent.render(Arrays.asList(new String[]{"foo"},
                                                                                                                     new String[]{"bar"}),
                                                                                                       deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testOr_noSubject()
    {
        final DeadboltHandler deadboltHandler = new AbstractNoPreAuthDeadboltHandler()
        {
            @Override
            public F.Promise<Optional<Subject>> getSubject(final Http.Context context)
            {
                return F.Promise.promise(Optional::empty);
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictContent.render(Collections.singletonList(new String[]{"foo"}),
                                                                                                       deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testAnd_fooPresent()
    {
        final DeadboltHandler deadboltHandler = new AbstractNoPreAuthDeadboltHandler()
        {
            @Override
            public F.Promise<Optional<Subject>> getSubject(final Http.Context context)
            {
                return F.Promise.promise(() -> Optional.of(new TestSubject.Builder().role(new TestRole("foo"))
                                                                                    .build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictContent.render(Collections.singletonList(new String[]{"foo", "bar"}),
                                                                                                       deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testAnd_barPresent()
    {
        final DeadboltHandler deadboltHandler = new AbstractNoPreAuthDeadboltHandler()
        {
            @Override
            public F.Promise<Optional<Subject>> getSubject(final Http.Context context)
            {
                return F.Promise.promise(() -> Optional.of(new TestSubject.Builder().role(new TestRole("bar"))
                                                                                    .build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictContent.render(Collections.singletonList(new String[]{"foo", "bar"}),
                                                                                                       deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testAnd_bothPresent()
    {
        final DeadboltHandler deadboltHandler = new AbstractNoPreAuthDeadboltHandler()
        {
            @Override
            public F.Promise<Optional<Subject>> getSubject(final Http.Context context)
            {
                return F.Promise.promise(() -> Optional.of(new TestSubject.Builder().role(new TestRole("foo"))
                                                                                    .role(new TestRole("bar"))
                                                                                    .build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictContent.render(Collections.singletonList(new String[]{"foo", "bar"}),
                                                                                                       deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertTrue(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testAnd_neitherPresent()
    {
        final DeadboltHandler deadboltHandler = new AbstractNoPreAuthDeadboltHandler()
        {
            @Override
            public F.Promise<Optional<Subject>> getSubject(final Http.Context context)
            {
                return F.Promise.promise(() -> Optional.of(new TestSubject.Builder().build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictContent.render(Collections.singletonList(new String[]{"foo", "bar"}),
                                                                                                       deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testAnd_noSubject()
    {
        final DeadboltHandler deadboltHandler = new AbstractNoPreAuthDeadboltHandler()
        {
            @Override
            public F.Promise<Optional<Subject>> getSubject(final Http.Context context)
            {
                return F.Promise.promise(Optional::empty);
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictContent.render(Collections.singletonList(new String[]{"foo", "bar"}),
                                                                                                       deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testNegatedRole_subjectHasRole()
    {
        final DeadboltHandler deadboltHandler = new AbstractNoPreAuthDeadboltHandler()
        {
            @Override
            public F.Promise<Optional<Subject>> getSubject(final Http.Context context)
            {
                return F.Promise.promise(() -> Optional.of(new TestSubject.Builder().role(new TestRole("foo"))
                                                                                    .build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictContent.render(Collections.singletonList(new String[]{"!foo"}),
                                                                                                       deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testNegatedRole_subjectDoesNotHaveRole()
    {
        final DeadboltHandler deadboltHandler = new AbstractNoPreAuthDeadboltHandler()
        {
            @Override
            public F.Promise<Optional<Subject>> getSubject(final Http.Context context)
            {
                return F.Promise.promise(() -> Optional.of(new TestSubject.Builder().role(new TestRole("bar"))
                                                                                    .build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictContent.render(Collections.singletonList(new String[]{"!foo"}),
                                                                                                       deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertTrue(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testNegatedRole_subjectHasMultipleRole()
    {
        final DeadboltHandler deadboltHandler = new AbstractNoPreAuthDeadboltHandler()
        {
            @Override
            public F.Promise<Optional<Subject>> getSubject(final Http.Context context)
            {
                return F.Promise.promise(() -> Optional.of(new TestSubject.Builder().role(new TestRole("foo"))
                                                                                    .role(new TestRole("bar"))
                                                                                    .build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictContent.render(Collections.singletonList(new String[]{"!foo"}),
                                                                                                       deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testNegatedRole_noRolesPresent()
    {
        final DeadboltHandler deadboltHandler = new AbstractNoPreAuthDeadboltHandler()
        {
            @Override
            public F.Promise<Optional<Subject>> getSubject(final Http.Context context)
            {
                return F.Promise.promise(() -> Optional.of(new TestSubject.Builder().build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictContent.render(Collections.singletonList(new String[]{"!foo"}),
                                                                                                       deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertTrue(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testNegatedRole_noSubject()
    {
        final DeadboltHandler deadboltHandler = new AbstractNoPreAuthDeadboltHandler()
        {
            @Override
            public F.Promise<Optional<Subject>> getSubject(final Http.Context context)
            {
                return F.Promise.promise(Optional::empty);
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictContent.render(Collections.singletonList(new String[]{"!foo"}),
                                                                                                       deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertTrue(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testOr_oneSideNegated_fooPresent()
    {
        final DeadboltHandler deadboltHandler = new AbstractNoPreAuthDeadboltHandler()
        {
            @Override
            public F.Promise<Optional<Subject>> getSubject(final Http.Context context)
            {
                return F.Promise.promise(() -> Optional.of(new TestSubject.Builder().role(new TestRole("foo"))
                                                                                    .build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictContent.render(Arrays.asList(new String[]{"!foo"},
                                                                                                                     new String[]{"bar"}),
                                                                                                       deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testOr_oneSideNegated_barPresent()
    {
        final DeadboltHandler deadboltHandler = new AbstractNoPreAuthDeadboltHandler()
        {
            @Override
            public F.Promise<Optional<Subject>> getSubject(final Http.Context context)
            {
                return F.Promise.promise(() -> Optional.of(new TestSubject.Builder().role(new TestRole("bar"))
                                                                                    .build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictContent.render(Arrays.asList(new String[]{"!foo"},
                                                                                                                     new String[]{"bar"}),
                                                                                                       deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertTrue(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testOr_oneSideNegated_bothPresent()
    {
        final DeadboltHandler deadboltHandler = new AbstractNoPreAuthDeadboltHandler()
        {
            @Override
            public F.Promise<Optional<Subject>> getSubject(final Http.Context context)
            {
                return F.Promise.promise(() -> Optional.of(new TestSubject.Builder().role(new TestRole("foo"))
                                                                                    .role(new TestRole("bar"))
                                                                                    .build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictContent.render(Arrays.asList(new String[]{"!foo"},
                                                                                                                     new String[]{"bar"}),
                                                                                                       deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertTrue(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testOr_oneSideNegated_neitherPresent()
    {
        final DeadboltHandler deadboltHandler = new AbstractNoPreAuthDeadboltHandler()
        {
            @Override
            public F.Promise<Optional<Subject>> getSubject(final Http.Context context)
            {
                return F.Promise.promise(() -> Optional.of(new TestSubject.Builder().build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictContent.render(Arrays.asList(new String[]{"!foo"},
                                                                                                                     new String[]{"bar"}),
                                                                                                       deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertTrue(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testOr_oneSideNegated_noSubject()
    {
        final DeadboltHandler deadboltHandler = new AbstractNoPreAuthDeadboltHandler()
        {
            @Override
            public F.Promise<Optional<Subject>> getSubject(final Http.Context context)
            {
                return F.Promise.promise(Optional::empty);
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictContent.render(Collections.singletonList(new String[]{"!foo"}),
                                                                                                       deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertTrue(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testAnd_oneSideNegated_fooPresent()
    {
        final DeadboltHandler deadboltHandler = new AbstractNoPreAuthDeadboltHandler()
        {
            @Override
            public F.Promise<Optional<Subject>> getSubject(final Http.Context context)
            {
                return F.Promise.promise(() -> Optional.of(new TestSubject.Builder().role(new TestRole("foo"))
                                                                                    .build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictContent.render(Collections.singletonList(new String[]{"!foo", "bar"}),
                                                                                                       deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testAnd_oneSideNegated_barPresent()
    {
        final DeadboltHandler deadboltHandler = new AbstractNoPreAuthDeadboltHandler()
        {
            @Override
            public F.Promise<Optional<Subject>> getSubject(final Http.Context context)
            {
                return F.Promise.promise(() -> Optional.of(new TestSubject.Builder().role(new TestRole("bar"))
                                                                                    .build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictContent.render(Collections.singletonList(new String[]{"!foo", "bar"}),
                                                                                                       deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertTrue(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testAnd_oneSideNegated_bothPresent()
    {
        final DeadboltHandler deadboltHandler = new AbstractNoPreAuthDeadboltHandler()
        {
            @Override
            public F.Promise<Optional<Subject>> getSubject(final Http.Context context)
            {
                return F.Promise.promise(() -> Optional.of(new TestSubject.Builder().role(new TestRole("foo"))
                                                                                    .role(new TestRole("bar"))
                                                                                    .build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictContent.render(Collections.singletonList(new String[]{"!foo", "bar"}),
                                                                                                       deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testAnd_oneSideNegated_neitherPresent()
    {
        final DeadboltHandler deadboltHandler = new AbstractNoPreAuthDeadboltHandler()
        {
            @Override
            public F.Promise<Optional<Subject>> getSubject(final Http.Context context)
            {
                return F.Promise.promise(() -> Optional.of(new TestSubject.Builder().build()));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictContent.render(Collections.singletonList(new String[]{"!foo", "bar"}),
                                                                                                       deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testAnd_oneSideNegated_noSubject()
    {
        final DeadboltHandler deadboltHandler = new AbstractNoPreAuthDeadboltHandler()
        {
            @Override
            public F.Promise<Optional<Subject>> getSubject(final Http.Context context)
            {
                return F.Promise.promise(Optional::empty);
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.restrictTest.restrictContent.render(Collections.singletonList(new String[]{"!foo", "bar"}),
                                                                                                       deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }
}