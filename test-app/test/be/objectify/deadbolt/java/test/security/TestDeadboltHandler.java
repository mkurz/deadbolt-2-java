package be.objectify.deadbolt.java.test.security;

import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.test.models.User;
import play.libs.F;
import play.mvc.Http;

/**
 * Extends the project's default DeadboltHandler to get the subject based on a cookie value - good for testing ONLY!
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public class TestDeadboltHandler extends MyDeadboltHandler
{
    @Override
    public Subject getSubject(Http.Context context)
    {
        final Http.Cookie userCookie = context.request().cookie("user");
        return User.findByUserName(userCookie.value());
    }
}
