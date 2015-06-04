package be.objectify.deadbolt.java.test.security;

import be.objectify.deadbolt.core.models.Permission;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.F;
import play.mvc.Http;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class CompositeDynamicResourceHandler implements DynamicResourceHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CompositeDynamicResourceHandler.class);

    private final Map<String, DynamicResourceHandler> delegates = new HashMap<>();

    CompositeDynamicResourceHandler(final Map<String, DynamicResourceHandler> delegates)
    {
        this.delegates.putAll(delegates);
    }

    @Override
    public F.Promise<Boolean> isAllowed(final String name,
                                        final String meta,
                                        final DeadboltHandler deadboltHandler,
                                        final Http.Context ctx)
    {
        final DynamicResourceHandler delegate = delegates.get(name);
        final F.Promise<Boolean> result;
        if (delegate == null)
        {
            LOGGER.error("No DynamicResourceHandler with name [{}] found, denying access",
                         name);
            result = F.Promise.pure(false);
        }
        else
        {
            result = delegate.isAllowed(name,
                                        meta,
                                        deadboltHandler,
                                        ctx);
        }
        return result;
    }

    @Override
    public F.Promise<Boolean> checkPermission(final String permissionValue,
                                              final DeadboltHandler deadboltHandler,
                                              final Http.Context ctx)
    {
        // this can be completely arbitrary, but to keep things simple for testing we're
        // just checking for zombies...just like I do every night before I go to bed
        return deadboltHandler.getSubject(ctx)
                .map(option -> {
                    boolean allow = false;
                    if (option.isPresent())
                    {
                        final List<? extends Permission> permissions = option.get()
                                                                             .getPermissions();
                        for (Iterator<? extends Permission> iterator = permissions.iterator(); !allow && iterator.hasNext(); )
                        {
                            final Permission permission = iterator.next();
                            final String value = permission.getValue();
                            allow = value != null && value.contains("zombie");
                        }
                    }
                    return allow;
                });
    }
}
