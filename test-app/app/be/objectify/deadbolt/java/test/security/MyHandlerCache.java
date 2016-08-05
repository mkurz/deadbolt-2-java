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
package be.objectify.deadbolt.java.test.security;

import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.cache.HandlerCache;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class MyHandlerCache implements HandlerCache
{
    private final DeadboltHandler handler;

    private final Map<String, DeadboltHandler> handlers = new HashMap<>();

    @Inject
    public MyHandlerCache(@HandlerQualifiers.MainHandler final DeadboltHandler handler,
                          @HandlerQualifiers.SomeOtherHandler final DeadboltHandler otherHandler)
    {
        this.handler = handler;
        this.handlers.put(handler.handlerName(),
                          handler);
        this.handlers.put(otherHandler.handlerName(),
                          otherHandler);
    }

    @Override
    public DeadboltHandler apply(final String name)
    {
        return handlers.get(name);
    }

    @Override
    public DeadboltHandler get()
    {
        return handler;
    }
}
