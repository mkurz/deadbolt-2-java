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
package be.objectify.deadbolt.java.filters;

import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.utils.TriFunction;
import play.mvc.Http;
import play.mvc.Result;

import java.util.concurrent.CompletionStage;
import java.util.function.Function;

/**
 * @author Steve Chaloner (steve@objectify.be)
 * @since 2.5.1
 */
public interface FilterFunction extends TriFunction<Http.RequestHeader, DeadboltHandler, Function<Http.RequestHeader, CompletionStage<Result>>, CompletionStage<Result>>
{
    /**
     * Test the constraint against the current request.
     *
     * @param requestHeader the request header
     * @param handler       the deadbolt handler
     * @param onSuccess     a function to process the request if the constraint test passes
     * @return a future for the result
     */
    @Override
    CompletionStage<Result> apply(Http.RequestHeader requestHeader,
                                  DeadboltHandler handler,
                                  Function<Http.RequestHeader, CompletionStage<Result>> onSuccess);
}
