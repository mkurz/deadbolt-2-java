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
package be.objectify.deadbolt.java;

import be.objectify.deadbolt.java.models.Permission;
import be.objectify.deadbolt.java.models.Subject;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * DeadboltHandler implementations are the main hook into the Deadbolt system.  Here, you can apply authentication
 * checks using {@link DeadboltHandler#beforeAuthCheck}, get the current user, decide what to do when access fails and
 * provide implementations for dynamic be.objectify.deadbolt.java.test.security.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public interface DeadboltHandler
{

    /**
     * Gets the unique id of this DeadboltHandler instance. Each instance of a DeadboltHandler interface gets
     * an application wide unique id.
     *
     * @return the application wide unique id of this DeadboltHandler instance.
     */
    long getId();

    /**
     * Invoked immediately before controller or view restrictions are checked. This forms the integration with any
     * authentication actions that may need to occur.
     *
     * @param context the HTTP context
     * @return the action result if an action other than the delegate must be taken, otherwise null. For a case where
     * the user is authenticated (or whatever your test condition is), this will be null otherwise the restriction
     * won't be applied.
     */
    CompletionStage<Optional<Result>> beforeAuthCheck(Http.Context context);

    /**
     * Gets the current {@link Subject}, e.g. the current user.
     *
     * @param context the HTTP context
     * @return the current subject
     */
    CompletionStage<Optional<? extends Subject>> getSubject(Http.Context context);

    /**
     * Invoked when an access failure is detected on <i>controllerClassName</i>.
     *
     * @param context the HTTP context
     * @param content the content type hint.  This can be used to return a response in the appropriate content
     *                type, e.g. JSON
     * @return the action result
     */
    CompletionStage<Result> onAuthFailure(Http.Context context,
                                          Optional<String> content);

    /**
     * Gets the handler used for dealing with resources restricted to specific users/groups.
     *
     * @param context the HTTP context
     * @return the handler for restricted resources. May be null.
     */
    CompletionStage<Optional<DynamicResourceHandler>> getDynamicResourceHandler(Http.Context context);

    /**
     * Gets the canonical name of the handler.  Defaults to the class name.
     *
     * @return whatever the implementor considers the canonical name of the handler to be
     */
    default String handlerName()
    {
        return getClass().getName();
    }

    /**
     * Invoked when access to a resource is authorized.
     *
     * @param context the context, can be used to get various bits of information such as the route and method
     * @param constraintType the type of constraint, e.g. Dynamic, etc
     * @param constraintPoint the point at which the constraint was applied
     */
    default void onAuthSuccess(final Http.Context context,
                               final String constraintType,
                               final ConstraintPoint constraintPoint) {
        // no-op
    }

    /**
     * Get the permissions associated with a role.
     *
     * @param roleName the role the permissions are associated with
     * @return a non-null list containing the permissions associated with the role
     */
    default CompletionStage<List<? extends Permission>> getPermissionsForRole(String roleName)
    {
        return CompletableFuture.completedFuture(Collections.emptyList());
    }
}
