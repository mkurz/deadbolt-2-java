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
package be.objectify.deadbolt.java.test.controllers.pattern;

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
public class InvertedRegexControllerConstraintsTest extends AbstractApplicationTest
{

    private static final int PORT = 3333;

    @Before
    public void setUp()
    {
        RestAssured.port = PORT;
    }

    @Test
    public void testProtectedByControllerLevelRegex_noSubjectIsPresent()
    {
        running(testServer(PORT,
                           fakeApplication()),
                () ->
                {
                    RestAssured.given()
                               .cookie("user", "greet")
                               .expect()
                               .statusCode(401)
                               .when()
                               .get("/pattern/invert/regex/c/checkMatch");
                });
    }

    @Test
    public void testProtectedByControllerLevelRegex_subjectDoesNotHavePermission()
    {
        running(testServer(PORT,
                           app()),
                () ->
                {
                    RestAssured.given()
                               .cookie("user", "steve")
                               .expect()
                               .statusCode(200)
                               .when()
                               .get("/pattern/invert/regex/c/checkMatch");
                });
    }

    @Test
    public void testProtectedByControllerLevelRegex_zombieKiller()
    {
        running(testServer(PORT,
                           app()),
                () ->
                {
                    RestAssured.given()
                               .cookie("user", "greet")
                               .expect()
                               .statusCode(401)
                               .when()
                               .get("/pattern/invert/regex/c/checkMatch");
                });
    }

    @Test
    public void testProtectedByControllerLevelRegex_vampireKiller()
    {
        running(testServer(PORT,
                           app()),
                () ->
                {
                    RestAssured.given()
                               .cookie("user", "lotte")
                               .expect()
                               .statusCode(401)
                               .when()
                               .get("/pattern/invert/regex/c/checkMatch");
                });
    }

    @Test
    public void testUnrestricted_noSubjectIsPresent()
    {
        running(testServer(PORT,
                           fakeApplication()),
                () ->
                {
                    RestAssured.given()
                               .cookie("user", "greet")
                               .expect()
                               .statusCode(200)
                               .when()
                               .get("/pattern/invert/regex/c/checkMatch/open");
                });
    }

    @Test
    public void testUnrestricted_subjectHasPermission()
    {
        running(testServer(PORT,
                           app()),
                () ->
                {
                    RestAssured.given()
                               .cookie("user", "steve")
                               .expect()
                               .statusCode(200)
                               .when()
                               .get("/pattern/invert/regex/c/checkMatch/open");
                });
    }


    @Test
    public void testUnrestricted_subjectDoesNotHavePermission()
    {
        running(testServer(PORT,
                           app()),
                () ->
                {
                    RestAssured.given()
                               .cookie("user", "greet")
                               .expect()
                               .statusCode(200)
                               .when()
                               .get("/pattern/invert/regex/c/checkMatch/open");
                });
    }
}
