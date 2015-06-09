package be.objectify.deadbolt.java.test.security;

import be.objectify.deadbolt.java.TemplateFailureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@Singleton
public class MyCustomTemplateFailureListener implements TemplateFailureListener
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MyCustomTemplateFailureListener.class);

    @Override
    public void failure(final String message,
                        final long timeout)
    {
        LOGGER.error("Template constraint failure: message [{}]  timeout [{}]ms",
                     message,
                     timeout);
    }
}
