package be.objectify.deadbolt.java.test.controllers;

import com.jayway.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;

import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;

public abstract class SubjectNotPresentTest extends AbstractApplicationTest
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
                               .get(String.format("/subject/not/present/%s/subjectMustNotBePresent",
                                                  pathComponent()));
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
                               .get(String.format("/subject/not/present/%s/subjectMustNotBePresent",
                                                  pathComponent()));
                });
    }
}
