/*
 * Copyright 2012-2016 Steve Chaloner
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
package be.objectify.deadbolt.java.composite;

import be.objectify.deadbolt.java.DeadboltHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Http;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class DynamicConstraint implements Constraint
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DynamicConstraint.class);

    private final String name;
    private final String meta;

    public DynamicConstraint(final String name,
                             final String meta)
    {
        this.name = name;
        this.meta = meta;
    }

    @Override
    public CompletionStage<Boolean> test(final Http.Context context,
                                         final DeadboltHandler handler,
                                         final Executor executor)
    {
        return handler.getDynamicResourceHandler(context)
                      .thenComposeAsync(maybeDrh -> maybeDrh.map(drh -> drh.isAllowed(name,
                                                                                      meta,
                                                                                      handler,
                                                                                      context))
                                                            .orElseGet(() -> {
                                                                LOGGER.error("No dynamic resource handler found for [{}]", name);
                                                                return CompletableFuture.completedFuture(false);
                                                            }),
                                        executor);
    }
}
