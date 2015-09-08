package be.objectify.deadbolt.java.test.controllers.pattern;

import be.objectify.deadbolt.java.test.DataLoader;
import com.jayway.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;

public class InvertedRegexMethodConstraintsTest
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
                               .get("/pattern/invert/regex/m/checkExactMatch");
                });
    }

    @Test
    public void testProtectedByMethodLevelRegex_subjectDoesNotHavePermission()
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
                               .get("/pattern/invert/regex/m/checkExactMatch");
                });
    }

    @Test
    public void testProtectedByMethodLevelRegex_subjectHasPermission()
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
                               .get("/pattern/invert/regex/m/checkExactMatch");
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
                               .get("/pattern/invert/regex/m/checkHierarchicalMatch");
                });
    }

    @Test
    public void testProtectedByMethodLevelRegex_hierarchicalMatch_subjectDoesNotHavePermission()
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
                               .get("/pattern/invert/regex/m/checkHierarchicalMatch");
                });
    }

    @Test
    public void testProtectedByMethodLevelRegex_hierarchicalMatch_subjectHasPermission()
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
                               .get("/pattern/invert/regex/m/checkHierarchicalMatch");
                });
    }

    @Test
    public void testProtectedByMethodLevelRegex_hierarchicalMatch_aDifferentSubjectHasPermission()
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
                               .get("/pattern/invert/regex/m/checkHierarchicalMatch");
                });
    }
}
