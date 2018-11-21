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

import be.objectify.deadbolt.java.ConfigKeys;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Http;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class CompositeDynamicResourceHandler implements DynamicResourceHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(CompositeDynamicResourceHandler.class);

    private final Map<String, DynamicResourceHandler> delegates = new HashMap<>();

    CompositeDynamicResourceHandler(final Map<String, DynamicResourceHandler> delegates)
    {
        this.delegates.putAll(delegates);
    }

    @Override
    public CompletionStage<Boolean> isAllowed(final String name,
                                              final Optional<String> meta,
                                              final DeadboltHandler deadboltHandler,
                                              final Http.RequestHeader requestHeader)
    {
        final DynamicResourceHandler delegate = delegates.get(name);
        final CompletionStage<Boolean> result;
        if (delegate == null)
        {
            LOGGER.error("No DynamicResourceHandler with name [{}] found, denying access",
                         name);
            result = CompletableFuture.completedFuture(false);
        }
        else
        {
            result = delegate.isAllowed(name,
                                        meta,
                                        deadboltHandler,
                                        requestHeader);
        }
        return result;
    }

    @Override
    public CompletionStage<Boolean> checkPermission(final String permissionValue,
                                                    final Optional<String> meta,
                                                    final DeadboltHandler deadboltHandler,
                                                    final Http.RequestHeader requestHeader)
    {
        // this can be completely arbitrary, but to keep things simple for testing we're
        // just checking for zombies...just like I do every night before I go to bed
        return deadboltHandler.getSubject(requestHeader)
                              .thenApply(option -> option.map(subject -> subject.getPermissions()
                                                                                .stream()
                                                                                .filter(perm -> perm.getValue().contains("zombie"))
                                                                                .count() > 0)
                                                         .orElseGet(() -> (Boolean) requestHeader.attrs().getOptional(ConfigKeys.PATTERN_INVERT)
                                                                                                                      .orElse(false)));
    }
}
