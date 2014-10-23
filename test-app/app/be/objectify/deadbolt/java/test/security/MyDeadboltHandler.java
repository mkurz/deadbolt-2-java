package be.objectify.deadbolt.java.test.security;

import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.AbstractDeadboltHandler;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class MyDeadboltHandler extends AbstractDeadboltHandler
{
    @Override
    public F.Promise<Subject> getSubject(Http.Context context)
    {
        // Example using play-authenticate
//        final AuthUserIdentity identity = PlayAuthenticate.getUser(context);
//        return F.Promise.promise(new F.Function0<Subject>()
//        {
//            @Override
//            public Subject apply() throws Throwable
//            {
//                return User.<Subject>findByAuthUserIdentity(identity);
//            }
//        });
        return super.getSubject(context);
    }

    @Override
    public F.Promise<Result> beforeAuthCheck(Http.Context context)
    {
        return F.Promise.pure(null);
    }
}
