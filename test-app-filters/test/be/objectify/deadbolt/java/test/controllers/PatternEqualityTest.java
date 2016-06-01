package be.objectify.deadbolt.java.test.controllers;

import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;

import java.util.Collections;
import be.objectify.deadbolt.java.test.DataLoader;
import be.objectify.deadbolt.java.test.controllers.AbstractApplicationTest;
import com.jayway.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;

public abstract class PatternEqualityTest extends AbstractApplicationTest
{

    private static final int PORT = 3333;

    @Before
    public void setUp()
    {
        RestAssured.port = PORT;
    }

    @Test
    public void testProtectedByMethodLevelEquality_noSubjectIsPresent()
    {
        running(testServer(PORT,
                           fakeApplication()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "greet")
                               .expect()
                               .statusCode(401)
                               .when()
                               .get(String.format("/pattern/equality/%s/checkEquality",
                                                  pathComponent()));
                });
    }

    @Test
    public void testProtectedByMethodLevelEquality_subjectDoesNotHavePermission()
    {
        running(testServer(PORT,
                           app()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "lotte")
                               .expect()
                               .statusCode(401)
                               .when()
                               .get(String.format("/pattern/equality/%s/checkEquality",
                                                  pathComponent()));
                });
    }

    @Test
    public void testProtectedByMethodLevelEquality_subjectHasPermission()
    {
        running(testServer(PORT,
                           app()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "greet")
                               .expect()
                               .statusCode(200)
                               .when()
                               .get(String.format("/pattern/equality/%s/checkEquality",
                                                  pathComponent()));
                });
    }

    @Test
    public void testProtectedByMethodLevelEquality_noSubjectIsPresent_inverted()
    {
        running(testServer(PORT,
                           fakeApplication()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "greet")
                               .expect()
                               .statusCode(401)
                               .when()
                               .get(String.format("/pattern/invert/equality/%s/checkEquality",
                                                  pathComponent()));
                });
    }

    @Test
    public void testProtectedByMethodLevelEquality_subjectDoesNotHavePermission_inverted()
    {
        running(testServer(PORT,
                           app()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "lotte")
                               .expect()
                               .statusCode(200)
                               .when()
                               .get(String.format("/pattern/invert/equality/%s/checkEquality",
                                                  pathComponent()));
                });
    }


    @Test
    public void testProtectedByMethodLevelEquality_subjectHasPermission_inverted()
    {
        running(testServer(PORT,
                           app()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "greet")
                               .expect()
                               .statusCode(401)
                               .when()
                               .get(String.format("/pattern/invert/equality/%s/checkEquality",
                                                  pathComponent()));
                });
    }
}
