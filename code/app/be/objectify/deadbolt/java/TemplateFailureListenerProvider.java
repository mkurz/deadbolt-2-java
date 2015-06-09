package be.objectify.deadbolt.java;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Play;

import javax.inject.Provider;
import javax.inject.Singleton;

/**
 * Looks for custom implementations of {@link TemplateFailureListener} in the injector.  Provides a no-op version
 * if nothing else can be found.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
@Singleton
public class TemplateFailureListenerProvider implements Provider<TemplateFailureListener>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateFailureListenerProvider.class);

    @Override
    public TemplateFailureListener get()
    {
        TemplateFailureListener listener;
        try
        {
            listener = Play.application().injector().instanceOf(TemplateFailureListener.class);
            LOGGER.info("Custom TemplateFailureListener found: [{}]", listener.getClass());
        }
        catch (Exception e)
        {
            LOGGER.info("No custom TemplateFailureListener found, falling back to no-op implementation");
            listener = new NoOpTemplateFailureListener();
        }
        return listener;
    }
}
