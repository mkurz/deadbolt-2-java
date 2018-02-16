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

import play.mvc.With;

import java.lang.annotation.Documented;
import static java.lang.annotation.ElementType.*;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.*;
import java.lang.annotation.Target;

import be.objectify.deadbolt.java.ConfigKeys;

/**
 * If a method-level Deadbolt annotation is marked as deferred, it can be run by adding this annotation to the class level.
 * This is useful if you have, for example, a class-level @Security.Authenticated(Secured.class) or similar.
 * See https://github.com/schaloner/deadbolt-2/issues/17 for the motivation..
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
@With(DeferredDeadboltAction.class)
@Repeatable(DeferredDeadbolt.List.class)
@Retention(RUNTIME)
@Target(TYPE)
@Documented
@Inherited
public @interface DeferredDeadbolt
{
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
     * Defines several {@code DeferredDeadbolt} annotations on the same element.
     */
    @Retention(RUNTIME)
    @Target(TYPE)
    @Documented
    @Inherited
    public @interface List {
        DeferredDeadbolt[] value();
    }
}