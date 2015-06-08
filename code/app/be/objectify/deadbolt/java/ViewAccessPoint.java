package be.objectify.deadbolt.java;

import be.objectify.deadbolt.java.cache.HandlerCache;
import play.Play;

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
        VIEW_SUPPORT = Play.application().injector().instanceOf(ViewSupport.class);
        HANDLER_CACHE = Play.application().injector().instanceOf(HandlerCache.class);
    }
}
