package be.objectify.deadbolt.java.test.controllers.pattern;

import be.objectify.deadbolt.java.test.DataLoader;
import com.google.common.collect.ImmutableMap;
import com.jayway.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;

import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;

public class CustomControllerConstraintsTest
{

    private static final int PORT = 3333;

    @Before
    public void setUp()
    {
        RestAssured.port = PORT;
    }

    @Test
    public void testProtectedByControllerLevelCustom_noSubjectIsPresent()
    {
        running(testServer(PORT,
                           fakeApplication(new ImmutableMap.Builder<String, String>().put("deadbolt.java.handlers.defaultHandler",
                                                                                          "be.objectify.deadbolt.java.test.security.TestDeadboltHandler")
                                                                                     .build())),
                () -> {
                    RestAssured.given()
                               .cookie("user", "greet")
                               .expect()
                               .statusCode(401)
                               .when()
                               .get("/pattern/custom/c/checkCustom");
                });
    }

    @Test
    public void testProtectedByControllerLevelCustom_subjectDoesNotHavePermission()
    {
        running(testServer(PORT,
                           fakeApplication(new ImmutableMap.Builder<String, String>().put("deadbolt.java.handlers.defaultHandler",
                                                                                          "be.objectify.deadbolt.java.test.security.TestDeadboltHandler")
                                                                                     .build(),
                                           new DataLoader("/be/objectify/deadbolt/java/test/standard.xml"))),
                () -> {
                    RestAssured.given()
                               .cookie("user", "lotte")
                               .expect()
                               .statusCode(401)
                               .when()
                               .get("/pattern/custom/c/checkCustom");
                });
    }

    @Test
    public void testProtectedByControllerLevelCustom_zombieKiller()
    {
        running(testServer(PORT,
                           fakeApplication(new ImmutableMap.Builder<String, String>().put("deadbolt.java.handlers.defaultHandler",
                                                                                          "be.objectify.deadbolt.java.test.security.TestDeadboltHandler")
                                                                                     .build(),
                                           new DataLoader("/be/objectify/deadbolt/java/test/standard.xml"))),
                () -> {
                    RestAssured.given()
                               .cookie("user", "greet")
                               .expect()
                               .statusCode(200)
                               .when()
                               .get("/pattern/custom/c/checkCustom");
                });
    }

    @Test
    public void testProtectedByControllerLevelCustom_somethingElseZombieRelated()
    {
        running(testServer(PORT,
                           fakeApplication(new ImmutableMap.Builder<String, String>().put("deadbolt.java.handlers.defaultHandler",
                                                                                          "be.objectify.deadbolt.java.test.security.TestDeadboltHandler")
                                                                                     .build(),
                                           new DataLoader("/be/objectify/deadbolt/java/test/standard.xml"))),
                () -> {
                    RestAssured.given()
                               .cookie("user", "mani")
                               .expect()
                               .statusCode(200)
                               .when()
                               .get("/pattern/custom/c/checkCustom");
                });
    }

    @Test
    public void testUnrestricted_noSubjectIsPresent()
    {
        running(testServer(PORT,
                           fakeApplication(new ImmutableMap.Builder<String, String>().put("deadbolt.java.handlers.defaultHandler",
                                                                                          "be.objectify.deadbolt.java.test.security.TestDeadboltHandler")
                                                                                     .build())),
                () -> {
                    RestAssured.given()
                               .cookie("user", "greet")
                               .expect()
                               .statusCode(200)
                               .when()
                               .get("/pattern/custom/c/checkCustom/open");
                });
    }

    @Test
    public void testUnrestricted_subjectHasPermission()
    {
        running(testServer(PORT,
                           fakeApplication(new ImmutableMap.Builder<String, String>().put("deadbolt.java.handlers.defaultHandler",
                                                                                          "be.objectify.deadbolt.java.test.security.TestDeadboltHandler")
                                                                                     .build(),
                                           new DataLoader("/be/objectify/deadbolt/java/test/standard.xml"))),
                () -> {
                    RestAssured.given()
                               .cookie("user", "steve")
                               .expect()
                               .statusCode(200)
                               .when()
                               .get("/pattern/custom/c/checkCustom/open");
                });
    }


    @Test
    public void testUnrestricted_subjectDoesNotHavePermission()
    {
        running(testServer(PORT,
                           fakeApplication(new ImmutableMap.Builder<String, String>().put("deadbolt.java.handlers.defaultHandler",
                                                                                          "be.objectify.deadbolt.java.test.security.TestDeadboltHandler")
                                                                                     .build(),
                                           new DataLoader("/be/objectify/deadbolt/java/test/standard.xml"))),
                () -> {
                    RestAssured.given()
                               .cookie("user", "greet")
                               .expect()
                               .statusCode(200)
                               .when()
                               .get("/pattern/custom/c/checkCustom/open");
                });
    }
}
