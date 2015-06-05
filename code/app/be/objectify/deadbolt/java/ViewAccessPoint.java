package be.objectify.deadbolt.java;

import be.objectify.deadbolt.java.cache.HandlerCache;
import play.Play;

/**
 * I can't inject {@link DeadboltViewSupport} directly into views, so it's hack time...
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public class ViewAccessPoint
{
    public final static DeadboltViewSupport VIEW_SUPPORT;
    public final static HandlerCache HANDLER_CACHE;

    static {
        VIEW_SUPPORT = Play.application().injector().instanceOf(DeadboltViewSupport.class);
        HANDLER_CACHE = Play.application().injector().instanceOf(HandlerCache.class);
    }
}
