package be.objectify.deadbolt.java;

import javax.inject.Singleton;

/**
 * No-op implementation of {@link TemplateFailureListener}.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
@Singleton
public class NoOpTemplateFailureListener implements TemplateFailureListener
{
    @Override
    public void failure(String message, long timeout)
    {
        // no-op
    }
}
