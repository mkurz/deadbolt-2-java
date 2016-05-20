package be.objectify.deadbolt.java.test;

import javax.inject.Inject;
import be.objectify.deadbolt.java.filters.DeadboltFilter;
import play.http.HttpFilters;
import play.mvc.EssentialFilter;

public class Filters implements HttpFilters {

    private final DeadboltFilter deadbolt;

    @Inject
    public Filters(final DeadboltFilter deadbolt) {
        this.deadbolt = deadbolt;
    }

    @Override
    public EssentialFilter[] filters() {
        return new EssentialFilter[]{deadbolt};
    }
}
