/*
 * Copyright 2010-2016 Steve Chaloner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.objectify.deadbolt.java.test.controllers.restrict;

import be.objectify.deadbolt.java.test.controllers.AbstractApplicationTest;
import com.jayway.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;

import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class RestrictControllerConstraintsTest extends AbstractApplicationTest
{

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
                           fakeApplication()),
                () -> RestAssured.given()
                           .cookie("user", "greet")
                           .expect()
                           .statusCode(401)
                           .when()
                           .get("/restrict/c/restrictedToFooAndBar"));
    }

    @Test
    public void testRestrictedToFooAndBar_unrestricted_noUserPresent()
    {
        running(testServer(PORT,
                           fakeApplication()),
                () -> RestAssured.given()
                           .cookie("user", "greet")
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/restrict/c/restrictedToFooAndBar/open"));
    }

    @Test
    public void testRestrictedToFooAndBar_bothRolesPresent()
    {
        running(testServer(PORT,
                           app()),
                () -> RestAssured.given()
                           .cookie("user", "greet")
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/restrict/c/restrictedToFooAndBar"));

    }

    @Test
    public void testRestrictedToFooAndBar_oneRolesPresent()
    {
        running(testServer(PORT,
                           app()),
                () -> RestAssured.given()
                           .cookie("user", "steve")
                           .expect()
                           .statusCode(401)
                           .when()
                           .get("/restrict/c/restrictedToFooAndBar"));

    }

    @Test
    public void testRestrictedToFooAndBar_noRolesPresent()
    {
        running(testServer(PORT,
                           app()),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(401)
                           .when()
                           .get("/restrict/c/restrictedToFooAndBar"));

    }

    @Test
    public void testRestrictedToFooOrBar_noUserPresent()
    {
        running(testServer(PORT,
                           fakeApplication()),
                () -> RestAssured.given()
                           .cookie("user", "greet")
                           .expect()
                           .statusCode(401)
                           .when()
                           .get("/restrict/c/restrictedToFooOrBar"));
    }

    @Test
    public void testRestrictedToFooAndBar_unrestricted_noRolesPresent()
    {
        running(testServer(PORT,
                           app()),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/restrict/c/restrictedToFooOrBar/open"));

    }

    @Test
    public void testRestrictedToFooOrBar_bothRolesPresent()
    {
        running(testServer(PORT,
                           app()),
                () -> RestAssured.given()
                           .cookie("user", "greet")
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/restrict/c/restrictedToFooOrBar"));

    }

    @Test
    public void testRestrictedToFooOrBar_oneRolesPresent()
    {
        running(testServer(PORT,
                           app()),
                () -> RestAssured.given()
                           .cookie("user", "steve")
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/restrict/c/restrictedToFooOrBar"));

    }

    @Test
    public void testRestrictedToFooOrBar_noRolesPresent()
    {
        running(testServer(PORT,
                           app()),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(401)
                           .when()
                           .get("/restrict/c/restrictedToFooOrBar"));

    }

    @Test
    public void testRestrictedToFooAndNotBar_noUserPresent()
    {
        running(testServer(PORT,
                           fakeApplication()),
                () -> RestAssured.given()
                           .cookie("user", "greet")
                           .expect()
                           .statusCode(401)
                           .when()
                           .get("/restrict/c/restrictedToFooAndNotBar"));
    }

    @Test
    public void testRestrictedToFooAndNotBar_unrestricted_noUserPresent()
    {
        running(testServer(PORT,
                           fakeApplication()),
                () -> RestAssured.given()
                           .cookie("user", "greet")
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/restrict/c/restrictedToFooAndNotBar/open"));
    }

    @Test
    public void testRestrictedToFooAndNotBar_bothRolesPresent()
    {
        running(testServer(PORT,
                           app()),
                () -> RestAssured.given()
                           .cookie("user", "greet")
                           .expect()
                           .statusCode(401)
                           .when()
                           .get("/restrict/c/restrictedToFooAndNotBar"));

    }

    @Test
    public void testRestrictedToFooAndNotBar_onlyBarPresent()
    {
        running(testServer(PORT,
                           app()),
                () -> RestAssured.given()
                           .cookie("user", "steve")
                           .expect()
                           .statusCode(401)
                           .when()
                           .get("/restrict/c/restrictedToFooAndNotBar"));

    }

    @Test
    public void testRestrictedToFooAndNotBar_noRolesPresent()
    {
        running(testServer(PORT,
                           app()),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(401)
                           .when()
                           .get("/restrict/c/restrictedToFooAndNotBar"));

    }

    @Test
    public void testRestrictedToFooOrNotBar_noUserPresent()
    {
        running(testServer(PORT,
                           fakeApplication()),
                () -> RestAssured.given()
                           .cookie("user", "greet")
                           .expect()
                           .statusCode(401)
                           .when()
                           .get("/restrict/c/restrictedToFooOrNotBar"));
    }

    @Test
    public void testRestrictedToFooOrNotBar_unrestricted_noUserPresent()
    {
        running(testServer(PORT,
                           fakeApplication()),
                () -> RestAssured.given()
                           .cookie("user", "greet")
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/restrict/c/restrictedToFooOrNotBar/open"));
    }

    @Test
    public void testRestrictedToFooOrNotBar_bothRolesPresent()
    {
        running(testServer(PORT,
                           app()),
                () -> RestAssured.given()
                           .cookie("user", "greet")
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/restrict/c/restrictedToFooOrNotBar"));

    }

    @Test
    public void testRestrictedToFooOrNotBar_onlyBarPresent()
    {
        running(testServer(PORT,
                           app()),
                () -> RestAssured.given()
                           .cookie("user", "steve")
                           .expect()
                           .statusCode(401)
                           .when()
                           .get("/restrict/c/restrictedToFooOrNotBar"));

    }

    @Test
    public void testRestrictedToFooOrNotBar_onlyHurdyPresent()
    {
        running(testServer(PORT,
                           app()),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/restrict/c/restrictedToFooOrNotBar"));

    }
}
