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
import io.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;

import static play.test.Helpers.running;
import static play.test.Helpers.testServer;

/**
 * @author Matthias Kurz (m.kurz@irregular.at)
 */
public class PatternModesTest extends AbstractApplicationTest
{

    private static final int PORT = 3333;

    @Before
    public void setUp()
    {
        RestAssured.port = PORT;
    }

    @Test
    public void testDefault_subjectHasPermission()
    {
        running(testServer(PORT,
                           app()),
                () -> RestAssured.given()
                           .cookie("user", "tom") // has both permissions
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/pattern/mode/default"));
    }

    @Test
    public void testDefault_subjectDoesNotHavePermission()
    {
        running(testServer(PORT,
                           app()),
                () -> RestAssured.given()
                           .cookie("user", "greet") // misses one of the two permissions
                           .expect()
                           .statusCode(401)
                           .when()
                           .get("/pattern/mode/default"));
    }

    @Test
    public void testAnd_subjectHasPermission()
    {
        running(testServer(PORT,
                           app()),
                () -> RestAssured.given()
                           .cookie("user", "tom") // has both permissions
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/pattern/mode/and"));
    }

    @Test
    public void testAnd_subjectDoesNotHavePermission()
    {
        running(testServer(PORT,
                           app()),
                () -> RestAssured.given()
                           .cookie("user", "greet") // misses one of the two permissions
                           .expect()
                           .statusCode(401)
                           .when()
                           .get("/pattern/mode/and"));
    }
    
    @Test
    public void testOr_subjectHasAllPermissions()
    {
        running(testServer(PORT,
                           app()),
                () -> RestAssured.given()
                           .cookie("user", "tom") // has both permissions even though one would be enough
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/pattern/mode/or"));
    }

    @Test
    public void testOr_subjectHasOnlyOnePermission()
    {
        running(testServer(PORT,
                           app()),
                () -> RestAssured.given()
                           .cookie("user", "greet") // has (only) one of the two permissions - passes now too!
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/pattern/mode/or"));
    }

    @Test
    public void testOr_subjectDoesNotHavePermission()
    {
        running(testServer(PORT,
                           app()),
                () -> RestAssured.given()
                           .cookie("user", "lotte") // does not even have one of the two permissions needed...
                           .expect()
                           .statusCode(401)
                           .when()
                           .get("/pattern/mode/or"));
    }
}
