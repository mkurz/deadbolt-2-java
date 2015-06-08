package be.objectify.deadbolt.java.testsupport;

import play.cache.CacheApi;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Persistent and annoying issues with "The play Cache is not alive (STATUS_SHUTDOWN)" during testing, so this stubs out CacheApi.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public class FakeCache implements CacheApi
{
    private final Map<String, Object> map = new HashMap<>();

    @Override
    public <T> T get(String s)
    {
        return (T)map.get(s);
    }

    @Override
    public <T> T getOrElse(String s, Callable<T> callable, int i)
    {
        return getOrElse(s, callable);
    }

    @Override
    public <T> T getOrElse(String s, Callable<T> callable)
    {
        try
        {
            T t = get(s);
            if (t == null) {
                t = callable.call();
                map.put(s, t);
            }
            return t;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    @Override
    public void set(String s, Object o, int i)
    {
        set(s, o);
    }

    @Override
    public void set(String s, Object o)
    {
        map.put(s, o);
    }

    @Override
    public void remove(String s)
    {
        map.remove(s);
    }
}
