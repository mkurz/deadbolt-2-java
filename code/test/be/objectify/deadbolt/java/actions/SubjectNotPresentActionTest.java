/*
 * Copyright 2010-2017 Steve Chaloner
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
package be.objectify.deadbolt.java.actions;

import be.objectify.deadbolt.java.ConstraintLogic;
import be.objectify.deadbolt.java.ConstraintPoint;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.cache.BeforeAuthCheckCache;
import be.objectify.deadbolt.java.cache.HandlerCache;
import be.objectify.deadbolt.java.utils.TriFunction;
import com.typesafe.config.ConfigFactory;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import play.libs.typedmap.TypedKey;
import play.mvc.Action;
import play.mvc.Controller;
import play.mvc.Http;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class SubjectNotPresentActionTest {

    @Test
    public void testConfig() throws Exception
    {
        final SubjectNotPresent subjectNotPresent = Mockito.mock(SubjectNotPresent.class);
        Mockito.when(subjectNotPresent.forceBeforeAuthCheck())
               .thenReturn(true);
        Mockito.when(subjectNotPresent.handlerKey())
               .thenReturn("foo");
        Mockito.when(subjectNotPresent.content())
               .thenReturn("x/y");
        final SubjectNotPresentAction action = new SubjectNotPresentAction(Mockito.mock(HandlerCache.class),
                                                                           Mockito.mock(BeforeAuthCheckCache.class),
                                                                           ConfigFactory.empty(),
                                                                           Mockito.mock(ConstraintLogic.class));
        action.configuration = subjectNotPresent;

        Assert.assertTrue(action.isForceBeforeAuthCheck());
        Assert.assertEquals("foo",
                            action.getHandlerKey());
        Assert.assertEquals("x/y",
                            action.getContent().orElse(null));
    }

    @Test
    public void testPresent() throws Exception
    {
        final SubjectNotPresentAction action = new SubjectNotPresentAction(Mockito.mock(HandlerCache.class),
                                                                           Mockito.mock(BeforeAuthCheckCache.class),
                                                                           ConfigFactory.empty(),
                                                                           Mockito.mock(ConstraintLogic.class));
        final Http.Request request = Mockito.mock(Http.Request.class);
        Mockito.when(request.uri())
               .thenReturn("http://localhost/test");

        final DeadboltHandler handler = Mockito.mock(DeadboltHandler.class);
        action.present(request,
                       handler,
                       Optional.empty());

        Mockito.verify(handler).onAuthFailure(request,
                                              Optional.empty());
    }

    @Test
    public void testNotPresent() throws Exception
    {
        final SubjectNotPresentAction action = new SubjectNotPresentAction(Mockito.mock(HandlerCache.class),
                                                                           Mockito.mock(BeforeAuthCheckCache.class),
                                                                           ConfigFactory.empty(),
                                                                           Mockito.mock(ConstraintLogic.class));
        action.delegate = Mockito.mock(Action.class);

        final Http.Request request = new Http.RequestBuilder().build();
        action.notPresent(request,
                          Mockito.mock(DeadboltHandler.class),
                          Optional.empty());

        Assert.assertTrue(action.isAuthorised());
        Mockito.verify(action.delegate).call(Mockito.any(Http.Request.class));
    }

    @Test
    public void testTestSubject() throws Exception
    {
        final ConstraintLogic constraintLogic = Mockito.mock(ConstraintLogic.class);
        Mockito.when(constraintLogic.subjectNotPresent(Mockito.any(Http.Request.class),
                                                       Mockito.any(DeadboltHandler.class),
                                                       Mockito.eq(Optional.empty()),
                                                       Mockito.any(TriFunction.class),
                                                       Mockito.any(TriFunction.class),
                                                       Mockito.eq(ConstraintPoint.CONTROLLER)))
               .thenReturn(CompletableFuture.completedFuture(Controller.TODO(new Http.RequestBuilder().build())));

        final SubjectNotPresentAction action = new SubjectNotPresentAction(Mockito.mock(HandlerCache.class),
                                                                           Mockito.mock(BeforeAuthCheckCache.class),
                                                                           ConfigFactory.empty(),
                                                                           constraintLogic);
        action.configuration = Mockito.mock(SubjectNotPresent.class);

        action.testSubject(constraintLogic,
                           Mockito.mock(Http.Request.class),
                           Mockito.mock(DeadboltHandler.class)).get();
        Mockito.verify(constraintLogic).subjectNotPresent(Mockito.any(Http.Request.class),
                                                          Mockito.any(DeadboltHandler.class),
                                                          Mockito.eq(Optional.empty()),
                                                          Mockito.any(TriFunction.class),
                                                          Mockito.any(TriFunction.class),
                                                          Mockito.eq(ConstraintPoint.CONTROLLER));
    }
}