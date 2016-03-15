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
// if you're not using Guice, see that DI's approach to binding multiple implementations
@BindingAnnotation
public @interface MainHandler
{
}
