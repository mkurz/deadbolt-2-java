package be.objectify.deadbolt.java.test.controllers;

import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;

import be.objectify.deadbolt.java.test.controllers.AbstractApplicationTest;
import com.jayway.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;

public class SubjectNotPresentMethodConstraintsTest extends AbstractApplicationTest
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
                               .get("/subject/not/present/m/subjectMustNotBePresent");
                });
    }

    @Test
    public void testSubjectNotMustBePresent_subjectIsPresent()
    {
        running(testServer(PORT,
                           app()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "greet")
                               .expect()
                               .statusCode(401)
                               .when()
                               .get("/subject/not/present/m/subjectMustNotBePresent");
                });
    }
}
