/*
 * Copyright 2010-2016 Steve Chaloner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
        return (T) map.get(s);
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
            if (t == null)
            {
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
