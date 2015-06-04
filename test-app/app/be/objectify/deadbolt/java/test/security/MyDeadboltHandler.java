package be.objectify.deadbolt.java.test.security;

import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.AbstractDeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class MyDeadboltHandler extends AbstractDeadboltHandler
{
    private final DynamicResourceHandler dynamicHandler;

    public MyDeadboltHandler()
    {
        Map<String, DynamicResourceHandler> delegates = new HashMap<>();
        delegates.put("niceName",
                      new NiceNameDynamicResourceHandler());
        this.dynamicHandler = new CompositeDynamicResourceHandler(delegates);
    }

    @Override
    public F.Promise<Optional<Subject>> getSubject(Http.Context context)
    {
        // Example using play-authenticate
//        final AuthUserIdentity identity = PlayAuthenticate.getUser(context);
//                return User.<Subject>findByAuthUserIdentity(identity);
        return super.getSubject(context);
    }

    @Override
    public F.Promise<Optional<Result>> beforeAuthCheck(Http.Context context)
    {
        return F.Promise.pure(Optional.empty());
    }

    @Override
    public F.Promise<Optional<DynamicResourceHandler>> getDynamicResourceHandler(Http.Context context)
    {
        return F.Promise.promise(() -> Optional.of(dynamicHandler));
    }
}
