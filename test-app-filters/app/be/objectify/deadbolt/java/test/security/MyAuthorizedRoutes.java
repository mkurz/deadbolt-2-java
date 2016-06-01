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
                                                 "/restrict/rp/restrictedToFooAndBar",
                                                 filterConstraints.restrict(allOfGroup("foo", "bar"))),
                             new AuthorizedRoute(GET,
                                                 "/restrict/rp/restrictedToFooOrBar",
                                                 filterConstraints.restrict(anyOf(allOf("foo"), allOf("bar")))),
                             new AuthorizedRoute(GET,
                                                 "/restrict/rp/restrictedToFooAndNotBar",
                                                 filterConstraints.restrict(allOfGroup("foo", "!bar"))),
                             new AuthorizedRoute(GET,
                                                 "/restrict/rp/restrictedToFooOrNotBar",
                                                 filterConstraints.restrict(anyOf(allOf("foo"), allOf("!bar")))),
                             new AuthorizedRoute(GET,
                                                 "/subject/present/rp/subjectMustBePresent",
                                                 filterConstraints.subjectPresent()),
                             new AuthorizedRoute(GET,
                                                 "/subject/present/rp/subjectMustBePresentInUnrestrictedController",
                                                 filterConstraints.subjectPresent()),
                             new AuthorizedRoute(GET,
                                                 "/subject/not/present/rp/subjectMustNotBePresent",
                                                 filterConstraints.subjectNotPresent()),
                             new AuthorizedRoute(GET,
                                                 "/dynamic/rp/niceName",
                                                 filterConstraints.dynamic("niceName")),
                             new AuthorizedRoute(GET,
                                                 "/composite/rp/foo",
                                                 filterConstraints.composite("curatorOrSubjectNotPresent")),
                             new AuthorizedRoute(GET,
                                                 "/pattern/equality/rp/checkEquality",
                                                 filterConstraints.pattern("killer.undead.zombie",
                                                                           PatternType.EQUALITY)),
                             new AuthorizedRoute(GET,
                                                 "/pattern/regex/rp/checkExactMatch",
                                                 filterConstraints.pattern("killer.undead.zombie",
                                                                           PatternType.REGEX)),
                             new AuthorizedRoute(GET,
                                                 "/pattern/regex/rp/checkHierarchicalMatch",
                                                 filterConstraints.pattern("killer.undead.*",
                                                                           PatternType.REGEX)),
                             new AuthorizedRoute(GET,
                                                 "/pattern/custom/rp/checkCustom",
                                                 filterConstraints.pattern("i-do-not-like-ice-cream",
                                                                           PatternType.CUSTOM)),
                             new AuthorizedRoute(GET,
                                                 "/pattern/invert/equality/rp/checkEquality",
                                                 filterConstraints.pattern("killer.undead.zombie",
                                                                           PatternType.EQUALITY,
                                                                           true)),
                             new AuthorizedRoute(GET,
                                                 "/pattern/invert/regex/rp/checkExactMatch",
                                                 filterConstraints.pattern("killer.undead.zombie",
                                                                           PatternType.REGEX,
                                                                           true)),
                             new AuthorizedRoute(GET,
                                                 "/pattern/invert/regex/rp/checkHierarchicalMatch",
                                                 filterConstraints.pattern("killer.undead.*",
                                                                           PatternType.REGEX,
                                                                           true)),
                             new AuthorizedRoute(GET,
                                                 "/pattern/invert/custom/rp/checkCustom",
                                                 filterConstraints.pattern("i-do-not-like-ice-cream",
                                                                           PatternType.CUSTOM,
                                                                           true)));
    }
}
