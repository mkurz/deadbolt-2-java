package be.objectify.deadbolt.java.test.controllers;

import com.jayway.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;

import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;

public abstract class PatternRegexTest extends AbstractApplicationTest
{

    private static final int PORT = 3333;

    @Before
    public void setUp()
    {
        RestAssured.port = PORT;
    }

    @Test
    public void testProtectedByMethodLevelRegex_noSubjectIsPresent()
    {
        running(testServer(PORT,
                           fakeApplication()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "greet")
                               .expect()
                               .statusCode(401)
                               .when()
                               .get(String.format("/pattern/regex/%s/checkExactMatch",
                                                  pathComponent()));
                });
    }

    @Test
    public void testProtectedByMethodLevelRegex_subjectDoesNotHavePermission()
    {
        running(testServer(PORT,
                           app()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "steve")
                               .expect()
                               .statusCode(401)
                               .when()
                               .get(String.format("/pattern/regex/%s/checkExactMatch",
                                                  pathComponent()));
                });
    }

    @Test
    public void testProtectedByMethodLevelRegex_subjectHasPermission()
    {
        running(testServer(PORT,
                           app()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "greet")
                               .expect()
                               .statusCode(200)
                               .when()
                               .get(String.format("/pattern/regex/%s/checkExactMatch",
                                                  pathComponent()));
                });
    }

    @Test
    public void testProtectedByMethodLevelRegex_hierarchicalMatch_noSubjectIsPresent()
    {
        running(testServer(PORT,
                           fakeApplication()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "greet")
                               .expect()
                               .statusCode(401)
                               .when()
                               .get(String.format("/pattern/regex/%s/checkHierarchicalMatch",
                                                  pathComponent()));
                });
    }

    @Test
    public void testProtectedByMethodLevelRegex_hierarchicalMatch_subjectDoesNotHavePermission()
    {
        running(testServer(PORT,
                           app()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "steve")
                               .expect()
                               .statusCode(401)
                               .when()
                               .get(String.format("/pattern/regex/%s/checkHierarchicalMatch",
                                                  pathComponent()));
                });
    }

    @Test
    public void testProtectedByMethodLevelRegex_hierarchicalMatch_subjectHasPermission()
    {
        running(testServer(PORT,
                           app()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "greet")
                               .expect()
                               .statusCode(200)
                               .when()
                               .get(String.format("/pattern/regex/%s/checkHierarchicalMatch",
                                                  pathComponent()));
                });
    }

    @Test
    public void testProtectedByMethodLevelRegex_hierarchicalMatch_aDifferentSubjectHasPermission()
    {
        running(testServer(PORT,
                           app()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "lotte")
                               .expect()
                               .statusCode(200)
                               .when()
                               .get(String.format("/pattern/regex/%s/checkHierarchicalMatch",
                                                  pathComponent()));
                });
    }

    @Test
    public void testProtectedByMethodLevelRegex_noSubjectIsPresent_inverted()
    {
        running(testServer(PORT,
                           fakeApplication()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "greet")
                               .expect()
                               .statusCode(401)
                               .when()
                               .get(String.format("/pattern/invert/regex/%s/checkExactMatch",
                                                  pathComponent()));
                });
    }

    @Test
    public void testProtectedByMethodLevelRegex_subjectDoesNotHavePermission_inverted()
    {
        running(testServer(PORT,
                           app()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "steve")
                               .expect()
                               .statusCode(200)
                               .when()
                               .get(String.format("/pattern/invert/regex/%s/checkExactMatch",
                                                  pathComponent()));
                });
    }

    @Test
    public void testProtectedByMethodLevelRegex_subjectHasPermission_inverted()
    {
        running(testServer(PORT,
                           app()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "greet")
                               .expect()
                               .statusCode(401)
                               .when()
                               .get(String.format("/pattern/invert/regex/%s/checkExactMatch",
                                                  pathComponent()));
                });
    }

    @Test
    public void testProtectedByMethodLevelRegex_hierarchicalMatch_noSubjectIsPresent_inverted()
    {
        running(testServer(PORT,
                           fakeApplication()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "greet")
                               .expect()
                               .statusCode(401)
                               .when()
                               .get(String.format("/pattern/invert/regex/%s/checkHierarchicalMatch",
                                                  pathComponent()));
                });
    }

    @Test
    public void testProtectedByMethodLevelRegex_hierarchicalMatch_subjectDoesNotHavePermission_inverted()
    {
        running(testServer(PORT,
                           app()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "steve")
                               .expect()
                               .statusCode(200)
                               .when()
                               .get(String.format("/pattern/invert/regex/%s/checkHierarchicalMatch",
                                                  pathComponent()));
                });
    }

    @Test
    public void testProtectedByMethodLevelRegex_hierarchicalMatch_subjectHasPermission_inverted()
    {
        running(testServer(PORT,
                           app()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "greet")
                               .expect()
                               .statusCode(401)
                               .when()
                               .get(String.format("/pattern/invert/regex/%s/checkHierarchicalMatch",
                                                  pathComponent()));
                });
    }

    @Test
    public void testProtectedByMethodLevelRegex_hierarchicalMatch_aDifferentSubjectHasPermission_inverted()
    {
        running(testServer(PORT,
                           app()),
                () -> {
                    RestAssured.given()
                               .cookie("user", "lotte")
                               .expect()
                               .statusCode(401)
                               .when()
                               .get(String.format("/pattern/invert/regex/%s/checkHierarchicalMatch",
                                                  pathComponent()));
                });
    }
}
