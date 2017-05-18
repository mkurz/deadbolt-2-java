/*
 * Copyright 2013 Steve Chaloner
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
package be.objectify.deadbolt.java.test.controllers;

import com.jayway.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;

import static play.test.Helpers.fakeApplication;
import static play.test.Helpers.running;
import static play.test.Helpers.testServer;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public abstract class PatternEqualityTest extends AbstractApplicationTest
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
                           fakeApplication()),
                () -> RestAssured.given()
                                 .cookie("user", "greet")
                                 .expect()
                                 .statusCode(401)
                                 .when()
                                 .get(String.format("/pattern/equality/%s/checkEquality",
                                                    pathComponent())));
    }

    @Test
    public void testProtectedByMethodLevelEquality_subjectDoesNotHavePermission()
    {
        running(testServer(PORT,
                           app()),
                () -> RestAssured.given()
                                 .cookie("user", "lotte")
                                 .expect()
                                 .statusCode(401)
                                 .when()
                                 .get(String.format("/pattern/equality/%s/checkEquality",
                                                    pathComponent())));
    }

    @Test
    public void testProtectedByMethodLevelEquality_subjectHasPermission()
    {
        running(testServer(PORT,
                           app()),
                () -> RestAssured.given()
                                 .cookie("user", "greet")
                                 .expect()
                                 .statusCode(200)
                                 .when()
                                 .get(String.format("/pattern/equality/%s/checkEquality",
                                                    pathComponent())));
    }

    @Test
    public void testProtectedByMethodLevelEquality_noSubjectIsPresent_inverted()
    {
        running(testServer(PORT,
                           fakeApplication()),
                () -> RestAssured.given()
                                 .cookie("user", "greet")
                                 .expect()
                                 .statusCode(401)
                                 .when()
                                 .get(String.format("/pattern/invert/equality/%s/checkEquality",
                                                    pathComponent())));
    }

    @Test
    public void testProtectedByMethodLevelEquality_subjectDoesNotHavePermission_inverted()
    {
        running(testServer(PORT,
                           app()),
                () -> RestAssured.given()
                                 .cookie("user", "lotte")
                                 .expect()
                                 .statusCode(200)
                                 .when()
                                 .get(String.format("/pattern/invert/equality/%s/checkEquality",
                                                    pathComponent())));
    }


    @Test
    public void testProtectedByMethodLevelEquality_subjectHasPermission_inverted()
    {
        running(testServer(PORT,
                           app()),
                () -> RestAssured.given()
                                 .cookie("user", "greet")
                                 .expect()
                                 .statusCode(401)
                                 .when()
                                 .get(String.format("/pattern/invert/equality/%s/checkEquality",
                                                    pathComponent())));
    }
}
