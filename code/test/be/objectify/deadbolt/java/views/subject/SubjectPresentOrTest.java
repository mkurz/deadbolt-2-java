package be.objectify.deadbolt.java.views.subject;

import be.objectify.deadbolt.core.models.Permission;
import be.objectify.deadbolt.core.models.Role;
import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.AbstractFakeApplicationTest;
import be.objectify.deadbolt.java.AbstractNoPreAuthDeadboltHandler;
import org.junit.Assert;
import org.junit.Test;
import play.mvc.Http;
import play.test.Helpers;
import play.twirl.api.Content;

import java.util.Collections;
import java.util.List;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class SubjectPresentOrTest extends AbstractFakeApplicationTest
{
    @Test
    public void testWithSubjectPresent()
    {
        final DeadboltHandler deadboltHandler = new AbstractNoPreAuthDeadboltHandler()
        {
            @Override
            public Subject getSubject(Http.Context context)
            {
                return new Subject() {
                    @Override
                    public List<? extends Role> getRoles()
                    {
                        return Collections.emptyList();
                    }

                    @Override
                    public List<? extends Permission> getPermissions()
                    {
                        return Collections.emptyList();
                    }

                    @Override
                    public String getIdentifier()
                    {
                        return "foo";
                    }
                };
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.subject.subjectPresentOrContent.render(deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertTrue(content.contains("This is protected by the constraint."));
        Assert.assertFalse(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testWithNoSubjectPresent()
    {
        final Content html = be.objectify.deadbolt.java.views.html.subject.subjectPresentOrContent.render(new AbstractNoPreAuthDeadboltHandler(){});
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }
}
