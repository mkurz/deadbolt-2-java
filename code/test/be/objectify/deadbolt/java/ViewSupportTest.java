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

import be.objectify.deadbolt.java.cache.HandlerCache;
import be.objectify.deadbolt.java.cache.PatternCache;
import be.objectify.deadbolt.java.models.PatternType;
import be.objectify.deadbolt.java.testsupport.TestHandlerCache;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import play.Configuration;
import play.mvc.Http;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class ViewSupportTest extends AbstractFakeApplicationTest
{
    private final HandlerCache handlerCache;

    public ViewSupportTest()
    {
        final Map<String, DynamicResourceHandler> specificDrhs = new HashMap<>();
        specificDrhs.put("allow",
                         new AbstractDynamicResourceHandler()
                         {
                             @Override
                             public CompletionStage<Boolean> checkPermission(final String permissionValue,
                                                                             final Optional<String> meta,
                                                                             final DeadboltHandler deadboltHandler,
                                                                             final Http.Context ctx)
                             {
                                 return CompletableFuture.completedFuture(true);
                             }
                         });
        specificDrhs.put("deny",
                         new AbstractDynamicResourceHandler()
                         {
                             @Override
                             public CompletionStage<Boolean> checkPermission(final String permissionValue,
                                                                             final Optional<String> meta,
                                                                             final DeadboltHandler deadboltHandler,
                                                                             final Http.Context ctx)
                             {
                                 return CompletableFuture.completedFuture(false);
                             }
                         });


        final DynamicResourceHandler drh = new AbstractDynamicResourceHandler()
        {
            @Override
            public CompletionStage<Boolean> checkPermission(final String permissionValue,
                                                            final Optional<String> meta,
                                                            final DeadboltHandler deadboltHandler,
                                                            final Http.Context ctx)
            {
                return specificDrhs.get(permissionValue).checkPermission(permissionValue,
                                                                         meta,
                                                                         deadboltHandler,
                                                                         ctx);
            }
        };

        final DeadboltHandler handler = Mockito.mock(DeadboltHandler.class);
        Mockito.when(handler.getDynamicResourceHandler(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.of(drh)));

        final DeadboltHandler noDrhHandler = Mockito.mock(DeadboltHandler.class);
        Mockito.when(noDrhHandler.getDynamicResourceHandler(Mockito.any(Http.Context.class)))
               .thenReturn(CompletableFuture.completedFuture(Optional.empty()));

        Map<String, DeadboltHandler> handlers = new HashMap<>();
        handlers.put("noDrh", noDrhHandler);
        handlerCache = new TestHandlerCache(handler,
                                            handlers);
    }

    @Test
    public void testCheckCustomPattern_noDynamicResourceHandler() throws Exception
    {
        try
        {
            viewSupport().viewPattern("foo",
                                      PatternType.CUSTOM,
                                      Optional.empty(),
                                      false,
                                      handlerCache.apply("noDrh"),
                                      Optional.empty(),
                                      1000L);
        }
        catch (Exception e)
        {
            Assert.assertEquals("java.lang.RuntimeException: A custom permission type is specified for value [foo] but no dynamic resource handler is provided",
                                e.getMessage());
        }
    }

    @Test
    public void testCheckCustomPattern_patternDoesNotPass() throws Exception
    {
        final boolean result = viewSupport().viewPattern("deny",
                                                         PatternType.CUSTOM,
                                                         Optional.empty(),
                                                         false,
                                                         handlerCache.get(),
                                                         Optional.empty(),
                                                         1000L);
        Assert.assertFalse(result);
    }

    @Test
    public void testCheckCustomPattern_patternPasses() throws Exception
    {
        final boolean result = viewSupport().viewPattern("allow",
                                                         PatternType.CUSTOM,
                                                         Optional.empty(),
                                                         false,
                                                         handlerCache.get(),
                                                         Optional.empty(),
                                                         1000L);
        Assert.assertTrue(result);
    }

    private ViewSupport viewSupport()
    {
        final ExecutionContextProvider ecProvider = Mockito.mock(ExecutionContextProvider.class);
        Mockito.when(ecProvider.get()).thenReturn(new DefaultDeadboltExecutionContextProvider());
        final ConstraintLogic constraintLogic = new ConstraintLogic(new DeadboltAnalyzer(),
                                                                    (deadboltHandler, context) -> CompletableFuture.completedFuture(Optional.empty()),
                                                                    Mockito.mock(PatternCache.class),
                                                                    ecProvider);

        return new ViewSupport(Mockito.mock(Configuration.class),
                               handlerCache,
                               new TemplateFailureListenerProvider(provideApplication().injector()),
                               constraintLogic);
    }

    @Override
    protected HandlerCache handlers()
    {
        return handlerCache;
    }
}
