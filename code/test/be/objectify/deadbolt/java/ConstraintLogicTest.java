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

import be.objectify.deadbolt.java.cache.DefaultPatternCache;
import be.objectify.deadbolt.java.cache.HandlerCache;
import be.objectify.deadbolt.java.cache.SubjectCache;
import be.objectify.deadbolt.java.models.Permission;
import be.objectify.deadbolt.java.models.Subject;
import be.objectify.deadbolt.java.testsupport.FakeCache;
import be.objectify.deadbolt.java.testsupport.TestHandlerCache;
import be.objectify.deadbolt.java.testsupport.TestPermission;
import be.objectify.deadbolt.java.testsupport.TestRole;
import be.objectify.deadbolt.java.testsupport.TestSubject;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import play.mvc.Http;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class ConstraintLogicTest extends AbstractFakeApplicationTest
{
    private final Consumer<CompletionStage<Boolean>> pass = future ->
    {
        try
        {
            Assert.assertTrue(future.toCompletableFuture().get());
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    };

    private final Consumer<CompletionStage<Boolean>> fail = future ->
    {
        try
        {
            Assert.assertFalse(future.toCompletableFuture().get());
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    };

    @Test
    public void testRestrict_pass() throws Exception
    {
        testRestrict(new String[]{"foo"},
                     pass);
    }

    @Test
    public void testRestrict_fail() throws Exception
    {
        testRestrict(new String[]{"bar"},
                     fail);
    }

    private void testRestrict(final String[] requiredRoles,
                              final Consumer<CompletionStage<Boolean>> test)
    {
        final ExecutionContextProvider ecProvider = Mockito.mock(ExecutionContextProvider.class);
        Mockito.when(ecProvider.get()).thenReturn(new DefaultDeadboltExecutionContextProvider());
        final SubjectCache subjectCache = Mockito.mock(SubjectCache.class);
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(new TestSubject.Builder().role(new TestRole("foo")).build())));
        final ConstraintLogic logic = new ConstraintLogic(new DeadboltAnalyzer(),
                                                          subjectCache,
                                                          new DefaultPatternCache(new FakeCache()),
                                                          ecProvider);

        final CompletionStage<Boolean> result = logic.restrict(context(),
                                                               handler(() -> new TestSubject.Builder().role(new TestRole("foo")).build()),
                                                               Optional.of("json"),
                                                               () -> Collections.singletonList(requiredRoles),
                                                               ctx -> CompletableFuture.completedFuture(true),
                                                               (ctx, handler, context) -> CompletableFuture.completedFuture(false),
                                                               ConstraintPoint.CONTROLLER);
        test.accept(result);

    }

    @Test
    public void testDynamic_pass() throws Exception
    {
        testDynamic(true,
                    pass);
    }

    @Test
    public void testDynamic_fail() throws Exception
    {
        testDynamic(false,
                    fail);
    }

    private void testDynamic(final boolean allow,
                             final Consumer<CompletionStage<Boolean>> test)
    {
        final ExecutionContextProvider ecProvider = Mockito.mock(ExecutionContextProvider.class);
        Mockito.when(ecProvider.get()).thenReturn(new DefaultDeadboltExecutionContextProvider());
        final SubjectCache subjectCache = Mockito.mock(SubjectCache.class);
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(new TestSubject.Builder().role(new TestRole("foo")).build())));
        final ConstraintLogic logic = new ConstraintLogic(new DeadboltAnalyzer(),
                                                          subjectCache,
                                                          new DefaultPatternCache(new FakeCache()),
                                                          ecProvider);

        final CompletionStage<Boolean> result = logic.dynamic(context(),
                                                              withDrh(() -> new AbstractDynamicResourceHandler()
                                                              {
                                                                  @Override
                                                                  public CompletionStage<Boolean> isAllowed(final String name,
                                                                                                            final Optional<String> meta,
                                                                                                            final DeadboltHandler deadboltHandler,
                                                                                                            final Http.Context ctx)
                                                                  {
                                                                      return CompletableFuture.completedFuture(allow);
                                                                  }
                                                              }),
                                                              Optional.of("json"),
                                                              "foo",
                                                              Optional.of("bar"),
                                                              ctx -> CompletableFuture.completedFuture(true),
                                                              (ctx, handler, context) -> CompletableFuture.completedFuture(false),
                                                              ConstraintPoint.CONTROLLER);
        test.accept(result);

    }

    @Test
    public void testRoleBasedPermissions_noAssociatedRoles()
    {
        testRoleBasedPermissions(new TestSubject.Builder().permission(new TestPermission("a.b.c")).build(),
                                 "foo",
                                 Collections.emptyList(),
                                 fail);
    }

    @Test
    public void testRoleBasedPermissions_exactMatch()
    {
        final TestPermission permission = new TestPermission("a.b.c");
        testRoleBasedPermissions(new TestSubject.Builder().permission(permission).build(),
                                 "foo",
                                 Collections.singletonList(permission),
                                 pass);
    }

    @Test
    public void testRoleBasedPermissions_wildcardMatch()
    {
        testRoleBasedPermissions(new TestSubject.Builder().permission(new TestPermission("a.b.c")).build(),
                                 "foo",
                                 Collections.singletonList(new TestPermission("a.b.*")),
                                 pass);
    }

    @Test
    public void testRoleBasedPermissions_range_pass()
    {
        testRoleBasedPermissions(new TestSubject.Builder().permission(new TestPermission("a.b.c")).build(),
                                 "foo",
                                 Collections.singletonList(new TestPermission("a.[bgj].c")),
                                 pass);
    }

    @Test
    public void testRoleBasedPermissions_range_fail()
    {
        testRoleBasedPermissions(new TestSubject.Builder().permission(new TestPermission("a.b.c")).build(),
                                 "foo",
                                 Collections.singletonList(new TestPermission("a.[qwerty].c")),
                                 fail);
    }

    @Test
    public void testRoleBasedPermissions_multipleSubjectPermissions()
    {
        testRoleBasedPermissions(new TestSubject.Builder().permission(new TestPermission("a.b.c"))
                                                          .permission(new TestPermission("d.e.f"))
                                                          .build(),
                                 "foo",
                                 Collections.singletonList(new TestPermission("d.e.f")),
                                 pass);
    }

    @Test
    public void testRoleBasedPermissions_multipleAssociatedPermissions()
    {
        testRoleBasedPermissions(new TestSubject.Builder().permission(new TestPermission("d.e.f"))
                                                          .build(),
                                 "foo",
                                 Arrays.asList(new TestPermission("a.b.c"),
                                               new TestPermission("d.e.f")),
                                 pass);
    }

    private void testRoleBasedPermissions(final Subject subject,
                                          final String roleName,
                                          final List<? extends Permission> associatedPermissions,
                                          final Consumer<CompletionStage<Boolean>> test)
    {
        final ExecutionContextProvider ecProvider = Mockito.mock(ExecutionContextProvider.class);
        Mockito.when(ecProvider.get()).thenReturn(new DefaultDeadboltExecutionContextProvider());
        final SubjectCache subjectCache = Mockito.mock(SubjectCache.class);
        Mockito.when(subjectCache.apply(Mockito.any(DeadboltHandler.class),
                                        Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(subject)));
        final ConstraintLogic logic = new ConstraintLogic(new DeadboltAnalyzer(),
                                                          subjectCache,
                                                          new DefaultPatternCache(new FakeCache()),
                                                          ecProvider);

        final CompletionStage<Boolean> result = logic.roleBasedPermissions(context(),
                                                                           handler(() -> subject,
                                                                                   associatedPermissions),
                                                                           Optional.of("json"),
                                                                           roleName,
                                                                           ctx -> CompletableFuture.completedFuture(true),
                                                                           (ctx, handler, context) -> CompletableFuture.completedFuture(false),
                                                                           ConstraintPoint.CONTROLLER);
        test.accept(result);
    }

    @Override
    public HandlerCache handlers()
    {
        // using new instances of handlers in the test
        return new TestHandlerCache(null,
                                    new HashMap<>());
    }
}