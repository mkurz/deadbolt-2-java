package be.objectify.deadbolt.java;

/**
 * Listens for failures when applying constraints at the template level.   Useful for extra logging or creating
 * a load-based template constraint timeout function.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public interface TemplateFailureListener
{
    void failure(String message,
                 long timeout);
}
