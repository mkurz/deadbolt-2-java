package be.objectify.deadbolt.java.test.controllers.subject;

import be.objectify.deadbolt.java.test.DataLoader;
import com.jayway.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;

public class SubjectNotPresentControllerConstraintsTest
{

    private static final int PORT = 3333;

    @Before
    public void setUp()
    {
        RestAssured.port = PORT;
    }

    @Test
    public void testSubjectMustNotBePresent_noSubjectIsPresent()
    {
        running(testServer(PORT,
                           fakeApplication()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "greet")
                               .expect()
                               .statusCode(200)
                               .when()
                               .get("/subject/not/present/c/subjectMustNotBePresent");
                });
    }

    @Test
    public void testSubjectMustNotBePresent_unrestricted_noSubjectIsPresent()
    {
        running(testServer(PORT,
                           fakeApplication()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "greet")
                               .expect()
                               .statusCode(200)
                               .when()
                               .get("/subject/not/present/c/subjectMustNotBePresent/open");
                });
    }

    @Test
    public void testSubjectMustNotBePresent_subjectIsPresent()
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
                               .get("/subject/not/present/c/subjectMustNotBePresent");
                });
    }

    @Test
    public void testSubjectMustBePresent_unrestricted_subjectIsPresent()
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
                            .get("/subject/not/present/c/subjectMustNotBePresent/open");
                });
    }
}
