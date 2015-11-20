/*
 * Copyright 2012 Steve Chaloner
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

import be.objectify.deadbolt.core.DeadboltAnalyzer;
import play.mvc.Http;

import javax.inject.Singleton;
import java.util.concurrent.CompletionStage;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@Singleton
public class JavaAnalyzer extends DeadboltAnalyzer
{
    /**
     * Checks a custom pattern using the {@link be.objectify.deadbolt.java.DynamicResourceHandler} obtained via the handler.
     *
     * @param handler the handler
     * @param context the context
     * @param value the pattern value
     * @return true iff the custom check succeeds
     */
    public CompletionStage<Boolean> checkCustomPattern(final DeadboltHandler handler,
                                                       final Http.Context context,
                                                       final String value)
    {
        return handler.getDynamicResourceHandler(context)
                      .thenApply(option -> option.orElseGet(() -> ExceptionThrowingDynamicResourceHandler.INSTANCE))
                      .thenCompose(drh -> drh.checkPermission(value,
                                                              handler,
                                                              context));
    }
}
