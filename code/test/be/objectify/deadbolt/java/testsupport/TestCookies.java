package be.objectify.deadbolt.java.testsupport;

import play.mvc.Http;

import java.util.ArrayList;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class TestCookies extends ArrayList<Http.Cookie> implements Http.Cookies {
    @Override
    public Http.Cookie get(final String name) {
        return null;
    }
}