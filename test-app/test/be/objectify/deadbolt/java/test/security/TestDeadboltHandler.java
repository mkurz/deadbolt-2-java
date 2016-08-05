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

import be.objectify.deadbolt.java.ExecutionContextProvider;
import be.objectify.deadbolt.java.models.Subject;
import be.objectify.deadbolt.java.test.models.User;
import play.mvc.Http;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Extends the project's default DeadboltHandler to get the subject based on a cookie value - good for testing ONLY!
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public class TestDeadboltHandler extends MyDeadboltHandler
{
    @Inject
    public TestDeadboltHandler(ExecutionContextProvider ecProvider)
    {
        super(ecProvider);
    }

    @Override
    public CompletionStage<Optional<? extends Subject>> getSubject(Http.Context context)
    {
        final Http.Cookie userCookie = context.request().cookie("user");
        return CompletableFuture.supplyAsync(() -> Optional.ofNullable(User.findByUserName(userCookie.value())));
    }
}
