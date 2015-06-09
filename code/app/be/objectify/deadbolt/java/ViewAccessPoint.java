package be.objectify.deadbolt.java;

import be.objectify.deadbolt.java.cache.HandlerCache;
import play.Play;
import play.inject.Injector;

/**
 * I can't inject {@link ViewSupport} directly into views, so it's hack time...
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public class ViewAccessPoint
{
    public final static ViewSupport VIEW_SUPPORT;
    public final static HandlerCache HANDLER_CACHE;

    static {
        final Injector injector = Play.application().injector();
        VIEW_SUPPORT = injector.instanceOf(ViewSupport.class);
        HANDLER_CACHE = injector.instanceOf(HandlerCache.class);
    }
}
