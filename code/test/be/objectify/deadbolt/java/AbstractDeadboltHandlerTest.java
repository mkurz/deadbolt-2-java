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

import be.objectify.deadbolt.java.models.Subject;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

/**
 * Tests the behaviour of {#link AbstractDeadboltHandler}.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public class AbstractDeadboltHandlerTest
{
    @Test
    public void testGetSubject() throws Exception
    {
        DeadboltHandler deadboltHandler = new AbstractDeadboltHandler()
        {
            @Override
            public CompletionStage<Optional<Result>> beforeAuthCheck(final Http.RequestHeader requestHeader, final Optional<String> content)
            {
                return CompletableFuture.completedFuture(Optional.empty());
            }
        };

        final Http.RequestHeader requestHeader = Mockito.mock(Http.Request.class);

        final CompletionStage<Optional<? extends Subject>> promise = deadboltHandler.getSubject(requestHeader);
        Assert.assertNotNull(promise);

        final Optional<? extends Subject> option = promise.toCompletableFuture().get(1000,
                                                                                     TimeUnit.MILLISECONDS);
        Assert.assertNotNull(option);
        Assert.assertFalse(option.isPresent());
    }

    @Test
    public void testOnAuthFailure() throws Exception
    {
        DeadboltHandler deadboltHandler = new AbstractDeadboltHandler()
        {
            @Override
            public CompletionStage<Optional<Result>> beforeAuthCheck(final Http.RequestHeader requestHeader, final Optional<String> content)
            {
                return CompletableFuture.completedFuture(Optional.empty());
            }
        };

        final Http.RequestHeader requestHeader = new Http.RequestBuilder().build();

        final CompletionStage<Result> promise = deadboltHandler.onAuthFailure(requestHeader,
                                                                              Optional.of("foo"));
        Assert.assertNotNull(promise);

        final Result result = promise.toCompletableFuture().get(100,
                                                                TimeUnit.MILLISECONDS);
        Assert.assertNotNull(result);
        Assert.assertEquals(401,
                            result.status());
    }

    @Test
    public void testGetDynamicResourceHandler() throws Exception
    {
        DeadboltHandler deadboltHandler = new AbstractDeadboltHandler()
        {
            @Override
            public CompletionStage<Optional<Result>> beforeAuthCheck(final Http.RequestHeader requestHeader, final Optional<String> content)
            {
                return CompletableFuture.completedFuture(Optional.empty());
            }
        };

        final Http.RequestHeader requestHeader = Mockito.mock(Http.Request.class);

        final CompletionStage<Optional<DynamicResourceHandler>> promise = deadboltHandler.getDynamicResourceHandler(requestHeader);
        Assert.assertNotNull(promise);

        final Optional<DynamicResourceHandler> option = promise.toCompletableFuture().get(1000,
                                                                                          TimeUnit.MILLISECONDS);
        Assert.assertNotNull(option);
        Assert.assertFalse(option.isPresent());
    }
}
