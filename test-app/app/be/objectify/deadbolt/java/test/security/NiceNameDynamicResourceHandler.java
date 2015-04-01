package be.objectify.deadbolt.java.test.security;

import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import play.mvc.Http;

/**
 * Dedicated handler to look for people with the same name as my wife.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public class NiceNameDynamicResourceHandler implements DynamicResourceHandler
{
    @Override
    public boolean isAllowed(final String name,
                             final String meta,
                             final DeadboltHandler deadboltHandler,
                             final Http.Context ctx)
    {
        final Subject subject = deadboltHandler.getSubject(ctx);
        return subject != null && subject.getIdentifier()
                                         .contains("greet");
    }

    @Override
    public boolean checkPermission(final String permissionValue,
                                   final DeadboltHandler deadboltHandler,
                                   final Http.Context ctx)
    {
        return false;
    }
}
