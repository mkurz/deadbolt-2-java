package be.objectify.deadbolt.java.views.dynamicTest;

import be.objectify.deadbolt.java.AbstractDynamicResourceHandler;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import be.objectify.deadbolt.java.AbstractFakeApplicationTest;
import be.objectify.deadbolt.java.AbstractNoPreAuthDeadboltHandler;
import org.junit.Assert;
import org.junit.Test;
import play.mvc.Http;
import play.test.Helpers;
import play.twirl.api.Content;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class DynamicTest extends AbstractFakeApplicationTest
{
    @Test
    public void testValid()
    {
        final DeadboltHandler deadboltHandler = new AbstractNoPreAuthDeadboltHandler()
        {
            @Override
            public DynamicResourceHandler getDynamicResourceHandler(Http.Context context)
            {
                return new AbstractDynamicResourceHandler()
                {
                    @Override
                    public boolean isAllowed(String name,
                                             String meta,
                                             DeadboltHandler deadboltHandler, Http.Context ctx)
                    {
                        return true;
                    }
                };
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.dynamicTest.dynamicContent.render("foo",
                                                                                                     "bar",
                                                                                                     deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertTrue(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testName()
    {
        final DeadboltHandler deadboltHandler = new AbstractNoPreAuthDeadboltHandler()
        {
            @Override
            public DynamicResourceHandler getDynamicResourceHandler(Http.Context context)
            {
                return new AbstractDynamicResourceHandler()
                {
                    @Override
                    public boolean isAllowed(String name,
                                             String meta,
                                             DeadboltHandler deadboltHandler, Http.Context ctx)
                    {
                        return "foo".equals(name);
                    }
                };
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.dynamicTest.dynamicContent.render("foo",
                                                                                                     "bar",
                                                                                                     deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertTrue(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testMeta()
    {
        final DeadboltHandler deadboltHandler = new AbstractNoPreAuthDeadboltHandler()
        {
            @Override
            public DynamicResourceHandler getDynamicResourceHandler(Http.Context context)
            {
                return new AbstractDynamicResourceHandler()
                {
                    @Override
                    public boolean isAllowed(String name,
                                             String meta,
                                             DeadboltHandler deadboltHandler, Http.Context ctx)
                    {
                        return "bar".equals(meta);
                    }
                };
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.dynamicTest.dynamicContent.render("foo",
                                                                                                     "bar",
                                                                                                     deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertTrue(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testInvalid()
    {
        final DeadboltHandler deadboltHandler = new AbstractNoPreAuthDeadboltHandler()
        {
            @Override
            public DynamicResourceHandler getDynamicResourceHandler(Http.Context context)
            {
                return new AbstractDynamicResourceHandler()
                {
                    @Override
                    public boolean isAllowed(String name, String meta, DeadboltHandler deadboltHandler, Http.Context ctx)
                    {
                        return false;
                    }
                };
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.dynamicTest.dynamicContent.render("foo",
                                                                                                     "bar",
                                                                                                     deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }
}