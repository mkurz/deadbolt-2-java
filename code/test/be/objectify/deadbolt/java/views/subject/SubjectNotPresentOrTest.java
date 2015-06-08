package be.objectify.deadbolt.java.views.subject;

import be.objectify.deadbolt.core.models.Permission;
import be.objectify.deadbolt.core.models.Role;
import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.AbstractFakeApplicationTest;
import be.objectify.deadbolt.java.AbstractNoPreAuthDeadboltHandler;
import org.junit.Assert;
import org.junit.Test;
import play.libs.F;
import play.mvc.Http;
import play.test.Helpers;
import play.twirl.api.Content;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class SubjectNotPresentOrTest extends AbstractFakeApplicationTest
{
    @Test
    public void testWithSubjectPresent()
    {
        final DeadboltHandler deadboltHandler = new AbstractNoPreAuthDeadboltHandler()
        {
            @Override
            public F.Promise<Optional<Subject>> getSubject(final Http.Context context)
            {
                return F.Promise.promise(() -> Optional.of(new Subject()
                {
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
                }));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.subject.subjectNotPresentOrContent.render(deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testWithNoSubjectPresent()
    {
        final Content html = be.objectify.deadbolt.java.views.html.subject.subjectNotPresentOrContent.render(new AbstractNoPreAuthDeadboltHandler(){});
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertTrue(content.contains("This is protected by the constraint."));
        Assert.assertFalse(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }
}
