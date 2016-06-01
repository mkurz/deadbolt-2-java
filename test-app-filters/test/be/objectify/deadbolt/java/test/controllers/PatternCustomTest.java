package be.objectify.deadbolt.java.test.controllers;

import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;

import be.objectify.deadbolt.java.test.controllers.AbstractApplicationTest;
import com.jayway.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;

public abstract class PatternCustomTest extends AbstractApplicationTest
{

    private static final int PORT = 3333;

    @Before
    public void setUp()
    {
        RestAssured.port = PORT;
    }

    @Test
    public void testProtectedByMethodLevelCustom_noSubjectIsPresent()
    {
        running(testServer(PORT,
                           fakeApplication()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "greet")
                               .expect()
                               .statusCode(401)
                               .when()
                               .get(String.format("/pattern/custom/%s/checkCustom",
                                                  pathComponent()));
                });
    }

    @Test
    public void testProtectedByMethodLevelCustom_subjectDoesNotHavePermission()
    {
        running(testServer(PORT,
                           app()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "lotte")
                               .expect()
                               .statusCode(401)
                               .when()
                               .get(String.format("/pattern/custom/%s/checkCustom",
                                                  pathComponent()));
                });
    }

    @Test
    public void testProtectedByMethodLevelCustom_zombieKiller()
    {
        running(testServer(PORT,
                           app()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "greet")
                               .expect()
                               .statusCode(200)
                               .when()
                               .get(String.format("/pattern/custom/%s/checkCustom",
                                                  pathComponent()));
                });
    }

    @Test
    public void testProtectedByMethodLevelCustom_zombieMovieFan()
    {
        running(testServer(PORT,
                           app()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "mani")
                               .expect()
                               .statusCode(200)
                               .when()
                               .get(String.format("/pattern/custom/%s/checkCustom",
                                                  pathComponent()));
                });
    }

    @Test
    public void testProtectedByMethodLevelCustom_noSubjectIsPresent_inverted()
    {
        running(testServer(PORT,
                           fakeApplication()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "greet")
                               .expect()
                               .statusCode(401)
                               .when()
                               .get(String.format("/pattern/invert/custom/%s/checkCustom",
                                                  pathComponent()));
                });
    }

    @Test
    public void testProtectedByMethodLevelCustom_subjectDoesNotHavePermission_inverted()
    {
        running(testServer(PORT,
                           app()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "lotte")
                               .expect()
                               .statusCode(200)
                               .when()
                               .get(String.format("/pattern/invert/custom/%s/checkCustom",
                                                  pathComponent()));
                });
    }

    @Test
    public void testProtectedByMethodLevelCustom_zombieKiller_inverted()
    {
        running(testServer(PORT,
                           app()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "greet")
                               .expect()
                               .statusCode(401)
                               .when()
                               .get(String.format("/pattern/invert/custom/%s/checkCustom",
                                                  pathComponent()));
                });
    }

    @Test
    public void testProtectedByMethodLevelCustom_zombieMovieFan_inverted()
    {
        running(testServer(PORT,
                           app()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "mani")
                               .expect()
                               .statusCode(401)
                               .when()
                               .get(String.format("/pattern/invert/custom/%s/checkCustom",
                                                  pathComponent()));
                });
    }
}
