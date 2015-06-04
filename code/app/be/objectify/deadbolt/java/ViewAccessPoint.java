package be.objectify.deadbolt.java;

import play.Play;

import javax.inject.Inject;

/**
 * I can't inject {@link DeadboltViewSupport} directly into views, so it's hack time...
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public class ViewAccessPoint
{
    public final static DeadboltViewSupport VIEW_SUPPORT;

    static {
        VIEW_SUPPORT = Play.application().injector().instanceOf(DeadboltViewSupport.class);
    }
}