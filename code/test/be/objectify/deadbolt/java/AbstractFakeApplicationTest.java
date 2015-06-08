package be.objectify.deadbolt.java;

import be.objectify.deadbolt.java.testsupport.FakeCache;
import org.junit.Before;
import org.mockito.Mockito;
import play.Application;
import play.Mode;
import play.api.inject.Binding;
import play.cache.CacheApi;
import play.inject.Bindings;
import play.inject.guice.GuiceApplicationBuilder;
import play.api.mvc.RequestHeader;
import play.mvc.Http;
import play.test.WithApplication;

import java.util.Collections;

import static play.inject.Bindings.bind;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class AbstractFakeApplicationTest extends WithApplication
{
    @Override
    protected Application provideApplication()
    {
        return new GuiceApplicationBuilder().bindings(new DeadboltModule())
                                            .overrides(bind(CacheApi.class).to(FakeCache.class))
                                            .in(Mode.TEST)
                                            .build();
    }

    @Before
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

    public Http.Context context()
    {
        return Http.Context.current.get();
    }
}
