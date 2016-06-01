package be.objectify.deadbolt.java.test;

import javax.inject.Inject;

import be.objectify.deadbolt.java.filters.DeadboltRouteCommentFilter;
import be.objectify.deadbolt.java.filters.DeadboltRoutePathFilter;
import play.http.HttpFilters;
import play.mvc.EssentialFilter;

public class Filters implements HttpFilters {

    private final DeadboltRoutePathFilter deadboltRoutePaths;
    private final DeadboltRouteCommentFilter deadboltComments;

    @Inject
    public Filters(final DeadboltRoutePathFilter deadboltRoutePaths,
                   final DeadboltRouteCommentFilter deadboltComments) {
        this.deadboltRoutePaths = deadboltRoutePaths;
        this.deadboltComments = deadboltComments;
    }

    @Override
    public EssentialFilter[] filters() {
        return new EssentialFilter[]
                {deadboltRoutePaths,
                deadboltComments};
    }
}
