package be.objectify.deadbolt.java.test.security;

import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.AbstractDeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import com.google.common.collect.Maps;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class MyDeadboltHandler extends AbstractDeadboltHandler
{
    private final DynamicResourceHandler dynamicHandler;

    public MyDeadboltHandler()
    {
        Map<String, DynamicResourceHandler> delegates = new HashMap<String, DynamicResourceHandler>();
        delegates.put("niceName",
                      new NiceNameDynamicResourceHandler());
        this.dynamicHandler = new CompositeDynamicResourceHandler(delegates);
    }

    @Override
    public Subject getSubject(Http.Context context)
    {
        // Example using play-authenticate
//        final AuthUserIdentity identity = PlayAuthenticate.getUser(context);
//                return User.<Subject>findByAuthUserIdentity(identity);
        return super.getSubject(context);
    }

    @Override
    public F.Promise<Result> beforeAuthCheck(Http.Context context)
    {
        return F.Promise.pure(null);
    }

    @Override
    public DynamicResourceHandler getDynamicResourceHandler(Http.Context context)
    {
        return dynamicHandler;
    }
}
