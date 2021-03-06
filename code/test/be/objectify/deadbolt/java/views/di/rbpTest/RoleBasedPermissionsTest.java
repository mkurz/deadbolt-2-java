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
package be.objectify.deadbolt.java.views.di.rbpTest;

import be.objectify.deadbolt.java.AbstractFakeApplicationTest;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.NoPreAuthDeadboltHandler;
import be.objectify.deadbolt.java.cache.HandlerCache;
import be.objectify.deadbolt.java.models.Permission;
import be.objectify.deadbolt.java.models.Subject;
import be.objectify.deadbolt.java.testsupport.TestHandlerCache;
import be.objectify.deadbolt.java.testsupport.TestPermission;
import be.objectify.deadbolt.java.testsupport.TestSubject;
import be.objectify.deadbolt.java.views.html.di.rbpTest.roleBasedPermissionsContent;
import be.objectify.deadbolt.java.views.html.di.roleBasedPermissions;
import org.junit.Assert;
import org.junit.Test;
import play.mvc.Http;
import play.test.Helpers;
import play.twirl.api.Content;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class RoleBasedPermissionsTest extends AbstractFakeApplicationTest
{
    @Test
    public void testNoPermissionsForRole()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler()
        {
            @Override
            public CompletionStage<Optional<? extends Subject>> getSubject(final Http.RequestHeader requestHeader)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new TestSubject.Builder().permission(new TestPermission("bar"))
                                                                                                .build()));
            }

            @Override
            public CompletionStage<List<? extends Permission>> getPermissionsForRole(final String roleName)
            {
                return CompletableFuture.completedFuture(Collections.emptyList());
            }
        };
        final Content html = roleBasedPermissionsContent().render("foo",
                                                                  deadboltHandler, new Http.RequestBuilder().build());
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testMatchingPermissionsForRole()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler()
        {
            @Override
            public CompletionStage<Optional<? extends Subject>> getSubject(final Http.RequestHeader requestHeader)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new TestSubject.Builder().permission(new TestPermission("bar"))
                                                                                                .build()));
            }

            @Override
            public CompletionStage<List<? extends Permission>> getPermissionsForRole(final String roleName)
            {
                return CompletableFuture.completedFuture(Collections.singletonList(new TestPermission("bar")));
            }
        };
        final Content html = roleBasedPermissionsContent().render("foo",
                                                                  deadboltHandler, new Http.RequestBuilder().build());
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertTrue(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testNoMatchingPermissionsForRole()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler()
        {
            @Override
            public CompletionStage<Optional<? extends Subject>> getSubject(final Http.RequestHeader requestHeader)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new TestSubject.Builder().permission(new TestPermission("bar"))
                                                                                                .build()));
            }

            @Override
            public CompletionStage<List<? extends Permission>> getPermissionsForRole(final String roleName)
            {
                return CompletableFuture.completedFuture(Collections.singletonList(new TestPermission("hurdy")));
            }
        };
        final Content html = roleBasedPermissionsContent().render("foo",
                                                                  deadboltHandler, new Http.RequestBuilder().build());
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testNoSubject()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler()
        {
            @Override
            public CompletionStage<Optional<? extends Subject>> getSubject(final Http.RequestHeader requestHeader)
            {
                return CompletableFuture.completedFuture(Optional.empty());
            }

            @Override
            public CompletionStage<List<? extends Permission>> getPermissionsForRole(final String roleName)
            {
                return CompletableFuture.completedFuture(Collections.singletonList(new TestPermission("hurdy")));
            }
        };
        final Content html = roleBasedPermissionsContent().render("foo",
                                                                  deadboltHandler, new Http.RequestBuilder().build());
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    private roleBasedPermissionsContent roleBasedPermissionsContent() {
        return new roleBasedPermissionsContent(new roleBasedPermissions(viewSupport(),
                                                                        handlers()));
    }

    public HandlerCache handlers()
    {
        return new TestHandlerCache(null,
                                    new HashMap<>());
    }
}