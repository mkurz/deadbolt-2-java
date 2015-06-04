package be.objectify.deadbolt.java.cache;

import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.DeadboltHandler;
import play.libs.F;
import play.mvc.Http;

import java.util.Optional;
import java.util.function.BiFunction;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public interface SubjectCache extends BiFunction<DeadboltHandler, Http.Context, F.Promise<Optional<Subject>>>
{
}
