package be.objectify.deadbolt.java;

import org.junit.Before;
import org.mockito.Mockito;
import play.api.mvc.RequestHeader;
import play.mvc.Http;
import play.test.FakeApplication;
import play.test.Helpers;
import play.test.WithApplication;

import java.util.Collections;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class AbstractFakeApplicationTest extends WithApplication
{
    protected FakeApplication provideFakeApplication()
    {
        return Helpers.fakeApplication(Collections.<String, Object>emptyMap(),
                                       Collections.singletonList("be.objectify.deadbolt.java.DeadboltPlugin"));
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
