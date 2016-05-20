package be.objectify.deadbolt.java.test.security;

import static be.objectify.deadbolt.java.utils.TemplateUtils.allOf;
import static be.objectify.deadbolt.java.utils.TemplateUtils.allOfGroup;
import static be.objectify.deadbolt.java.utils.TemplateUtils.anyOf;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Provider;
import be.objectify.deadbolt.java.filters.AuthorizedRoute;
import be.objectify.deadbolt.java.filters.AuthorizedRoutes;
import be.objectify.deadbolt.java.filters.FilterConstraints;
import be.objectify.deadbolt.java.models.PatternType;
import be.objectify.deadbolt.java.utils.TemplateUtils;

public class MyAuthorizedRoutes extends AuthorizedRoutes {

    private static final Optional<String> GET = Optional.of("GET");

    @Inject
    public MyAuthorizedRoutes(final Provider<FilterConstraints> filterConstraints) {
        super(filterConstraints);
    }

    @Override
    public List<AuthorizedRoute> routes() {
        return Arrays.asList(new AuthorizedRoute(GET,
                                                 "/restrict/m/restrictedToFooAndBar",
                                                 filterConstraints.restrict(allOfGroup("foo", "bar"))),
                             new AuthorizedRoute(GET,
                                                 "/restrict/m/restrictedToFooOrBar",
                                                 filterConstraints.restrict(anyOf(allOf("foo"), allOf("bar")))),
                             new AuthorizedRoute(GET,
                                                 "/restrict/m/restrictedToFooAndNotBar",
                                                 filterConstraints.restrict(allOfGroup("foo", "!bar"))),
                             new AuthorizedRoute(GET,
                                                 "/restrict/m/restrictedToFooOrNotBar",
                                                 filterConstraints.restrict(anyOf(allOf("foo"), allOf("!bar")))),
                             new AuthorizedRoute(GET,
                                                 "/subject/present/m/subjectMustBePresent",
                                                 filterConstraints.subjectPresent()),
                             new AuthorizedRoute(GET,
                                                 "/subject/present/m/subjectMustBePresentInUnrestrictedController",
                                                 filterConstraints.subjectPresent()),
                             new AuthorizedRoute(GET,
                                                 "/subject/not/present/m/subjectMustNotBePresent",
                                                 filterConstraints.subjectNotPresent()),
                             new AuthorizedRoute(GET,
                                                 "/dynamic/m/niceName",
                                                 filterConstraints.dynamic("niceName")),
                             new AuthorizedRoute(GET,
                                                 "/composite/m/foo",
                                                 filterConstraints.composite("curatorOrSubjectNotPresent")),
                             new AuthorizedRoute(GET,
                                                 "/pattern/equality/m/checkEquality",
                                                 filterConstraints.pattern("killer.undead.zombie",
                                                                           PatternType.EQUALITY)),
                             new AuthorizedRoute(GET,
                                                 "/pattern/regex/m/checkExactMatch",
                                                 filterConstraints.pattern("killer.undead.zombie",
                                                                           PatternType.REGEX)),
                             new AuthorizedRoute(GET,
                                                 "/pattern/regex/m/checkHierarchicalMatch",
                                                 filterConstraints.pattern("killer.undead.*",
                                                                           PatternType.REGEX)),
                             new AuthorizedRoute(GET,
                                                 "/pattern/custom/m/checkCustom",
                                                 filterConstraints.pattern("i-do-not-like-ice-cream",
                                                                           PatternType.CUSTOM)),
                             new AuthorizedRoute(GET,
                                                 "/pattern/invert/equality/m/checkEquality",
                                                 filterConstraints.pattern("killer.undead.zombie",
                                                                           PatternType.EQUALITY,
                                                                           true)),
                             new AuthorizedRoute(GET,
                                                 "/pattern/invert/regex/m/checkExactMatch",
                                                 filterConstraints.pattern("killer.undead.zombie",
                                                                           PatternType.REGEX,
                                                                           true)),
                             new AuthorizedRoute(GET,
                                                 "/pattern/invert/regex/m/checkHierarchicalMatch",
                                                 filterConstraints.pattern("killer.undead.*",
                                                                           PatternType.REGEX,
                                                                           true)),
                             new AuthorizedRoute(GET,
                                                 "/pattern/invert/custom/m/checkCustom",
                                                 filterConstraints.pattern("i-do-not-like-ice-cream",
                                                                           PatternType.CUSTOM,
                                                                           true)));
    }
}
