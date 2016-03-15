package be.objectify.deadbolt.java;

import be.objectify.deadbolt.java.models.Subject;
import be.objectify.deadbolt.java.cache.HandlerCache;
import be.objectify.deadbolt.java.testsupport.FakeCache;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Application;
import play.Mode;
import play.api.mvc.RequestHeader;
import play.cache.CacheApi;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.test.Helpers;
import play.test.WithApplication;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

import static play.inject.Bindings.bind;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public abstract class AbstractFakeApplicationTest extends WithApplication
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractFakeApplicationTest.class);

    @Override
    protected Application provideApplication()
    {
        return new GuiceApplicationBuilder().bindings(new DeadboltModule())
                                            .bindings(bind(HandlerCache.class).toInstance(handlers()))
                                            .overrides(bind(CacheApi.class).to(FakeCache.class))
                                            .in(Mode.TEST)
                                            .build();
    }

    @Override
    public void startPlay()
    {
        super.startPlay();
        Http.Context.current.set(new Http.Context(1L,
                                                  Mockito.mock(RequestHeader.class),
                                                  Mockito.mock(Http.Request.class),
                                                  Collections.<String, String>emptyMap(),
                                                  Collections.<String, String>emptyMap(),
                                                  Collections.<String, Object>emptyMap()));
    }

    public DeadboltHandler init(final Supplier<Subject> getSubject)
    {
        final DeadboltHandler handler = handler(getSubject);

        Helpers.start(this.app);

        return handler;
    }

    protected abstract HandlerCache handlers();

    protected ExecutionContextProvider ecProvider()
    {
        final ExecutionContextProvider ecProvider = Mockito.mock(ExecutionContextProvider.class);
        Mockito.when(ecProvider.get())
               .thenReturn(new DefaultDeadboltExecutionContextProvider());
        return ecProvider;
    }

    protected DeadboltHandler handler(final Supplier<Subject> getSubject)
    {
        return new AbstractNoPreAuthDeadboltHandler(ecProvider())
        {
            @Override
            public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context)
            {
                return CompletableFuture.supplyAsync(() -> Optional.ofNullable(getSubject.get()));
            }
        };
    }

    public Http.Context context()
    {
        return Http.Context.current.get();
    }

    protected class DefaultHandlerCache implements HandlerCache
    {
        private final DeadboltHandler defaultHandler;
        private final Map<String, DeadboltHandler> handlers = new HashMap<>();

        public DefaultHandlerCache(final DeadboltHandler defaultHandler,
                                   final Map<String, DeadboltHandler> handlers)
        {
            this.defaultHandler = defaultHandler;
            this.handlers.putAll(handlers);
        }

        /**
         * Get the handler mapped to the given key.
         *
         * @param handlerKey the unique key of the handler
         * @return an option of the handler
         */
        @Override
        public DeadboltHandler apply(final String handlerKey)
        {
            final DeadboltHandler handler;
            if (handlers.containsKey(handlerKey))
            {
                handler = handlers.get(handlerKey);
                LOGGER.debug("Retrieved handler [{}] for key [{}]",
                             handler,
                             handlerKey);
            }
            else
            {
                LOGGER.error("Handler key [{}] is not defined.  You need to look at this urgently.");
                // don't do this in real life! Returning null is for forcing tests to fail if the key is wrong
                handler = null;
            }
            return handler;
        }

        /**
         * Get the default DeadboltHandler.
         */
        @Override
        public DeadboltHandler get()
        {
            return defaultHandler;
        }
    }
}
