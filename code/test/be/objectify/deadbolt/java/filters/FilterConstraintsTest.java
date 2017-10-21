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

import be.objectify.deadbolt.java.AbstractDynamicResourceHandler;
import be.objectify.deadbolt.java.ConstraintLogic;
import be.objectify.deadbolt.java.DeadboltAnalyzer;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.DefaultDeadboltExecutionContextProvider;
import be.objectify.deadbolt.java.ExecutionContextProvider;
import be.objectify.deadbolt.java.cache.CompositeCache;
import be.objectify.deadbolt.java.cache.DefaultCompositeCache;
import be.objectify.deadbolt.java.cache.DefaultPatternCache;
import be.objectify.deadbolt.java.cache.DefaultSubjectCache;
import be.objectify.deadbolt.java.cache.SubjectCache;
import be.objectify.deadbolt.java.composite.SubjectPresentConstraint;
import be.objectify.deadbolt.java.models.PatternType;
import be.objectify.deadbolt.java.models.Subject;
import be.objectify.deadbolt.java.testsupport.TestCookies;
import be.objectify.deadbolt.java.testsupport.TestPermission;
import be.objectify.deadbolt.java.testsupport.TestRole;
import be.objectify.deadbolt.java.testsupport.TestSubject;
import com.typesafe.config.ConfigFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import play.core.j.HttpExecutionContext;
import play.libs.typedmap.TypedEntry;
import play.libs.typedmap.TypedMap;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import play.routing.HandlerDef;
import play.routing.Router;

import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class FilterConstraintsTest
{
    private final DeadboltAnalyzer analyzer = new DeadboltAnalyzer();
    private FilterConstraints filterConstraints;
    private Http.RequestHeader requestHeader;
    private SubjectCache subjectCache;
    private Http.Context context;
    private DeadboltHandler handler;
    private ConstraintLogic constraintLogic;

    @Before
    public void setUp()
    {
        final ExecutionContextProvider ecProvider = Mockito.mock(ExecutionContextProvider.class);
        Mockito.when(ecProvider.get())
               .thenReturn(new DefaultDeadboltExecutionContextProvider(HttpExecutionContext.fromThread(Executors.newSingleThreadExecutor())));

        handler = Mockito.mock(DeadboltHandler.class);
        Mockito.when(handler.beforeAuthCheck(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(handler.onAuthFailure(Mockito.any(Http.Context.class),
                                           Mockito.any(Optional.class)))
               .thenReturn(CompletableFuture.completedFuture(Results.forbidden()));

        context = Mockito.mock(Http.Context.class);
        subjectCache = new DefaultSubjectCache(ConfigFactory.empty(),
                                               ecProvider);

        constraintLogic = new ConstraintLogic(analyzer,
                                              subjectCache,
                                              new DefaultPatternCache());

        final CompositeCache compositeCache = new DefaultCompositeCache();
        compositeCache.register("testConstraint",
                                new SubjectPresentConstraint(Optional.empty(),
                                                             constraintLogic));

        filterConstraints = new FilterConstraints(constraintLogic,
                                                  compositeCache);

        requestHeader = Mockito.mock(Http.RequestHeader.class);

        final HandlerDef handlerDef = Mockito.mock(HandlerDef.class);
        Mockito.when(handlerDef.path()).thenReturn("/foo");
        Mockito.when(handlerDef.verb()).thenReturn("GET");
        final TypedMap attrs = TypedMap.create(new TypedEntry(Router.Attrs.HANDLER_DEF,
                                                              handlerDef));
        Mockito.when(requestHeader.attrs()).thenReturn(attrs);
        Mockito.when(requestHeader.method()).thenReturn("GET");
        Mockito.when(requestHeader.uri()).thenReturn("http://localhost/foo");
        Mockito.when(requestHeader.clientCertificateChain()).thenReturn(Optional.empty());
        Mockito.when(requestHeader.cookies()).thenReturn(new TestCookies());
    }

    @After
    public void tearDown()
    {
        filterConstraints = null;
        requestHeader = null;
        subjectCache = null;
        handler = null;
    }

    @Test
    public void testSubjectPresent_pass() throws Exception
    {
        final boolean[] flag = {false};
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));
        final CompletionStage<Result> eventualResult = filterConstraints.subjectPresent()
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertTrue(flag[0]);
    }

    @Test
    public void testSubjectPresent_withContent_pass() throws Exception
    {
        final boolean[] flag = {false};
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));
        final CompletionStage<Result> eventualResult = filterConstraints.subjectPresent(Optional.of("json"))
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertTrue(flag[0]);
    }

    @Test
    public void testSubjectPresent_fail() throws Exception
    {
        final boolean[] flag = {false};
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        final CompletionStage<Result> eventualResult = filterConstraints.subjectPresent()
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1)).onAuthFailure(Mockito.any(Http.Context.class),
                                                       Mockito.eq(Optional.empty()));
    }

    @Test
    public void testSubjectPresent_withContent_fail() throws Exception
    {
        final boolean[] flag = {false};
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        final CompletionStage<Result> eventualResult = filterConstraints.subjectPresent(Optional.of("json"))
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1)).onAuthFailure(Mockito.any(Http.Context.class),
                                                       Mockito.eq(Optional.of("json")));
    }

    @Test
    public void testSubjectNotPresent_pass() throws Exception
    {
        final boolean[] flag = {false};
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        final CompletionStage<Result> eventualResult = filterConstraints.subjectNotPresent()
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertTrue(flag[0]);
    }

    @Test
    public void testSubjectNotPresent_withContent_pass() throws Exception
    {
        final boolean[] flag = {false};
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        final CompletionStage<Result> eventualResult = filterConstraints.subjectNotPresent(Optional.of("json"))
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertTrue(flag[0]);
    }

    @Test
    public void testSubjectNotPresent_fail() throws Exception
    {
        final boolean[] flag = {false};
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));
        final CompletionStage<Result> eventualResult = filterConstraints.subjectNotPresent()
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1)).onAuthFailure(Mockito.any(Http.Context.class),
                                                       Mockito.eq(Optional.empty()));
    }

    @Test
    public void testSubjectNotPresent_withContent_fail() throws Exception
    {
        final boolean[] flag = {false};
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));
        final CompletionStage<Result> eventualResult = filterConstraints.subjectNotPresent(Optional.of("json"))
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1)).onAuthFailure(Mockito.any(Http.Context.class),
                                                       Mockito.eq(Optional.of("json")));
    }

    @Test
    public void testRestrict_pass() throws Exception
    {
        final boolean[] flag = {false};
        final TestSubject subject = new TestSubject.Builder().role(new TestRole("foo"))
                                                             .build();
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(subject)));
        final CompletionStage<Result> eventualResult = filterConstraints.restrict(Collections.singletonList(new String[]{"foo"}))
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertTrue(flag[0]);
    }

    @Test
    public void testRestrict_withContent_pass() throws Exception
    {
        final boolean[] flag = {false};
        final TestSubject subject = new TestSubject.Builder().role(new TestRole("foo"))
                                                             .build();
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(subject)));
        final CompletionStage<Result> eventualResult = filterConstraints.restrict(Collections.singletonList(new String[]{"foo"}),
                                                                                  Optional.of("json"))
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertTrue(flag[0]);
    }

    @Test
    public void testRestrict_fail() throws Exception
    {
        final boolean[] flag = {false};
        final TestSubject subject = new TestSubject.Builder().role(new TestRole("foo"))
                                                             .build();
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(subject)));
        final CompletionStage<Result> eventualResult = filterConstraints.restrict(Collections.singletonList(new String[]{"hurdy"}))
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1)).onAuthFailure(Mockito.any(Http.Context.class),
                                                       Mockito.eq(Optional.empty()));
    }

    @Test
    public void testRestrict_withContent_fail() throws Exception
    {
        final boolean[] flag = {false};
        final TestSubject subject = new TestSubject.Builder().role(new TestRole("foo"))
                                                             .build();
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(subject)));
        final CompletionStage<Result> eventualResult = filterConstraints.restrict(Collections.singletonList(new String[]{"hurdy"}),
                                                                                  Optional.of("json"))
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1)).onAuthFailure(Mockito.any(Http.Context.class),
                                                       Mockito.eq(Optional.of("json")));
    }

    @Test
    public void testPattern_equality_pass() throws Exception
    {
        final boolean[] flag = {false};
        final TestSubject subject = new TestSubject.Builder().permission(new TestPermission("foo"))
                                                             .build();
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(subject)));
        final CompletionStage<Result> eventualResult = filterConstraints.pattern("foo",
                                                                                 PatternType.EQUALITY)
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertTrue(flag[0]);
    }

    @Test
    public void testPattern_regex_pass() throws Exception
    {
        final boolean[] flag = {false};
        final TestSubject subject = new TestSubject.Builder().permission(new TestPermission("foo.bar"))
                                                             .build();
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(subject)));
        final CompletionStage<Result> eventualResult = filterConstraints.pattern("foo.*",
                                                                                 PatternType.REGEX)
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertTrue(flag[0]);
    }

    @Test
    public void testPattern_custom_pass() throws Exception
    {
        final boolean[] flag = {false};
        context.args = new HashMap<>();
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));
        Mockito.when(handler.getDynamicResourceHandler(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(new AbstractDynamicResourceHandler()
               {
                   @Override
                   public CompletionStage<Boolean> checkPermission(final String permissionValue,
                                                                   final Optional<String> meta,
                                                                   final DeadboltHandler deadboltHandler,
                                                                   final Http.Context ctx)
                   {
                       return CompletableFuture.completedFuture(Boolean.TRUE);
                   }
               })));
        final CompletionStage<Result> eventualResult = filterConstraints.pattern("foo",
                                                                                 PatternType.CUSTOM)
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertTrue(flag[0]);
    }

    @Test
    public void testPattern_equality_fail() throws Exception
    {
        final boolean[] flag = {false};
        final TestSubject subject = new TestSubject.Builder().permission(new TestPermission("foo"))
                                                             .build();
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(subject)));
        final CompletionStage<Result> eventualResult = filterConstraints.pattern("bar",
                                                                                 PatternType.EQUALITY)
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1)).onAuthFailure(Mockito.any(Http.Context.class),
                                                       Mockito.eq(Optional.empty()));
    }

    @Test
    public void testPattern_regex_fail() throws Exception
    {
        final boolean[] flag = {false};
        final TestSubject subject = new TestSubject.Builder().permission(new TestPermission("bar.foo"))
                                                             .build();
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(subject)));
        final CompletionStage<Result> eventualResult = filterConstraints.pattern("foo.*",
                                                                                 PatternType.REGEX)
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1)).onAuthFailure(Mockito.any(Http.Context.class),
                                                       Mockito.eq(Optional.empty()));
    }

    @Test
    public void testPattern_custom_fail() throws Exception
    {
        final boolean[] flag = {false};
        context.args = new HashMap<>();
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));
        Mockito.when(handler.getDynamicResourceHandler(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(new AbstractDynamicResourceHandler()
               {
                   @Override
                   public CompletionStage<Boolean> checkPermission(final String permissionValue,
                                                                   final Optional<String> meta,
                                                                   final DeadboltHandler deadboltHandler,
                                                                   final Http.Context ctx)
                   {
                       return CompletableFuture.completedFuture(Boolean.FALSE);
                   }
               })));
        final CompletionStage<Result> eventualResult = filterConstraints.pattern("foo",
                                                                                 PatternType.CUSTOM)
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1)).onAuthFailure(Mockito.any(Http.Context.class),
                                                       Mockito.eq(Optional.empty()));
    }

    @Test
    public void testPattern_equality_withMeta_pass() throws Exception
    {
        final boolean[] flag = {false};
        final TestSubject subject = new TestSubject.Builder().permission(new TestPermission("foo"))
                                                             .build();
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(subject)));
        final CompletionStage<Result> eventualResult = filterConstraints.pattern("foo",
                                                                                 PatternType.EQUALITY,
                                                                                 Optional.of("moo"))
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertTrue(flag[0]);
    }

    @Test
    public void testPattern_regex_withMeta_pass() throws Exception
    {
        final boolean[] flag = {false};
        final TestSubject subject = new TestSubject.Builder().permission(new TestPermission("foo.bar"))
                                                             .build();
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(subject)));
        final CompletionStage<Result> eventualResult = filterConstraints.pattern("foo.*",
                                                                                 PatternType.REGEX,
                                                                                 Optional.of("moo"))
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertTrue(flag[0]);
    }

    @Test
    public void testPattern_custom_withMeta_pass() throws Exception
    {
        final boolean[] flag = {false};
        context.args = new HashMap<>();
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));
        Mockito.when(handler.getDynamicResourceHandler(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(new AbstractDynamicResourceHandler()
               {
                   @Override
                   public CompletionStage<Boolean> checkPermission(final String permissionValue,
                                                                   final Optional<String> meta,
                                                                   final DeadboltHandler deadboltHandler,
                                                                   final Http.Context ctx)
                   {
                       return CompletableFuture.completedFuture(meta.orElse("meh").equals("moo"));
                   }
               })));
        final CompletionStage<Result> eventualResult = filterConstraints.pattern("foo",
                                                                                 PatternType.CUSTOM,
                                                                                 Optional.of("moo"))
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertTrue(flag[0]);
    }

    @Test
    public void testPattern_equality_withMeta_fail() throws Exception
    {
        final boolean[] flag = {false};
        final TestSubject subject = new TestSubject.Builder().permission(new TestPermission("foo"))
                                                             .build();
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(subject)));
        final CompletionStage<Result> eventualResult = filterConstraints.pattern("bar",
                                                                                 PatternType.EQUALITY,
                                                                                 Optional.of("moo"))
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1)).onAuthFailure(Mockito.any(Http.Context.class),
                                                       Mockito.eq(Optional.empty()));
    }

    @Test
    public void testPattern_regex_withMeta_fail() throws Exception
    {
        final boolean[] flag = {false};
        final TestSubject subject = new TestSubject.Builder().permission(new TestPermission("bar.foo"))
                                                             .build();
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(subject)));
        final CompletionStage<Result> eventualResult = filterConstraints.pattern("foo.*",
                                                                                 PatternType.REGEX,
                                                                                 Optional.of("moo"))
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1)).onAuthFailure(Mockito.any(Http.Context.class),
                                                       Mockito.eq(Optional.empty()));
    }

    @Test
    public void testPattern_custom_withMeta_fail() throws Exception
    {
        final boolean[] flag = {false};
        context.args = new HashMap<>();
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));
        Mockito.when(handler.getDynamicResourceHandler(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(new AbstractDynamicResourceHandler()
               {
                   @Override
                   public CompletionStage<Boolean> checkPermission(final String permissionValue,
                                                                   final Optional<String> meta,
                                                                   final DeadboltHandler deadboltHandler,
                                                                   final Http.Context ctx)
                   {
                       return CompletableFuture.completedFuture(meta.orElse("meh").equals("moo"));
                   }
               })));
        final CompletionStage<Result> eventualResult = filterConstraints.pattern("foo",
                                                                                 PatternType.CUSTOM,
                                                                                 Optional.of("bluergh"))
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1)).onAuthFailure(Mockito.any(Http.Context.class),
                                                       Mockito.eq(Optional.empty()));
    }

    @Test
    public void testPattern_equality_invert_pass() throws Exception
    {
        final boolean[] flag = {false};
        final TestSubject subject = new TestSubject.Builder().permission(new TestPermission("foo"))
                                                             .build();
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(subject)));
        final CompletionStage<Result> eventualResult = filterConstraints.pattern("foo",
                                                                                 PatternType.EQUALITY,
                                                                                 true)
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1)).onAuthFailure(Mockito.any(Http.Context.class),
                                                       Mockito.eq(Optional.empty()));
    }

    @Test
    public void testPattern_regex_invert_pass() throws Exception
    {
        final boolean[] flag = {false};
        final TestSubject subject = new TestSubject.Builder().permission(new TestPermission("foo.bar"))
                                                             .build();
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(subject)));
        final CompletionStage<Result> eventualResult = filterConstraints.pattern("foo.*",
                                                                                 PatternType.REGEX,
                                                                                 true)
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1)).onAuthFailure(Mockito.any(Http.Context.class),
                                                       Mockito.eq(Optional.empty()));
    }

    @Test
    public void testPattern_custom_invert_pass() throws Exception
    {
        final boolean[] flag = {false};
        context.args = new HashMap<>();
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));
        Mockito.when(handler.getDynamicResourceHandler(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(new AbstractDynamicResourceHandler()
               {
                   @Override
                   public CompletionStage<Boolean> checkPermission(final String permissionValue,
                                                                   final Optional<String> meta,
                                                                   final DeadboltHandler deadboltHandler,
                                                                   final Http.Context ctx)
                   {
                       return CompletableFuture.completedFuture(Boolean.TRUE);
                   }
               })));
        final CompletionStage<Result> eventualResult = filterConstraints.pattern("foo",
                                                                                 PatternType.CUSTOM,
                                                                                 true)
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1)).onAuthFailure(Mockito.any(Http.Context.class),
                                                       Mockito.eq(Optional.empty()));
    }

    @Test
    public void testPattern_equality_invert_fail() throws Exception
    {
        final boolean[] flag = {false};
        final TestSubject subject = new TestSubject.Builder().permission(new TestPermission("foo"))
                                                             .build();
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(subject)));
        final CompletionStage<Result> eventualResult = filterConstraints.pattern("bar",
                                                                                 PatternType.EQUALITY,
                                                                                 true)
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertTrue(flag[0]);
    }

    @Test
    public void testPattern_regex_invert_fail() throws Exception
    {
        final boolean[] flag = {false};
        final TestSubject subject = new TestSubject.Builder().permission(new TestPermission("bar.foo"))
                                                             .build();
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(subject)));
        final CompletionStage<Result> eventualResult = filterConstraints.pattern("foo.*",
                                                                                 PatternType.REGEX,
                                                                                 true)
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertTrue(flag[0]);
    }

    @Test
    public void testPattern_custom_invert_fail() throws Exception
    {
        final boolean[] flag = {false};
        context.args = new HashMap<>();
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));
        Mockito.when(handler.getDynamicResourceHandler(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(new AbstractDynamicResourceHandler()
               {
                   @Override
                   public CompletionStage<Boolean> checkPermission(final String permissionValue,
                                                                   final Optional<String> meta,
                                                                   final DeadboltHandler deadboltHandler,
                                                                   final Http.Context ctx)
                   {
                       return CompletableFuture.completedFuture(Boolean.FALSE);
                   }
               })));
        final CompletionStage<Result> eventualResult = filterConstraints.pattern("foo",
                                                                                 PatternType.CUSTOM,
                                                                                 true)
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertTrue(flag[0]);
    }

    @Test
    public void testPattern_equality_allArgs_pass() throws Exception
    {
        final boolean[] flag = {false};
        final TestSubject subject = new TestSubject.Builder().permission(new TestPermission("foo"))
                                                             .build();
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(subject)));
        final CompletionStage<Result> eventualResult = filterConstraints.pattern("foo",
                                                                                 PatternType.EQUALITY,
                                                                                 Optional.of("moo"),
                                                                                 false,
                                                                                 Optional.of("json"))
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertTrue(flag[0]);
    }

    @Test
    public void testPattern_regex_allArgs_pass() throws Exception
    {
        final boolean[] flag = {false};
        final TestSubject subject = new TestSubject.Builder().permission(new TestPermission("foo.bar"))
                                                             .build();
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(subject)));
        final CompletionStage<Result> eventualResult = filterConstraints.pattern("foo.*",
                                                                                 PatternType.REGEX,
                                                                                 Optional.of("moo"),
                                                                                 false,
                                                                                 Optional.of("json"))
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertTrue(flag[0]);
    }

    @Test
    public void testPattern_custom_allArgs_pass() throws Exception
    {
        final boolean[] flag = {false};
        context.args = new HashMap<>();
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));
        Mockito.when(handler.getDynamicResourceHandler(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(new AbstractDynamicResourceHandler()
               {
                   @Override
                   public CompletionStage<Boolean> checkPermission(final String permissionValue,
                                                                   final Optional<String> meta,
                                                                   final DeadboltHandler deadboltHandler,
                                                                   final Http.Context ctx)
                   {
                       return CompletableFuture.completedFuture(meta.orElse("meh").equals("moo"));
                   }
               })));
        final CompletionStage<Result> eventualResult = filterConstraints.pattern("foo",
                                                                                 PatternType.CUSTOM,
                                                                                 Optional.of("moo"),
                                                                                 false,
                                                                                 Optional.of("json"))
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertTrue(flag[0]);
    }

    @Test
    public void testPattern_equality_allArgs_fail() throws Exception
    {
        final boolean[] flag = {false};
        final TestSubject subject = new TestSubject.Builder().permission(new TestPermission("foo"))
                                                             .build();
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(subject)));
        final CompletionStage<Result> eventualResult = filterConstraints.pattern("bar",
                                                                                 PatternType.EQUALITY,
                                                                                 Optional.of("moo"),
                                                                                 false,
                                                                                 Optional.of("json"))
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1)).onAuthFailure(Mockito.any(Http.Context.class),
                                                       Mockito.eq(Optional.of("json")));
    }

    @Test
    public void testPattern_regex_allArgs_fail() throws Exception
    {
        final boolean[] flag = {false};
        final TestSubject subject = new TestSubject.Builder().permission(new TestPermission("bar.foo"))
                                                             .build();
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(subject)));
        final CompletionStage<Result> eventualResult = filterConstraints.pattern("foo.*",
                                                                                 PatternType.REGEX,
                                                                                 Optional.of("moo"),
                                                                                 false,
                                                                                 Optional.of("json"))
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1)).onAuthFailure(Mockito.any(Http.Context.class),
                                                       Mockito.eq(Optional.of("json")));
    }

    @Test
    public void testPattern_custom_allArgs_fail() throws Exception
    {
        final boolean[] flag = {false};
        context.args = new HashMap<>();
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));
        Mockito.when(handler.getDynamicResourceHandler(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(new AbstractDynamicResourceHandler()
               {
                   @Override
                   public CompletionStage<Boolean> checkPermission(final String permissionValue,
                                                                   final Optional<String> meta,
                                                                   final DeadboltHandler deadboltHandler,
                                                                   final Http.Context ctx)
                   {
                       return CompletableFuture.completedFuture(meta.orElse("meh").equals("moo"));
                   }
               })));
        final CompletionStage<Result> eventualResult = filterConstraints.pattern("foo",
                                                                                 PatternType.CUSTOM,
                                                                                 Optional.of("bluergh"),
                                                                                 false,
                                                                                 Optional.of("json"))
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1)).onAuthFailure(Mockito.any(Http.Context.class),
                                                       Mockito.eq(Optional.of("json")));
    }

    @Test
    public void testPattern_equality_allArgs_invert_pass() throws Exception
    {
        final boolean[] flag = {false};
        final TestSubject subject = new TestSubject.Builder().permission(new TestPermission("foo"))
                                                             .build();
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(subject)));
        final CompletionStage<Result> eventualResult = filterConstraints.pattern("foo",
                                                                                 PatternType.EQUALITY,
                                                                                 Optional.of("moo"),
                                                                                 true,
                                                                                 Optional.of("json"))
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1)).onAuthFailure(Mockito.any(Http.Context.class),
                                                       Mockito.eq(Optional.of("json")));
    }

    @Test
    public void testPattern_regex_allArgs_invert_pass() throws Exception
    {
        final boolean[] flag = {false};
        final TestSubject subject = new TestSubject.Builder().permission(new TestPermission("foo.bar"))
                                                             .build();
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(subject)));
        final CompletionStage<Result> eventualResult = filterConstraints.pattern("foo.*",
                                                                                 PatternType.REGEX,
                                                                                 Optional.of("moo"),
                                                                                 true,
                                                                                 Optional.of("json"))
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1)).onAuthFailure(Mockito.any(Http.Context.class),
                                                       Mockito.eq(Optional.of("json")));
    }

    @Test
    public void testPattern_custom_allArgs_invert_pass() throws Exception
    {
        final boolean[] flag = {false};
        context.args = new HashMap<>();
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));
        Mockito.when(handler.getDynamicResourceHandler(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(new AbstractDynamicResourceHandler()
               {
                   @Override
                   public CompletionStage<Boolean> checkPermission(final String permissionValue,
                                                                   final Optional<String> meta,
                                                                   final DeadboltHandler deadboltHandler,
                                                                   final Http.Context ctx)
                   {
                       return CompletableFuture.completedFuture(meta.orElse("meh").equals("moo"));
                   }
               })));
        final CompletionStage<Result> eventualResult = filterConstraints.pattern("foo",
                                                                                 PatternType.CUSTOM,
                                                                                 Optional.of("moo"),
                                                                                 true,
                                                                                 Optional.of("json"))
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1)).onAuthFailure(Mockito.any(Http.Context.class),
                                                       Mockito.eq(Optional.of("json")));
    }

    @Test
    public void testPattern_equality_allArgs_invert_fail() throws Exception
    {
        final boolean[] flag = {false};
        final TestSubject subject = new TestSubject.Builder().permission(new TestPermission("foo"))
                                                             .build();
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(subject)));
        final CompletionStage<Result> eventualResult = filterConstraints.pattern("bar",
                                                                                 PatternType.EQUALITY,
                                                                                 Optional.of("moo"),
                                                                                 true,
                                                                                 Optional.of("json"))
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertTrue(flag[0]);
    }

    @Test
    public void testPattern_regex_allArgs_invert_fail() throws Exception
    {
        final boolean[] flag = {false};
        final TestSubject subject = new TestSubject.Builder().permission(new TestPermission("bar.foo"))
                                                             .build();
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(subject)));
        final CompletionStage<Result> eventualResult = filterConstraints.pattern("foo.*",
                                                                                 PatternType.REGEX,
                                                                                 Optional.of("moo"),
                                                                                 true,
                                                                                 Optional.of("json"))
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertTrue(flag[0]);
    }

    @Test
    public void testPattern_custom_allArgs_invert_fail() throws Exception
    {
        final boolean[] flag = {false};
        context.args = new HashMap<>();
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));
        Mockito.when(handler.getDynamicResourceHandler(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(new AbstractDynamicResourceHandler()
               {
                   @Override
                   public CompletionStage<Boolean> checkPermission(final String permissionValue,
                                                                   final Optional<String> meta,
                                                                   final DeadboltHandler deadboltHandler,
                                                                   final Http.Context ctx)
                   {
                       return CompletableFuture.completedFuture(meta.orElse("meh").equals("moo"));
                   }
               })));
        final CompletionStage<Result> eventualResult = filterConstraints.pattern("foo",
                                                                                 PatternType.CUSTOM,
                                                                                 Optional.of("bluergh"),
                                                                                 true,
                                                                                 Optional.of("json"))
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertTrue(flag[0]);
    }

    @Test
    public void testDynamic_pass() throws Exception
    {
        final boolean[] flag = {false};
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));
        Mockito.when(handler.getDynamicResourceHandler(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(new AbstractDynamicResourceHandler()
               {
                   @Override
                   public CompletionStage<Boolean> isAllowed(final String name,
                                                             final Optional<String> meta,
                                                             final DeadboltHandler deadboltHandler,
                                                             final Http.Context ctx)
                   {
                       return CompletableFuture.completedFuture(Boolean.TRUE);
                   }
               })));
        final CompletionStage<Result> eventualResult = filterConstraints.dynamic("foo")
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertTrue(flag[0]);
    }

    @Test
    public void testDynamic_withMeta_pass() throws Exception
    {
        final boolean[] flag = {false};
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));
        Mockito.when(handler.getDynamicResourceHandler(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(new AbstractDynamicResourceHandler()
               {
                   @Override
                   public CompletionStage<Boolean> isAllowed(final String name,
                                                             final Optional<String> meta,
                                                             final DeadboltHandler deadboltHandler,
                                                             final Http.Context ctx)
                   {
                       return CompletableFuture.completedFuture(meta.orElse("meh").equals("moo"));
                   }
               })));
        final CompletionStage<Result> eventualResult = filterConstraints.dynamic("foo",
                                                                                 Optional.of("moo"))
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertTrue(flag[0]);
    }

    @Test
    public void testDynamic_withMetaAndContent_pass() throws Exception
    {
        final boolean[] flag = {false};
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));
        Mockito.when(handler.getDynamicResourceHandler(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(new AbstractDynamicResourceHandler()
               {
                   @Override
                   public CompletionStage<Boolean> isAllowed(final String name,
                                                             final Optional<String> meta,
                                                             final DeadboltHandler deadboltHandler,
                                                             final Http.Context ctx)
                   {
                       return CompletableFuture.completedFuture(meta.orElse("meh").equals("moo"));
                   }
               })));
        final CompletionStage<Result> eventualResult = filterConstraints.dynamic("foo",
                                                                                 Optional.of("moo"),
                                                                                 Optional.of("json"))
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertTrue(flag[0]);
    }

    @Test
    public void testDynamic_fail() throws Exception
    {
        final boolean[] flag = {false};
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));
        Mockito.when(handler.getDynamicResourceHandler(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(new AbstractDynamicResourceHandler()
               {
                   @Override
                   public CompletionStage<Boolean> isAllowed(final String name,
                                                             final Optional<String> meta,
                                                             final DeadboltHandler deadboltHandler,
                                                             final Http.Context ctx)
                   {
                       return CompletableFuture.completedFuture(Boolean.FALSE);
                   }
               })));
        final CompletionStage<Result> eventualResult = filterConstraints.dynamic("foo")
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1)).onAuthFailure(Mockito.any(Http.Context.class),
                                                       Mockito.eq(Optional.empty()));
    }

    @Test
    public void testDynamic_withMeta_fail() throws Exception
    {
        final boolean[] flag = {false};
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));
        Mockito.when(handler.getDynamicResourceHandler(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(new AbstractDynamicResourceHandler()
               {
                   @Override
                   public CompletionStage<Boolean> isAllowed(final String name,
                                                             final Optional<String> meta,
                                                             final DeadboltHandler deadboltHandler,
                                                             final Http.Context ctx)
                   {
                       return CompletableFuture.completedFuture(meta.orElse("meh").equals("moo"));
                   }
               })));
        final CompletionStage<Result> eventualResult = filterConstraints.dynamic("foo",
                                                                                 Optional.of("bleurgh"))
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1)).onAuthFailure(Mockito.any(Http.Context.class),
                                                       Mockito.eq(Optional.empty()));
    }

    @Test
    public void testDynamic_withMetaAndContent_fail() throws Exception
    {
        final boolean[] flag = {false};
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));
        Mockito.when(handler.getDynamicResourceHandler(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(new AbstractDynamicResourceHandler()
               {
                   @Override
                   public CompletionStage<Boolean> isAllowed(final String name,
                                                             final Optional<String> meta,
                                                             final DeadboltHandler deadboltHandler,
                                                             final Http.Context ctx)
                   {
                       return CompletableFuture.completedFuture(meta.orElse("meh").equals("moo"));
                   }
               })));
        final CompletionStage<Result> eventualResult = filterConstraints.dynamic("foo",
                                                                                 Optional.of("bleurgh"),
                                                                                 Optional.of("json"))
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1)).onAuthFailure(Mockito.any(Http.Context.class),
                                                       Mockito.eq(Optional.of("json")));
    }

    @Test
    public void testComposite_byName_pass() throws Exception
    {
        final boolean[] flag = {false};
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));
        final CompletionStage<Result> eventualResult = filterConstraints.composite("testConstraint")
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertTrue(flag[0]);
    }

    @Test
    public void testComposite_byName_withContent_pass() throws Exception
    {
        final boolean[] flag = {false};
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));
        final CompletionStage<Result> eventualResult = filterConstraints.composite("testConstraint",
                                                                                   Optional.of("json"))
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertTrue(flag[0]);
    }

    @Test
    public void testComposite_byName_fail() throws Exception
    {
        final boolean[] flag = {false};
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        final CompletionStage<Result> eventualResult = filterConstraints.composite("testConstraint")
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1)).onAuthFailure(Mockito.any(Http.Context.class),
                                                       Mockito.eq(Optional.empty()));
    }

    @Test
    public void testComposite_byName_withContent_fail() throws Exception
    {
        final boolean[] flag = {false};
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        final CompletionStage<Result> eventualResult = filterConstraints.composite("testConstraint",
                                                                                   Optional.of("json"))
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1)).onAuthFailure(Mockito.any(Http.Context.class),
                                                       Mockito.eq(Optional.of("json")));
    }

    @Test
    public void testComposite_byConstraint_pass() throws Exception
    {
        final boolean[] flag = {false};
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));
        final CompletionStage<Result> eventualResult = filterConstraints.composite(new SubjectPresentConstraint(Optional.empty(),
                                                                                                                constraintLogic))
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertTrue(flag[0]);
    }

    @Test
    public void testComposite_byConstraint_withContent_pass() throws Exception
    {
        final boolean[] flag = {false};
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(Mockito.mock(Subject.class))));
        final CompletionStage<Result> eventualResult = filterConstraints.composite(new SubjectPresentConstraint(Optional.empty(),
                                                                                                                constraintLogic),
                                                                                   Optional.of("json"))
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertTrue(flag[0]);
    }

    @Test
    public void testComposite_byConstraint_fail() throws Exception
    {
        final boolean[] flag = {false};
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(handler.getPermissionsForRole("foo"))
               .then(invocation -> Collections.singletonList(new TestPermission("bar")));
        final CompletionStage<Result> eventualResult = filterConstraints.composite(new SubjectPresentConstraint(Optional.empty(),
                                                                                                                constraintLogic))
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1)).onAuthFailure(Mockito.any(Http.Context.class),
                                                       Mockito.eq(Optional.empty()));
    }

    @Test
    public void testComposite_byConstraint_withContent_fail() throws Exception
    {
        final boolean[] flag = {false};
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));
        Mockito.when(handler.getPermissionsForRole("foo"))
               .then(invocation -> Collections.singletonList(new TestPermission("bar")));
        final CompletionStage<Result> eventualResult = filterConstraints.composite(new SubjectPresentConstraint(Optional.empty(),
                                                                                                                constraintLogic),
                                                                                   Optional.of("json"))
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1)).onAuthFailure(Mockito.any(Http.Context.class),
                                                       Mockito.eq(Optional.of("json")));
    }

    @Test
    public void testRoleBasedPermissions_pass() throws Exception
    {
        final boolean[] flag = {false};
        final TestSubject subject = new TestSubject.Builder().permission(new TestPermission("bar"))
                                                             .build();
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(subject)));
        Mockito.when(handler.getPermissionsForRole("foo"))
               .then(invocation -> CompletableFuture.completedFuture(Collections.singletonList(new TestPermission("bar"))));
        final CompletionStage<Result> eventualResult = filterConstraints.roleBasedPermissions("foo")
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertTrue(flag[0]);
    }

    @Test
    public void testRoleBasedPermissions_withContent_pass() throws Exception
    {
        final boolean[] flag = {false};
        final TestSubject subject = new TestSubject.Builder().permission(new TestPermission("bar"))
                                                             .build();
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(subject)));
        Mockito.when(handler.getPermissionsForRole("foo"))
               .then(invocation -> CompletableFuture.completedFuture(Collections.singletonList(new TestPermission("bar"))));
        final CompletionStage<Result> eventualResult = filterConstraints.roleBasedPermissions("foo",
                                                                                              Optional.of("json"))
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertTrue(flag[0]);
    }

    @Test
    public void testRoleBasedPermissions_fail() throws Exception
    {
        final boolean[] flag = {false};
        final TestSubject subject = new TestSubject.Builder().permission(new TestPermission("hurdy"))
                                                             .build();
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(subject)));
        Mockito.when(handler.getPermissionsForRole("foo"))
               .then(invocation -> CompletableFuture.completedFuture(Collections.singletonList(new TestPermission("bar"))));

        final CompletionStage<Result> eventualResult = filterConstraints.roleBasedPermissions("foo")
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1)).onAuthFailure(Mockito.any(Http.Context.class),
                                                       Mockito.eq(Optional.empty()));
    }

    @Test
    public void testRoleBasedPermissions_withContent_fail() throws Exception
    {
        final boolean[] flag = {false};
        final TestSubject subject = new TestSubject.Builder().permission(new TestPermission("hurdy"))
                                                             .build();
        Mockito.when(handler.getSubject(context))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(subject)));
        Mockito.when(handler.getPermissionsForRole("foo"))
               .then(invocation -> CompletableFuture.completedFuture(Collections.singletonList(new TestPermission("bar"))));

        final CompletionStage<Result> eventualResult = filterConstraints.roleBasedPermissions("foo",
                                                                                              Optional.of("json"))
                                                                        .apply(context,
                                                                               requestHeader,
                                                                               handler,
                                                                               rh ->
                                                                               {
                                                                                   flag[0] = true;
                                                                                   return CompletableFuture.completedFuture(Results.ok());
                                                                               });
        ((CompletableFuture) eventualResult).get();
        Assert.assertFalse(flag[0]);
        Mockito.verify(handler,
                       Mockito.times(1)).onAuthFailure(Mockito.any(Http.Context.class),
                                                       Mockito.eq(Optional.of("json")));
    }
}