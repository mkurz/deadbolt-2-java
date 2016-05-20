package be.objectify.deadbolt.java.testsupport;

import java.util.ArrayList;
import play.mvc.Http;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class TestCookies extends ArrayList<Http.Cookie> implements Http.Cookies {
    @Override
    public Http.Cookie get(final String name) {
        return null;
    }
}