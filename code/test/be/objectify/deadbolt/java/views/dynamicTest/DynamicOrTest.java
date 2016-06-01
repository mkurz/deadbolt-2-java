package be.objectify.deadbolt.java.views.dynamicTest;

import be.objectify.deadbolt.java.AbstractDynamicResourceHandler;
import be.objectify.deadbolt.java.AbstractFakeApplicationTest;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import be.objectify.deadbolt.java.NoPreAuthDeadboltHandler;
import be.objectify.deadbolt.java.cache.HandlerCache;
import be.objectify.deadbolt.java.testsupport.TestHandlerCache;
import org.junit.Assert;
import org.junit.Test;
import play.mvc.Http;
import play.test.Helpers;
import play.twirl.api.Content;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class DynamicOrTest extends AbstractFakeApplicationTest
{
    @Test
    public void testValid()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<DynamicResourceHandler>> getDynamicResourceHandler(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new TestDynamicResourceHandler((name, meta) -> true)));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.dynamicTest.dynamicOrContent.render("foo",
                                                                                                       Optional.of("bar"),
                                                                                                       deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertTrue(content.contains("This is protected by the constraint."));
        Assert.assertFalse(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testName()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<DynamicResourceHandler>> getDynamicResourceHandler(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new TestDynamicResourceHandler((name, meta) -> "foo".equals(name))));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.dynamicTest.dynamicOrContent.render("foo",
                                                                                                       Optional.of("bar"),
                                                                                                       deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertTrue(content.contains("This is protected by the constraint."));
        Assert.assertFalse(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testMeta()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<DynamicResourceHandler>> getDynamicResourceHandler(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new TestDynamicResourceHandler((name, meta) -> meta.map("bar"::equals).orElse(false))));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.dynamicTest.dynamicOrContent.render("foo",
                                                                                                       Optional.of("bar"),
                                                                                                       deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertTrue(content.contains("This is protected by the constraint."));
        Assert.assertFalse(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    @Test
    public void testInvalid()
    {
        final DeadboltHandler deadboltHandler = new NoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<DynamicResourceHandler>> getDynamicResourceHandler(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(() -> Optional.of(new TestDynamicResourceHandler((name, meta) -> false)));
            }
        };
        final Content html = be.objectify.deadbolt.java.views.html.dynamicTest.dynamicOrContent.render("foo",
                                                                                                       Optional.of("bar"),
                                                                                                       deadboltHandler);
        final String content = Helpers.contentAsString(html);
        Assert.assertTrue(content.contains("This is before the constraint."));
        Assert.assertFalse(content.contains("This is protected by the constraint."));
        Assert.assertTrue(content.contains("This is default content in case the constraint denies access to the protected content."));
        Assert.assertTrue(content.contains("This is after the constraint."));
    }

    public HandlerCache handlers()
    {
        // using new instances of handlers in the test
        return new TestHandlerCache(null,
                                    new HashMap<>());
    }

    private class TestDynamicResourceHandler extends AbstractDynamicResourceHandler
    {
        private final BiFunction<String, Optional<String>, Boolean> test;

        private TestDynamicResourceHandler(final BiFunction<String, Optional<String>, Boolean> test)
        {
            this.test = test;
        }

        @Override
        public CompletionStage<Boolean> isAllowed(final String name,
                                                  final Optional<String> meta,
                                                  final DeadboltHandler deadboltHandler,
                                                  final Http.Context ctx)
        {
            return CompletableFuture.completedFuture(test.apply(name,
                                                                meta));
        }
    }
}
