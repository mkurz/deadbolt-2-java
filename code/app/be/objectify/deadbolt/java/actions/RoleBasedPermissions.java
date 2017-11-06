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
package be.objectify.deadbolt.java.actions;

import be.objectify.deadbolt.java.ConfigKeys;
import play.mvc.With;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Steve Chaloner (steve@objectify.be)
 * @since 2.5.1
 */
@With(RoleBasedPermissionsAction.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
@Documented
@Inherited
public @interface RoleBasedPermissions
{
    /**
     * The role name.  Permissions associated with the role, obtained via {@link be.objectify.deadbolt.java.DeadboltHandler#getPermissionsForRole(String)},
     * are used to test for authorization.
     *
     * @return the role name
     */
    String value();

    /**
     * Indicates the expected response type.  Useful when working with non-HTML responses.  This is free text, which you
     * can use in {@link be.objectify.deadbolt.java.DeadboltHandler#onAuthFailure} to decide on how to handle the response.
     *
     * @return a content indicator
     */
    String content() default "";

    /**
     * Use a specific {@link be.objectify.deadbolt.java.DeadboltHandler} for this restriction in place of the global
     * one, identified by a key.
     *
     * @return the ky of the handler
     */
    String handlerKey() default ConfigKeys.DEFAULT_HANDLER_KEY;

    /**
     * If true, the annotation will only be run if there is a {@link DeferredDeadbolt} annotation at the class level.
     *
     * @return true iff the associated action should be deferred until class-level annotations are applied.
     */
    boolean deferred() default false;
}