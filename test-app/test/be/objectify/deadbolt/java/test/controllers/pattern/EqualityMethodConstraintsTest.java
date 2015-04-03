package be.objectify.deadbolt.java.test.controllers.pattern;

import be.objectify.deadbolt.java.test.DataLoader;
import com.google.common.collect.ImmutableMap;
import com.jayway.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;

import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;

public class EqualityMethodConstraintsTest
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
                           fakeApplication(new ImmutableMap.Builder<String, String>().put("deadbolt.java.handlers.defaultHandler",
                                                                                          "be.objectify.deadbolt.java.test.security.TestDeadboltHandler")
                                                                                     .build())),
                new Runnable()
                {
                    @Override
                    public void run()
                    {
                        RestAssured.given()
                                   .cookie("user", "greet")
                                   .expect()
                                   .statusCode(401)
                                   .when()
                                   .get("/pattern/equality/m/checkEquality");
                    }
                });
    }

    @Test
    public void testProtectedByMethodLevelEquality_subjectDoesNotHavePermission()
    {
        running(testServer(PORT,
                           fakeApplication(new ImmutableMap.Builder<String, String>().put("deadbolt.java.handlers.defaultHandler",
                                                                                          "be.objectify.deadbolt.java.test.security.TestDeadboltHandler")
                                                                                     .build(),
                                           new DataLoader("/be/objectify/deadbolt/java/test/standard.xml"))),
                new Runnable()
                {
                    @Override
                    public void run()
                    {
                        RestAssured.given()
                                   .cookie("user", "lotte")
                                   .expect()
                                   .statusCode(401)
                                   .when()
                                   .get("/pattern/equality/m/checkEquality");
                    }
                });
    }


    @Test
    public void testProtectedByMethodLevelEquality_subjectHasPermission()
    {
        running(testServer(PORT,
                           fakeApplication(new ImmutableMap.Builder<String, String>().put("deadbolt.java.handlers.defaultHandler",
                                                                                          "be.objectify.deadbolt.java.test.security.TestDeadboltHandler")
                                                                                     .build(),
                                           new DataLoader("/be/objectify/deadbolt/java/test/standard.xml"))),
                new Runnable()
                {
                    @Override
                    public void run()
                    {
                        RestAssured.given()
                                   .cookie("user", "greet")
                                   .expect()
                                   .statusCode(200)
                                   .when()
                                   .get("/pattern/equality/m/checkEquality");
                    }
                });
    }
}
