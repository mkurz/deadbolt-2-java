package be.objectify.deadbolt.java.test.controllers.composite;

import be.objectify.deadbolt.java.test.DataLoader;
import com.jayway.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;

public class CompositeMethodConstraintsTest
{

    private static final int PORT = 3333;

    @Before
    public void setUp()
    {
        RestAssured.port = PORT;
    }

    @Test
    public void protectedByControllerLeveComposite_noSubjectIsPresent()
    {
        running(testServer(PORT,
                           fakeApplication()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "greet")
                               .expect()
                               .statusCode(200)
                               .when()
                               .get("/composite/m/foo");
                });
    }

    @Test
    public void protectedByControllerLeveComposite_subjectHasPermission()
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
                               .get("/composite/m/foo");
                });
    }

    @Test
    public void protectedByControllerLeveComposite_subjectDoesNotHavePermission()
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
                               .get("/composite/m/foo");
                });
    }
    @Test
    public void protectedByControllerLeveComposite_noSubjectIsPresent_controllerExplicitlyUnrestricted()
    {
        running(testServer(PORT,
                           fakeApplication()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "greet")
                               .expect()
                               .statusCode(200)
                               .when()
                               .get("/composite/m/fooInUnrestrictedController");
                });
    }

    @Test
    public void protectedByControllerLeveComposite_subjectHasPermission_controllerExplicitlyUnrestricted()
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
                               .get("/composite/m/fooInUnrestrictedController");
                });
    }

    @Test
    public void protectedByControllerLeveComposite_subjectDoesNotHavePermission_controllerExplicitlyUnrestricted()
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
                               .get("/composite/m/fooInUnrestrictedController");
                });
    }
}
