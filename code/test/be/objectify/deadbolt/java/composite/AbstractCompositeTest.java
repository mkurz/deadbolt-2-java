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
package be.objectify.deadbolt.java.composite;

import be.objectify.deadbolt.java.AbstractDeadboltHandler;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import be.objectify.deadbolt.java.models.Permission;
import be.objectify.deadbolt.java.models.Role;
import be.objectify.deadbolt.java.models.Subject;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public abstract class AbstractCompositeTest
{
    protected DeadboltHandler withDrh(final DynamicResourceHandler drh)
    {
        return new AbstractDeadboltHandler()
        {
            @Override
            public CompletionStage<Optional<Result>> beforeAuthCheck(Http.RequestHeader requestHeader, Optional<String> content)
            {
                return CompletableFuture.completedFuture(Optional.empty());
            }

            @Override
            public CompletionStage<Optional<DynamicResourceHandler>> getDynamicResourceHandler(Http.RequestHeader requestHeader)
            {
                return CompletableFuture.completedFuture(Optional.of(drh));
            }
        };
    }

    protected DeadboltHandler withSubject(final Supplier<Subject> subject)
    {
        return new AbstractDeadboltHandler()
        {
            @Override
            public CompletionStage<Optional<Result>> beforeAuthCheck(final Http.RequestHeader requestHeader, final Optional<String> content)
            {
                return CompletableFuture.completedFuture(Optional.empty());
            }

            @Override
            public CompletionStage<Optional<? extends Subject>> getSubject(final Http.RequestHeader requestHeader)
            {
                return CompletableFuture.completedFuture(Optional.ofNullable(subject.get()));
            }
        };
    }

    protected Subject subject()
    {
        return subject(Collections::emptyList,
                       Collections::emptyList);
    }

    protected Subject subject(final Permission... permissions)
    {
        return subject(Collections::emptyList,
                       () -> Arrays.asList(permissions));
    }

    protected Subject subject(final Role... roles)
    {
        return subject(() -> Arrays.asList(roles),
                       Collections::emptyList);
    }

    protected boolean toBoolean(final CompletionStage<F.Tuple<Boolean, Http.RequestHeader>> cs) throws Exception
    {
        return ((CompletableFuture<F.Tuple<Boolean, Http.RequestHeader>>) cs).get()._1;
    }

    protected Subject subject(Supplier<List<? extends Role>> roles,
                              Supplier<List<? extends Permission>> permissions)
    {
        return new Subject()
        {
            @Override
            public List<? extends Role> getRoles()
            {
                return roles.get();
            }

            @Override
            public List<? extends Permission> getPermissions()
            {
                return permissions.get();
            }

            @Override
            public String getIdentifier()
            {
                return "foo";
            }
        };
    }
}
