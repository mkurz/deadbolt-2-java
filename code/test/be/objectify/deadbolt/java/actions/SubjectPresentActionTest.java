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
import be.objectify.deadbolt.java.cache.HandlerCache;
import be.objectify.deadbolt.java.utils.TriFunction;
import com.typesafe.config.ConfigFactory;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Results;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class SubjectPresentActionTest {

    @Test
    public void testConfig() throws Exception
    {
        final SubjectPresent subjectPresent = Mockito.mock(SubjectPresent.class);
        Mockito.when(subjectPresent.forceBeforeAuthCheck())
               .thenReturn(true);
        Mockito.when(subjectPresent.handlerKey())
               .thenReturn("foo");
        Mockito.when(subjectPresent.content())
               .thenReturn("x/y");
        final SubjectPresentAction action = new SubjectPresentAction(Mockito.mock(HandlerCache.class),
                                                                     ConfigFactory.empty(),
                                                                     Mockito.mock(ConstraintLogic.class));
        action.configuration = subjectPresent;

        final AbstractSubjectAction<SubjectPresent>.Config config = action.config();
        Assert.assertTrue(config.forceBeforeAuthCheck);
        Assert.assertEquals("foo",
                            config.handlerKey);
        Assert.assertEquals("x/y",
                            config.content.orElse(null));
    }

    @Test
    public void testPresent() throws Exception
    {
        final SubjectPresentAction action = new SubjectPresentAction(Mockito.mock(HandlerCache.class),
                                                                     ConfigFactory.empty(),
                                                                     Mockito.mock(ConstraintLogic.class));
        action.delegate = Mockito.mock(Action.class);

        final Http.Context ctx = Mockito.mock(Http.Context.class);
        ctx.args = new HashMap<>();
        action.present(ctx,
                       Mockito.mock(DeadboltHandler.class),
                       Optional.empty());

        Assert.assertTrue((Boolean)ctx.args.get("deadbolt.action-authorised"));
        Mockito.verify(action.delegate).call(ctx);
    }

    @Test
    public void testNotPresent() throws Exception
    {
        final SubjectPresentAction action = new SubjectPresentAction(Mockito.mock(HandlerCache.class),
                                                                     ConfigFactory.empty(),
                                                                     Mockito.mock(ConstraintLogic.class));

        final Http.Context ctx = Mockito.mock(Http.Context.class);
        ctx.args = new HashMap<>();
        final Http.Request request = Mockito.mock(Http.Request.class);
        Mockito.when(request.uri())
               .thenReturn("http://localhost/test");
        Mockito.when(ctx.request())
               .thenReturn(request);

        final DeadboltHandler handler = Mockito.mock(DeadboltHandler.class);
        action.notPresent(ctx,
                          handler,
                          Optional.empty());

        Assert.assertTrue((Boolean)ctx.args.get("deadbolt.action-unauthorised"));
        Mockito.verify(handler).onAuthFailure(ctx,
                                              Optional.empty());
    }
    @Test
    public void testTestSubject() throws Exception
    {
        final ConstraintLogic constraintLogic = Mockito.mock(ConstraintLogic.class);
        Mockito.when(constraintLogic.subjectPresent(Mockito.any(Http.Context.class),
                                                    Mockito.any(DeadboltHandler.class),
                                                    Mockito.eq(Optional.empty()),
                                                    Mockito.any(TriFunction.class),
                                                    Mockito.any(TriFunction.class),
                                                    Mockito.eq(ConstraintPoint.CONTROLLER)))
               .thenReturn(CompletableFuture.completedFuture(Results.TODO));

        final SubjectPresentAction action = new SubjectPresentAction(Mockito.mock(HandlerCache.class),
                                                                     ConfigFactory.empty(),
                                                                     constraintLogic);
        action.configuration = Mockito.mock(SubjectPresent.class);

        action.testSubject(constraintLogic,
                           Mockito.mock(Http.Context.class),
                           action.config(),
                           Mockito.mock(DeadboltHandler.class)).get();
        Mockito.verify(constraintLogic).subjectPresent(Mockito.any(Http.Context.class),
                                                       Mockito.any(DeadboltHandler.class),
                                                       Mockito.eq(Optional.empty()),
                                                       Mockito.any(TriFunction.class),
                                                       Mockito.any(TriFunction.class),
                                                       Mockito.eq(ConstraintPoint.CONTROLLER));
    }
}