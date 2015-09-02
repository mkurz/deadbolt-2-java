package be.objectify.deadbolt.java.test.controllers.pattern;

import be.objectify.deadbolt.java.test.DataLoader;
import com.jayway.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;

public class InvertedRegexControllerConstraintsTest
{

    private static final int PORT = 3333;

    @Before
    public void setUp()
    {
        RestAssured.port = PORT;
    }

    @Test
    public void testProtectedByControllerLevelRegex_noSubjectIsPresent()
    {
        running(testServer(PORT,
                           fakeApplication()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "greet")
                               .expect()
                               .statusCode(200)
                               .when()
                               .get("/pattern/invert/regex/c/checkMatch");
                });
    }

    @Test
    public void testProtectedByControllerLevelRegex_subjectDoesNotHavePermission()
    {
        running(testServer(PORT,
                           fakeApplication(Collections.emptyMap(),
                                           new DataLoader("/be/objectify/deadbolt/java/test/standard.xml"))),
                () -> {
                    RestAssured.given()
                               .cookie("user", "steve")
                               .expect()
                               .statusCode(200)
                               .when()
                               .get("/pattern/invert/regex/c/checkMatch");
                });
    }

    @Test
    public void testProtectedByControllerLevelRegex_zombieKiller()
    {
        running(testServer(PORT,
                           fakeApplication(Collections.emptyMap(),
                                           new DataLoader("/be/objectify/deadbolt/java/test/standard.xml"))),
                () -> {
                    RestAssured.given()
                               .cookie("user", "greet")
                               .expect()
                               .statusCode(401)
                               .when()
                               .get("/pattern/invert/regex/c/checkMatch");
                });
    }

    @Test
    public void testProtectedByControllerLevelRegex_vampireKiller()
    {
        running(testServer(PORT,
                           fakeApplication(Collections.emptyMap(),
                                           new DataLoader("/be/objectify/deadbolt/java/test/standard.xml"))),
                () -> {
                    RestAssured.given()
                               .cookie("user", "lotte")
                               .expect()
                               .statusCode(401)
                               .when()
                               .get("/pattern/invert/regex/c/checkMatch");
                });
    }

    @Test
    public void testUnrestricted_noSubjectIsPresent()
    {
        running(testServer(PORT,
                           fakeApplication()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "greet")
                               .expect()
                               .statusCode(200)
                               .when()
                               .get("/pattern/invert/regex/c/checkMatch/open");
                });
    }

    @Test
    public void testUnrestricted_subjectHasPermission()
    {
        running(testServer(PORT,
                           fakeApplication(Collections.emptyMap(),
                                           new DataLoader("/be/objectify/deadbolt/java/test/standard.xml"))),
                () -> {
                    RestAssured.given()
                               .cookie("user", "steve")
                               .expect()
                               .statusCode(200)
                               .when()
                               .get("/pattern/invert/regex/c/checkMatch/open");
                });
    }


    @Test
    public void testUnrestricted_subjectDoesNotHavePermission()
    {
        running(testServer(PORT,
                           fakeApplication(Collections.emptyMap(),
                                           new DataLoader("/be/objectify/deadbolt/java/test/standard.xml"))),
                () -> {
                    RestAssured.given()
                               .cookie("user", "greet")
                               .expect()
                               .statusCode(200)
                               .when()
                               .get("/pattern/invert/regex/c/checkMatch/open");
                });
    }
}
