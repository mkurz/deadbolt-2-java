package be.objectify.deadbolt.java.test.security;

import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.cache.HandlerCache;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class MyHandlerCache implements HandlerCache
{
    private final DeadboltHandler handler;

    public MyHandlerCache(final DeadboltHandler handler)
    {
        this.handler = handler;
    }

    @Override
    public DeadboltHandler apply(String s)
    {
        return handler;
    }

    @Override
    public DeadboltHandler get()
    {
        return handler;
    }
}
