package be.objectify.deadbolt.java.test.controllers;

import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;

import be.objectify.deadbolt.java.test.controllers.AbstractApplicationTest;
import com.jayway.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;

public abstract class RestrictTest extends AbstractApplicationTest {

    private static final int PORT = 3333;

    @Before
    public void setUp()
    {
        RestAssured.port = PORT;
    }

    @Test
    public void testRestrictedToFooAndBar_noUserPresent()
    {
        running(testServer(PORT,
                           fakeApplication()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "greet")
                               .expect()
                               .statusCode(401)
                               .when()
                               .get(String.format("/restrict/%s/restrictedToFooAndBar",
                                                  pathComponent()));
                });
    }

    @Test
    public void testRestrictedToFooAndBar_bothRolesPresent()
    {
        running(testServer(PORT,
                           app()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "greet")
                               .expect()
                               .statusCode(200)
                               .when()
                               .get(String.format("/restrict/%s/restrictedToFooAndBar",
                                                  pathComponent()));
                });

    }

    @Test
    public void testRestrictedToFooAndBar_oneRolesPresent()
    {
        running(testServer(PORT,
                           app()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "steve")
                               .expect()
                               .statusCode(401)
                               .when()
                               .get(String.format("/restrict/%s/restrictedToFooAndBar",
                                                  pathComponent()));
                });

    }

    @Test
    public void testRestrictedToFooAndBar_noRolesPresent()
    {
        running(testServer(PORT,
                           app()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "lotte")
                               .expect()
                               .statusCode(401)
                               .when()
                               .get(String.format("/restrict/%s/restrictedToFooAndBar",
                                                  pathComponent()));
                });

    }

    @Test
    public void testRestrictedToFooOrBar_noUserPresent()
    {
        running(testServer(PORT,
                           fakeApplication()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "greet")
                               .expect()
                               .statusCode(401)
                               .when()
                               .get(String.format("/restrict/%s/restrictedToFooOrBar",
                                                  pathComponent()));
                });
    }

    @Test
    public void testRestrictedToFooOrBar_bothRolesPresent()
    {
        running(testServer(PORT,
                           app()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "greet")
                               .expect()
                               .statusCode(200)
                               .when()
                               .get(String.format("/restrict/%s/restrictedToFooOrBar",
                                                  pathComponent()));
                });

    }

    @Test
    public void testRestrictedToFooOrBar_oneRolesPresent()
    {
        running(testServer(PORT,
                           app()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "steve")
                               .expect()
                               .statusCode(200)
                               .when()
                               .get(String.format("/restrict/%s/restrictedToFooOrBar",
                                                  pathComponent()));
                });

    }

    @Test
    public void testRestrictedToFooOrBar_noRolesPresent()
    {
        running(testServer(PORT,
                           app()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "lotte")
                               .expect()
                               .statusCode(401)
                               .when()
                               .get(String.format("/restrict/%s/restrictedToFooOrBar",
                                                  pathComponent()));
                });

    }

    @Test
    public void testRestrictedToFooAndNotBar_noUserPresent()
    {
        running(testServer(PORT,
                           fakeApplication()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "greet")
                               .expect()
                               .statusCode(401)
                               .when()
                               .get(String.format("/restrict/%s/restrictedToFooAndNotBar",
                                                  pathComponent()));
                });
    }

    @Test
    public void testRestrictedToFooAndNotBar_bothRolesPresent()
    {
        running(testServer(PORT,
                           app()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "greet")
                               .expect()
                               .statusCode(401)
                               .when()
                               .get(String.format("/restrict/%s/restrictedToFooAndNotBar",
                                                  pathComponent()));
                });

    }

    @Test
    public void testRestrictedToFooAndNotBar_onlyBarPresent()
    {
        running(testServer(PORT,
                           app()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "steve")
                               .expect()
                               .statusCode(401)
                               .when()
                               .get(String.format("/restrict/%s/restrictedToFooAndNotBar",
                                                  pathComponent()));
                });

    }

    @Test
    public void testRestrictedToFooAndNotBar_noRolesPresent()
    {
        running(testServer(PORT,
                           app()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "lotte")
                               .expect()
                               .statusCode(401)
                               .when()
                               .get(String.format("/restrict/%s/restrictedToFooAndNotBar",
                                                  pathComponent()));
                });

    }

    @Test
    public void testRestrictedToFooOrNotBar_noUserPresent()
    {
        running(testServer(PORT,
                           fakeApplication()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "greet")
                               .expect()
                               .statusCode(401)
                               .when()
                               .get(String.format("/restrict/%s/restrictedToFooOrNotBar",
                                                  pathComponent()));
                });
    }

    @Test
    public void testRestrictedToFooOrNotBar_bothRolesPresent()
    {
        running(testServer(PORT,
                           app()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "greet")
                               .expect()
                               .statusCode(200)
                               .when()
                               .get(String.format("/restrict/%s/restrictedToFooOrNotBar",
                                                  pathComponent()));
                });

    }

    @Test
    public void testRestrictedToFooOrNotBar_onlyBarPresent()
    {
        running(testServer(PORT,
                           app()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "steve")
                               .expect()
                               .statusCode(401)
                               .when()
                               .get(String.format("/restrict/%s/restrictedToFooOrNotBar",
                                                  pathComponent()));
                });

    }

    @Test
    public void testRestrictedToFooOrNotBar_onlyHurdyPresent()
    {
        running(testServer(PORT,
                           app()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "lotte")
                               .expect()
                               .statusCode(200)
                               .when()
                               .get(String.format("/restrict/%s/restrictedToFooOrNotBar",
                                                  pathComponent()));
                });

    }
}