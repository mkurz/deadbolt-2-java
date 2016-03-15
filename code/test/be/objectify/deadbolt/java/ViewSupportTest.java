package be.objectify.deadbolt.java;

import be.objectify.deadbolt.java.cache.HandlerCache;
import be.objectify.deadbolt.java.cache.PatternCache;
import be.objectify.deadbolt.java.models.PatternType;
import be.objectify.deadbolt.java.models.Subject;
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
import java.util.function.Supplier;

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
                             private final String foo = "true";
                             @Override
                             public CompletionStage<Boolean> checkPermission(final String permissionValue,
                                                                             final DeadboltHandler deadboltHandler,
                                                                             final Http.Context ctx)
                             {
                                 return CompletableFuture.completedFuture(true);
                             }
                         });
        specificDrhs.put("deny",
                         new AbstractDynamicResourceHandler()
                         {
                             private final String bar = "false";
                             @Override
                             public CompletionStage<Boolean> checkPermission(final String permissionValue,
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
                                                            final DeadboltHandler deadboltHandler,
                                                            final Http.Context ctx)
            {
                return specificDrhs.get(permissionValue).checkPermission(permissionValue,
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
        handlerCache = new DefaultHandlerCache(handler,
                                               handlers);
    }

    @Test
    public void testCheckCustomPattern_noDynamicResourceHandler() throws Exception
    {
        try
        {
            viewSupport().viewPattern("foo",
                                      PatternType.CUSTOM,
                                      handlerCache.apply("noDrh"),
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
                                                         handlerCache.get(),
                                                         1000L);
        Assert.assertFalse(result);
    }

    @Test
    public void testCheckCustomPattern_patternPasses() throws Exception
    {
        final boolean result = viewSupport().viewPattern("allow",
                                                         PatternType.CUSTOM,
                                                         handlerCache.get(),
                                                         1000L);
        Assert.assertTrue(result);
    }

    private ViewSupport viewSupport()
    {
        final ExecutionContextProvider ecProvider = Mockito.mock(ExecutionContextProvider.class);
        Mockito.when(ecProvider.get()).thenReturn(new DefaultDeadboltExecutionContextProvider());

        return new ViewSupport(Mockito.mock(Configuration.class),
                               new DeadboltAnalyzer(),
                               (handler, context) -> CompletableFuture.completedFuture(Optional.<Subject>empty()),
                               handlerCache,
                               Mockito.mock(PatternCache.class),
                               new TemplateFailureListenerProvider(),
                               ecProvider);
    }

    @Override
    protected HandlerCache handlers()
    {
        return handlerCache;
    }
}
