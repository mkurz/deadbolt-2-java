package be.objectify.deadbolt.java.views.subject;

import be.objectify.deadbolt.java.AbstractFakeApplicationTest;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.cache.HandlerCache;
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
public class SubjectNotPresentOrTest extends AbstractFakeApplicationTest
{
    private final HandlerCache handlers = handlers();

    @Test
    public void testWithSubjectPresent()
    {
        final Content html = be.objectify.deadbolt.java.views.html.subject.subjectNotPresentOrContent.render(handlers.apply("present"));
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testWithNoSubjectPresent()
    {
        final Content html = be.objectify.deadbolt.java.views.html.subject.subjectNotPresentOrContent.render(handlers.apply("notPresent"));
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertTrue(content.contains("This is protected by the constraint."));
        Assert.assertFalse(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    public HandlerCache handlers()
    {
        final Map<String, DeadboltHandler> handlers = new HashMap<>();

        handlers.put("present", handler(() -> new TestSubject.Builder().build()));
        handlers.put("notPresent", handler(() -> null));

        return new DefaultHandlerCache(null,
                                       handlers);
    }
}
