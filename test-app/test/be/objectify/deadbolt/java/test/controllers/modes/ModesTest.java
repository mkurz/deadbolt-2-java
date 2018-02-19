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
package be.objectify.deadbolt.java.test.controllers.modes;

import be.objectify.deadbolt.java.test.controllers.AbstractApplicationTest;

import com.google.common.collect.ImmutableMap;
import com.jayway.restassured.RestAssured;
import org.junit.Before;
import org.junit.Test;

import static play.test.Helpers.running;
import static play.test.Helpers.testServer;

/**
 * @author Matthias Kurz (m.kurz@irregular.at)
 */
public class ModesTest extends AbstractApplicationTest
{

    private static final int PORT = 3333;
    private static final String CONFIG_KEY_MODE = "deadbolt.java.constraint-mode";
    private static final String CONFIG_KEY_CAF = "play.http.actionComposition.controllerAnnotationsFirst";

    @Before
    public void setUp()
    {
        RestAssured.port = PORT;
    }

    @Test
    public void testAnd_subjectHasPermission_methodAnnotationsFirst()
    {
        running(testServer(PORT,
                           app(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "AND",
                                    CONFIG_KEY_CAF, "false"))),
                () -> RestAssured.given()
                           .cookie("user", "greet")
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/mode/checkMode"));
    }

    @Test
    public void testAnd_subjectHasPermission_controllerAnnotationsFirst()
    {
        running(testServer(PORT,
                           app(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "AND",
                                    CONFIG_KEY_CAF, "true"))),
                () -> RestAssured.given()
                           .cookie("user", "greet")
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/mode/checkMode"));
    }

    @Test
    public void testAnd_subjectDoesNotHavePermission_methodAnnotationsFirst()
    {
        running(testServer(PORT,
                           app(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "AND",
                                    CONFIG_KEY_CAF, "false"))),
                () -> RestAssured.given()
                           .cookie("user", "mani")
                           .expect()
                           .statusCode(401)
                           .when()
                           .get("/mode/checkMode"));
    }

    @Test
    public void testAnd_subjectDoesNotHavePermission_controllerAnnotationsFirst()
    {
        running(testServer(PORT,
                           app(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "AND",
                                    CONFIG_KEY_CAF, "true"))),
                () -> RestAssured.given()
                           .cookie("user", "mani")
                           .expect()
                           .statusCode(401)
                           .when()
                           .get("/mode/checkMode"));
    }

    @Test
    public void testAnd_noSubjectIsPresent_methodAnnotationsFirst()
    {
        running(testServer(PORT,
                           appWithoutUser(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "AND",
                                    CONFIG_KEY_CAF, "false"))),
                () -> RestAssured.given()
                           .cookie("user", "greet") // usually that user would pass, but no user present in backend
                           .expect()
                           .statusCode(401)
                           .when()
                           .get("/mode/checkMode"));
    }

    @Test
    public void testAnd_noSubjectIsPresent_controllerAnnotationsFirst()
    {
        running(testServer(PORT,
                          appWithoutUser(ImmutableMap.of(
                                   CONFIG_KEY_MODE, "AND",
                                   CONFIG_KEY_CAF, "true"))),
                () -> RestAssured.given()
                           .cookie("user", "greet") // usually that user would pass, but no user present in backend
                           .expect()
                           .statusCode(401)
                           .when()
                           .get("/mode/checkMode"));
    }

    @Test
    public void testOr_subjectHasPermission_methodAnnotationsFirst()
    {
        running(testServer(PORT,
                           app(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "OR",
                                    CONFIG_KEY_CAF, "false"))),
                () -> RestAssured.given()
                           .cookie("user", "mani") // same user which fails in AND mode has permission in OR mode (because role is ok!)
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/mode/checkMode"));
    }

    @Test
    public void testOr_subjectHasPermission_controllerAnnotationsFirst()
    {
        running(testServer(PORT,
                           app(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "OR",
                                    CONFIG_KEY_CAF, "true"))),
                () -> RestAssured.given()
                           .cookie("user", "mani") // same user which fails in AND mode has permission in OR mode (because role is ok!)
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/mode/checkMode"));
    }

    @Test
    public void testOr_subjectDoesNotHavePermission_methodAnnotationsFirst()
    {
        running(testServer(PORT,
                           app(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "OR",
                                    CONFIG_KEY_CAF, "false"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(401)
                           .when()
                           .get("/mode/checkMode"));
    }

    @Test
    public void testOr_subjectDoesNotHavePermission_controllerAnnotationsFirst()
    {
        running(testServer(PORT,
                           app(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "OR",
                                    CONFIG_KEY_CAF, "true"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(401)
                           .when()
                           .get("/mode/checkMode"));
    }

    @Test
    public void testOr_noSubjectIsPresent_methodAnnotationsFirst()
    {
        running(testServer(PORT,
                           appWithoutUser(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "OR",
                                    CONFIG_KEY_CAF, "false"))),
                () -> RestAssured.given()
                           .cookie("user", "mani") // usually that user would pass, but no user present in backend
                           .expect()
                           .statusCode(401)
                           .when()
                           .get("/mode/checkMode"));
    }

    @Test
    public void testOr_noSubjectIsPresent_controllerAnnotationsFirst()
    {
        running(testServer(PORT,
                           appWithoutUser(ImmutableMap.of(
                                   CONFIG_KEY_MODE, "OR",
                                   CONFIG_KEY_CAF, "true"))),
                () -> RestAssured.given()
                           .cookie("user", "mani") // usually that user would pass, but no user present in backend
                           .expect()
                           .statusCode(401)
                           .when()
                           .get("/mode/checkMode"));
    }

    @Test
    public void testDefaultMode_subjectDoesNotHavePermission_methodAnnotationsFirst()
    {
        running(testServer(PORT,
                           app(ImmutableMap.of(
                                    //CONFIG_KEY_MODE, "PROCESS_FIRST_CONSTRAINT_ONLY", // that's the default
                                    CONFIG_KEY_CAF, "false"))), // false is the default in Play
                () -> RestAssured.given()
                           .cookie("user", "mani")
                           .expect()
                           .statusCode(401) // We fail Because the method annotation is processed first (and nothing else)
                           .when()
                           .get("/mode/checkMode"));
    }

    @Test
    public void testDefaultMode_subjectDoesHavePermission_controllerAnnotationsFirst()
    {
        running(testServer(PORT,
                           app(ImmutableMap.of(
                                    //CONFIG_KEY_MODE, "PROCESS_FIRST_CONSTRAINT_ONLY", // that's the default
                                    CONFIG_KEY_CAF, "true"))),
                () -> RestAssured.given()
                           .cookie("user", "mani")
                           .expect()
                           .statusCode(200) // WE NOW PASS!!! Because now the controller annotation is processed first (and nothing else)
                           .when()
                           .get("/mode/checkMode"));
    }

    @Test
    public void testDefaultMode_noSubjectIsPresent_methodAnnotationsFirst()
    {
        running(testServer(PORT,
                           appWithoutUser(ImmutableMap.of(
                                    //CONFIG_KEY_MODE, "PROCESS_FIRST_CONSTRAINT_ONLY", // that's the default
                                    CONFIG_KEY_CAF, "false"))), // false is the default in Play
                () -> RestAssured.given()
                           .cookie("user", "greet") // use a user that would usually pass
                           .expect()
                           .statusCode(401)
                           .when()
                           .get("/mode/checkMode"));
    }

    @Test
    public void testDefaultMode_noSubjectIsPresent_controllerAnnotationsFirst()
    {
        running(testServer(PORT,
                           appWithoutUser(ImmutableMap.of(
                                    //CONFIG_KEY_MODE, "PROCESS_FIRST_CONSTRAINT_ONLY", // that's the default
                                    CONFIG_KEY_CAF, "true"))),
                () -> RestAssured.given()
                           .cookie("user", "mani")
                           .expect()
                           .statusCode(401)
                           .when()
                           .get("/mode/checkMode"));
    }

    @Test
    public void testAnd_Unrestricted_subjectHasPermission_methodAnnotationsFirst()
    {
        running(testServer(PORT,
                           app(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "AND",
                                    CONFIG_KEY_CAF, "false"))),
                () -> RestAssured.given()
                           .cookie("user", "greet")
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/mode/checkModeUnrestricted"));
    }

    @Test
    public void testAnd_Unrestricted_subjectHasPermission_controllerAnnotationsFirst()
    {
        running(testServer(PORT,
                           app(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "AND",
                                    CONFIG_KEY_CAF, "true"))),
                () -> RestAssured.given()
                           .cookie("user", "greet")
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/mode/checkModeUnrestricted"));
    }

    @Test
    public void testAnd_Unrestricted_subjectDoesNotHavePermission_methodAnnotationsFirst()
    {
        running(testServer(PORT,
                           app(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "AND",
                                    CONFIG_KEY_CAF, "false"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(401)
                           .when()
                           .get("/mode/checkModeUnrestricted"));
    }

    @Test
    public void testAnd_Unrestricted_subjectDoesNotHavePermission_controllerAnnotationsFirst()
    {
        running(testServer(PORT,
                           app(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "AND",
                                    CONFIG_KEY_CAF, "true"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(401)
                           .when()
                           .get("/mode/checkModeUnrestricted"));
    }

    @Test
    public void testAnd_Unrestricted_noSubjectIsPresent_methodAnnotationsFirst()
    {
        running(testServer(PORT,
                           appWithoutUser(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "AND",
                                    CONFIG_KEY_CAF, "false"))),
                () -> RestAssured.given()
                           .cookie("user", "greet") // usually that user would pass, but no user present in backend
                           .expect()
                           .statusCode(401)
                           .when()
                           .get("/mode/checkModeUnrestricted"));
    }

    @Test
    public void testAnd_Unrestricted_noSubjectIsPresent_controllerAnnotationsFirst()
    {
        running(testServer(PORT,
                          appWithoutUser(ImmutableMap.of(
                                   CONFIG_KEY_MODE, "AND",
                                   CONFIG_KEY_CAF, "true"))),
                () -> RestAssured.given()
                           .cookie("user", "greet") // usually that user would pass, but no user present in backend
                           .expect()
                           .statusCode(401)
                           .when()
                           .get("/mode/checkModeUnrestricted"));
    }

    @Test
    public void testOr_Unrestricted_subjectHasPermission_methodAnnotationsFirst()
    {
        running(testServer(PORT,
                           app(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "OR",
                                    CONFIG_KEY_CAF, "false"))),
                () -> RestAssured.given()
                           .cookie("user", "mani")
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/mode/checkModeUnrestricted"));
    }

    @Test
    public void testOr_Unrestricted_subjectHasPermission_controllerAnnotationsFirst()
    {
        running(testServer(PORT,
                           app(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "OR",
                                    CONFIG_KEY_CAF, "true"))),
                () -> RestAssured.given()
                           .cookie("user", "mani")
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/mode/checkModeUnrestricted"));
    }

    @Test
    public void testOr_Unrestricted_subjectDoesNotHavePermission_methodAnnotationsFirst()
    {
        running(testServer(PORT,
                           app(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "OR",
                                    CONFIG_KEY_CAF, "false"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/mode/checkModeUnrestricted"));
    }

    @Test
    public void testOr_Unrestricted_subjectDoesNotHavePermission_controllerAnnotationsFirst()
    {
        running(testServer(PORT,
                           app(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "OR",
                                    CONFIG_KEY_CAF, "true"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/mode/checkModeUnrestricted"));
    }

    @Test
    public void testOr_Unrestricted_noSubjectIsPresent_methodAnnotationsFirst()
    {
        running(testServer(PORT,
                           appWithoutUser(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "OR",
                                    CONFIG_KEY_CAF, "false"))),
                () -> RestAssured.given()
                           .cookie("user", "mani")
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/mode/checkModeUnrestricted"));
    }

    @Test
    public void testOr_Unrestricted_noSubjectIsPresent_controllerAnnotationsFirst()
    {
        running(testServer(PORT,
                           appWithoutUser(ImmutableMap.of(
                                   CONFIG_KEY_MODE, "OR",
                                   CONFIG_KEY_CAF, "true"))),
                () -> RestAssured.given()
                           .cookie("user", "mani")
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/mode/checkModeUnrestricted"));
    }

    @Test
    public void testDefaultMode_Unrestricted_subjectDoesNotHavePermission_methodAnnotationsFirst()
    {
        running(testServer(PORT,
                           app(ImmutableMap.of(
                                    //CONFIG_KEY_MODE, "PROCESS_FIRST_CONSTRAINT_ONLY", // that's the default
                                    CONFIG_KEY_CAF, "false"))), // false is the default in Play
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(200) // Success because the method annotation (@Unrestricted) is processed first (and nothing else)
                           .when()
                           .get("/mode/checkModeUnrestricted"));
    }

    @Test
    public void testDefaultMode_Unrestricted_subjectDoesHavePermission_controllerAnnotationsFirst()
    {
        running(testServer(PORT,
                           app(ImmutableMap.of(
                                    //CONFIG_KEY_MODE, "PROCESS_FIRST_CONSTRAINT_ONLY", // that's the default
                                    CONFIG_KEY_CAF, "true"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(401) // Fails because now the controller annotation is processed first (and nothing else)
                           .when()
                           .get("/mode/checkModeUnrestricted"));
    }

    @Test
    public void testDefaultMode_Unrestricted_noSubjectIsPresent_methodAnnotationsFirst()
    {
        running(testServer(PORT,
                           appWithoutUser(ImmutableMap.of(
                                    //CONFIG_KEY_MODE, "PROCESS_FIRST_CONSTRAINT_ONLY", // that's the default
                                    CONFIG_KEY_CAF, "false"))), // false is the default in Play
                () -> RestAssured.given()
                           .cookie("user", "lotte") // use a user that would usually pass
                           .expect()
                           .statusCode(200) // Success because the method annotation (@Unrestricted) is processed first (and nothing else)
                           .when()
                           .get("/mode/checkModeUnrestricted"));
    }

    @Test
    public void testDefaultMode_Unrestricted_noSubjectIsPresent_controllerAnnotationsFirst()
    {
        running(testServer(PORT,
                           appWithoutUser(ImmutableMap.of(
                                    //CONFIG_KEY_MODE, "PROCESS_FIRST_CONSTRAINT_ONLY", // that's the default
                                    CONFIG_KEY_CAF, "true"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(401) // Fails because now the controller annotation is processed first (and nothing else)
                           .when()
                           .get("/mode/checkModeUnrestricted"));
    }

    @Test
    public void testAnd_deferredDeadbolt_subjectIsPresent_methodAnnotationsFirst()
    {
        running(testServer(PORT,
                           app(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "AND",
                                    CONFIG_KEY_CAF, "false"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/mode/NoConstraintsDeferredDeadbolt"));
    }

    @Test
    public void testAnd_deferredDeadbolt_subjectIsPresent_controllerAnnotationsFirst()
    {
        running(testServer(PORT,
                           app(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "AND",
                                    CONFIG_KEY_CAF, "true"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/mode/NoConstraintsDeferredDeadbolt"));
    }

    @Test
    public void testAnd_deferredDeadbolt_noSubjectIsPresent_methodAnnotationsFirst()
    {
        running(testServer(PORT,
                           appWithoutUser(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "AND",
                                    CONFIG_KEY_CAF, "false"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/mode/NoConstraintsDeferredDeadbolt"));
    }

    @Test
    public void testAnd_deferredDeadbolt_noSubjectIsPresent_controllerAnnotationsFirst()
    {
        running(testServer(PORT,
                           appWithoutUser(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "AND",
                                    CONFIG_KEY_CAF, "true"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/mode/NoConstraintsDeferredDeadbolt"));
    }

    @Test
    public void testOr_deferredDeadbolt_subjectIsPresent_methodAnnotationsFirst()
    {
        running(testServer(PORT,
                           app(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "OR",
                                    CONFIG_KEY_CAF, "false"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/mode/NoConstraintsDeferredDeadbolt"));
    }

    @Test
    public void testOr_deferredDeadbolt_subjectIsPresent_controllerAnnotationsFirst()
    {
        running(testServer(PORT,
                           app(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "OR",
                                    CONFIG_KEY_CAF, "true"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/mode/NoConstraintsDeferredDeadbolt"));
    }

    @Test
    public void testOr_deferredDeadbolt_noSubjectIsPresent_methodAnnotationsFirst()
    {
        running(testServer(PORT,
                           appWithoutUser(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "OR",
                                    CONFIG_KEY_CAF, "false"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/mode/NoConstraintsDeferredDeadbolt"));
    }

    @Test
    public void testOr_deferredDeadbolt_noSubjectIsPresent_controllerAnnotationsFirst()
    {
        running(testServer(PORT,
                           appWithoutUser(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "OR",
                                    CONFIG_KEY_CAF, "true"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/mode/NoConstraintsDeferredDeadbolt"));
    }

    @Test
    public void testDefaultMode_deferredDeadbolt_subjectIsPresent_methodAnnotationsFirst()
    {
        running(testServer(PORT,
                           app(ImmutableMap.of(
                                    //CONFIG_KEY_MODE, "PROCESS_FIRST_CONSTRAINT_ONLY", // that's the default
                                    CONFIG_KEY_CAF, "false"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/mode/NoConstraintsDeferredDeadbolt"));
    }

    @Test
    public void testDefaultMode_deferredDeadbolt_subjectIsPresent_controllerAnnotationsFirst()
    {
        running(testServer(PORT,
                           app(ImmutableMap.of(
                                    //CONFIG_KEY_MODE, "PROCESS_FIRST_CONSTRAINT_ONLY", // that's the default
                                    CONFIG_KEY_CAF, "true"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/mode/NoConstraintsDeferredDeadbolt"));
    }

    @Test
    public void testDefaultMode_deferredDeadbolt_noSubjectIsPresent_methodAnnotationsFirst()
    {
        running(testServer(PORT,
                           appWithoutUser(ImmutableMap.of(
                                    //CONFIG_KEY_MODE, "PROCESS_FIRST_CONSTRAINT_ONLY", // that's the default
                                    CONFIG_KEY_CAF, "false"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/mode/NoConstraintsDeferredDeadbolt"));
    }

    @Test
    public void testDefaultMode_deferredDeadbolt_noSubjectIsPresent_controllerAnnotationsFirst()
    {
        running(testServer(PORT,
                           appWithoutUser(ImmutableMap.of(
                                    //CONFIG_KEY_MODE, "PROCESS_FIRST_CONSTRAINT_ONLY", // that's the default
                                    CONFIG_KEY_CAF, "true"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/mode/NoConstraintsDeferredDeadbolt"));
    }

    @Test
    public void testAnd_beforeAccess_subjectIsPresent_methodAnnotationsFirst()
    {
        running(testServer(PORT,
                           app(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "AND",
                                    CONFIG_KEY_CAF, "false"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/mode/NoConstraintsBeforeAccess"));
    }

    @Test
    public void testAnd_beforeAccess_subjectIsPresent_controllerAnnotationsFirst()
    {
        running(testServer(PORT,
                           app(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "AND",
                                    CONFIG_KEY_CAF, "true"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/mode/NoConstraintsBeforeAccess"));
    }

    @Test
    public void testAnd_beforeAccess_noSubjectIsPresent_methodAnnotationsFirst()
    {
        running(testServer(PORT,
                           appWithoutUser(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "AND",
                                    CONFIG_KEY_CAF, "false"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/mode/NoConstraintsBeforeAccess"));
    }

    @Test
    public void testAnd_beforeAccess_noSubjectIsPresent_controllerAnnotationsFirst()
    {
        running(testServer(PORT,
                           appWithoutUser(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "AND",
                                    CONFIG_KEY_CAF, "true"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/mode/NoConstraintsBeforeAccess"));
    }

    @Test
    public void testOr_beforeAccess_subjectIsPresent_methodAnnotationsFirst()
    {
        running(testServer(PORT,
                           app(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "OR",
                                    CONFIG_KEY_CAF, "false"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/mode/NoConstraintsBeforeAccess"));
    }

    @Test
    public void testOr_beforeAccess_subjectIsPresent_controllerAnnotationsFirst()
    {
        running(testServer(PORT,
                           app(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "OR",
                                    CONFIG_KEY_CAF, "true"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/mode/NoConstraintsBeforeAccess"));
    }

    @Test
    public void testOr_beforeAccess_noSubjectIsPresent_methodAnnotationsFirst()
    {
        running(testServer(PORT,
                           appWithoutUser(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "OR",
                                    CONFIG_KEY_CAF, "false"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/mode/NoConstraintsBeforeAccess"));
    }

    @Test
    public void testOr_beforeAccess_noSubjectIsPresent_controllerAnnotationsFirst()
    {
        running(testServer(PORT,
                           appWithoutUser(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "OR",
                                    CONFIG_KEY_CAF, "true"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/mode/NoConstraintsBeforeAccess"));
    }

    @Test
    public void testDefaultMode_beforeAccess_subjectIsPresent_methodAnnotationsFirst()
    {
        running(testServer(PORT,
                           app(ImmutableMap.of(
                                    //CONFIG_KEY_MODE, "PROCESS_FIRST_CONSTRAINT_ONLY", // that's the default
                                    CONFIG_KEY_CAF, "false"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/mode/NoConstraintsBeforeAccess"));
    }

    @Test
    public void testDefaultMode_beforeAccess_subjectIsPresent_controllerAnnotationsFirst()
    {
        running(testServer(PORT,
                           app(ImmutableMap.of(
                                    //CONFIG_KEY_MODE, "PROCESS_FIRST_CONSTRAINT_ONLY", // that's the default
                                    CONFIG_KEY_CAF, "true"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/mode/NoConstraintsBeforeAccess"));
    }

    @Test
    public void testDefaultMode_beforeAccess_noSubjectIsPresent_methodAnnotationsFirst()
    {
        running(testServer(PORT,
                           appWithoutUser(ImmutableMap.of(
                                    //CONFIG_KEY_MODE, "PROCESS_FIRST_CONSTRAINT_ONLY", // that's the default
                                    CONFIG_KEY_CAF, "false"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/mode/NoConstraintsBeforeAccess"));
    }

    @Test
    public void testDefaultMode_beforeAccess_noSubjectIsPresent_controllerAnnotationsFirst()
    {
        running(testServer(PORT,
                           appWithoutUser(ImmutableMap.of(
                                    //CONFIG_KEY_MODE, "PROCESS_FIRST_CONSTRAINT_ONLY", // that's the default
                                    CONFIG_KEY_CAF, "true"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(200)
                           .when()
                           .get("/mode/NoConstraintsBeforeAccess"));
    }

    @Test
    public void testAnd_beforeAccessAndPattern_subjectIsPresent_methodAnnotationsFirst()
    {
        running(testServer(PORT,
                           app(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "AND",
                                    CONFIG_KEY_CAF, "false"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(401)
                           .when()
                           .get("/mode/NoConstraintsBeforeAccessAndPattern"));
    }

    @Test
    public void testAnd_beforeAccessAndPattern_subjectIsPresent_controllerAnnotationsFirst()
    {
        running(testServer(PORT,
                           app(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "AND",
                                    CONFIG_KEY_CAF, "true"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(401)
                           .when()
                           .get("/mode/NoConstraintsBeforeAccessAndPattern"));
    }

    @Test
    public void testAnd_beforeAccessAndPattern_noSubjectIsPresent_methodAnnotationsFirst()
    {
        running(testServer(PORT,
                           appWithoutUser(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "AND",
                                    CONFIG_KEY_CAF, "false"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(401)
                           .when()
                           .get("/mode/NoConstraintsBeforeAccessAndPattern"));
    }

    @Test
    public void testAnd_beforeAccessAndPattern_noSubjectIsPresent_controllerAnnotationsFirst()
    {
        running(testServer(PORT,
                           appWithoutUser(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "AND",
                                    CONFIG_KEY_CAF, "true"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(401)
                           .when()
                           .get("/mode/NoConstraintsBeforeAccessAndPattern"));
    }

    @Test
    public void testOr_beforeAccessAndPattern_subjectIsPresent_methodAnnotationsFirst()
    {
        running(testServer(PORT,
                           app(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "OR",
                                    CONFIG_KEY_CAF, "false"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(401)
                           .when()
                           .get("/mode/NoConstraintsBeforeAccessAndPattern"));
    }

    @Test
    public void testOr_beforeAccessAndPattern_subjectIsPresent_controllerAnnotationsFirst()
    {
        running(testServer(PORT,
                           app(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "OR",
                                    CONFIG_KEY_CAF, "true"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(401)
                           .when()
                           .get("/mode/NoConstraintsBeforeAccessAndPattern"));
    }

    @Test
    public void testOr_beforeAccessAndPattern_noSubjectIsPresent_methodAnnotationsFirst()
    {
        running(testServer(PORT,
                           appWithoutUser(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "OR",
                                    CONFIG_KEY_CAF, "false"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(401)
                           .when()
                           .get("/mode/NoConstraintsBeforeAccessAndPattern"));
    }

    @Test
    public void testOr_beforeAccessAndPattern_noSubjectIsPresent_controllerAnnotationsFirst()
    {
        running(testServer(PORT,
                           appWithoutUser(ImmutableMap.of(
                                    CONFIG_KEY_MODE, "OR",
                                    CONFIG_KEY_CAF, "true"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(401)
                           .when()
                           .get("/mode/NoConstraintsBeforeAccessAndPattern"));
    }

    @Test
    public void testDefaultMode_beforeAccessAndPattern_subjectIsPresent_methodAnnotationsFirst()
    {
        running(testServer(PORT,
                           app(ImmutableMap.of(
                                    //CONFIG_KEY_MODE, "PROCESS_FIRST_CONSTRAINT_ONLY", // that's the default
                                    CONFIG_KEY_CAF, "false"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(401)
                           .when()
                           .get("/mode/NoConstraintsBeforeAccessAndPattern"));
    }

    @Test
    public void testDefaultMode_beforeAccessAndPattern_subjectIsPresent_controllerAnnotationsFirst()
    {
        running(testServer(PORT,
                           app(ImmutableMap.of(
                                    //CONFIG_KEY_MODE, "PROCESS_FIRST_CONSTRAINT_ONLY", // that's the default
                                    CONFIG_KEY_CAF, "true"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(401)
                           .when()
                           .get("/mode/NoConstraintsBeforeAccessAndPattern"));
    }

    @Test
    public void testDefaultMode_beforeAccessAndPattern_noSubjectIsPresent_methodAnnotationsFirst()
    {
        running(testServer(PORT,
                           appWithoutUser(ImmutableMap.of(
                                    //CONFIG_KEY_MODE, "PROCESS_FIRST_CONSTRAINT_ONLY", // that's the default
                                    CONFIG_KEY_CAF, "false"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(401)
                           .when()
                           .get("/mode/NoConstraintsBeforeAccessAndPattern"));
    }

    @Test
    public void testDefaultMode_beforeAccessAndPattern_noSubjectIsPresent_controllerAnnotationsFirst()
    {
        running(testServer(PORT,
                           appWithoutUser(ImmutableMap.of(
                                    //CONFIG_KEY_MODE, "PROCESS_FIRST_CONSTRAINT_ONLY", // that's the default
                                    CONFIG_KEY_CAF, "true"))),
                () -> RestAssured.given()
                           .cookie("user", "lotte")
                           .expect()
                           .statusCode(401)
                           .when()
                           .get("/mode/NoConstraintsBeforeAccessAndPattern"));
    }
}
