package be.objectify.deadbolt.java.test.security;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PARAMETER})
@BindingAnnotation
public @interface SomeOtherHandler
{
}
