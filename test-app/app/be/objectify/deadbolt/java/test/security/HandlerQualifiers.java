package be.objectify.deadbolt.java.test.security;

import com.google.inject.BindingAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * if you're not using Guice, see that DI's approach to binding multiple implementations.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public class HandlerQualifiers
{
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.PARAMETER})
    @BindingAnnotation
    public @interface MainHandler
    {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.PARAMETER})
    @BindingAnnotation
    public @interface SomeOtherHandler
    {
    }
}
