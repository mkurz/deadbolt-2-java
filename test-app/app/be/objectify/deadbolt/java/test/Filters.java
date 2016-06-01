package be.objectify.deadbolt.java.test;

import be.objectify.deadbolt.java.filters.DeadboltRouteCommentFilter;
import play.http.HttpFilters;
import play.mvc.EssentialFilter;

import javax.inject.Inject;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class Filters implements HttpFilters
{
    private final DeadboltRouteCommentFilter deadbolt;

    @Inject
    public Filters(final DeadboltRouteCommentFilter deadbolt)
    {
        this.deadbolt = deadbolt;
    }

    @Override
    public EssentialFilter[] filters()
    {
        return new EssentialFilter[]{deadbolt};
    }
}
