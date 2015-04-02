package be.objectify.deadbolt.java.views;

import be.objectify.deadbolt.java.AbstractDeadboltHandler;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public abstract class AbstractNoPreAuthDeadboltHandler extends AbstractDeadboltHandler
{
    @Override
    public F.Promise<Result> beforeAuthCheck(Http.Context context)
    {
        return F.Promise.pure(null);
    }
}
