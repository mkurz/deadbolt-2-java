package be.objectify.deadbolt.java.test.controllers;

import be.objectify.deadbolt.java.test.DataLoader;
import com.google.common.collect.ImmutableMap;
import com.jayway.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;

import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;

public class RestrictMethodConstraintsTest {

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
                                   .get("/restrictMethods/restrictedToFooAndBar");
                    }
                });
    }

    @Test
    public void testRestrictedToFooAndBar_bothRolesPresent()
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
                                   .get("/restrictMethods/restrictedToFooAndBar");
                    }
                });

    }

    @Test
    public void testRestrictedToFooAndBar_oneRolesPresent()
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
                                   .cookie("user", "steve")
                                   .expect()
                                   .statusCode(401)
                                   .when()
                                   .get("/restrictMethods/restrictedToFooAndBar");
                    }
                });

    }

    @Test
    public void testRestrictedToFooAndBar_noRolesPresent()
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
                                   .get("/restrictMethods/restrictedToFooAndBar");
                    }
                });

    }

    @Test
    public void testRestrictedToFooOrBar_noUserPresent()
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
                                   .get("/restrictMethods/restrictedToFooOrBar");
                    }
                });
    }

    @Test
    public void testRestrictedToFooOrBar_bothRolesPresent()
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
                                   .get("/restrictMethods/restrictedToFooOrBar");
                    }
                });

    }

    @Test
    public void testRestrictedToFooOrBar_oneRolesPresent()
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
                                   .cookie("user", "steve")
                                   .expect()
                                   .statusCode(200)
                                   .when()
                                   .get("/restrictMethods/restrictedToFooOrBar");
                    }
                });

    }

    @Test
    public void testRestrictedToFooOrBar_noRolesPresent()
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
                                   .get("/restrictMethods/restrictedToFooOrBar");
                    }
                });

    }

    @Test
    public void testRestrictedToFooAndNotBar_noUserPresent()
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
                                   .get("/restrictMethods/restrictedToFooAndNotBar");
                    }
                });
    }

    @Test
    public void testRestrictedToFooAndNotBar_bothRolesPresent()
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
                                   .statusCode(401)
                                   .when()
                                   .get("/restrictMethods/restrictedToFooAndNotBar");
                    }
                });

    }

    @Test
    public void testRestrictedToFooAndNotBar_onlyBarPresent()
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
                                   .cookie("user", "steve")
                                   .expect()
                                   .statusCode(401)
                                   .when()
                                   .get("/restrictMethods/restrictedToFooAndNotBar");
                    }
                });

    }

    @Test
    public void testRestrictedToFooAndNotBar_noRolesPresent()
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
                                   .get("/restrictMethods/restrictedToFooAndNotBar");
                    }
                });

    }

    @Test
    public void testRestrictedToFooOrNotBar_noUserPresent()
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
                                   .get("/restrictMethods/restrictedToFooOrNotBar");
                    }
                });
    }

    @Test
    public void testRestrictedToFooOrNotBar_bothRolesPresent()
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
                                   .get("/restrictMethods/restrictedToFooOrNotBar");
                    }
                });

    }

    @Test
    public void testRestrictedToFooOrNotBar_onlyBarPresent()
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
                                   .cookie("user", "steve")
                                   .expect()
                                   .statusCode(401)
                                   .when()
                                   .get("/restrictMethods/restrictedToFooOrNotBar");
                    }
                });

    }

    @Test
    public void testRestrictedToFooOrNotBar_onlyHurdyPresent()
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
                                   .statusCode(200)
                                   .when()
                                   .get("/restrictMethods/restrictedToFooOrNotBar");
                    }
                });

    }
}
