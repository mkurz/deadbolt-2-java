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
package be.objectify.deadbolt.java;

import play.mvc.Http;

import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * Throws a runtime exception when a required {@link DynamicResourceHandler} is not found.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public class ExceptionThrowingDynamicResourceHandler implements DynamicResourceHandler
{
    public static final DynamicResourceHandler INSTANCE = new ExceptionThrowingDynamicResourceHandler();

    private ExceptionThrowingDynamicResourceHandler()
    {
        // no-op
    }

    @Override
    public CompletionStage<Boolean> isAllowed(final String name,
                                              final Optional<String> meta,
                                              final DeadboltHandler deadboltHandler,
                                              final Http.RequestHeader requestHeader)
    {
        throw new RuntimeException(String.format("A dynamic resource with name [%s] is specified but no dynamic resource handler is provided",
                                                 name));
    }

    @Override
    public CompletionStage<Boolean> checkPermission(final String permissionValue,
                                                    final Optional<String> meta,
                                                    final DeadboltHandler deadboltHandler,
                                                    final Http.RequestHeader requestHeader)
    {
        throw new RuntimeException(String.format("A custom permission type is specified for value [%s] but no dynamic resource handler is provided",
                                                 permissionValue));
    }
}
