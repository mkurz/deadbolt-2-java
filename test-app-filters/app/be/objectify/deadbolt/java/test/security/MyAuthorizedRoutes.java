/*
 * Copyright 2013 Steve Chaloner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.objectify.deadbolt.java.test.security;

import be.objectify.deadbolt.java.filters.AuthorizedRoute;
import be.objectify.deadbolt.java.filters.AuthorizedRoutes;
import be.objectify.deadbolt.java.filters.FilterConstraints;
import be.objectify.deadbolt.java.models.PatternType;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static be.objectify.deadbolt.java.utils.TemplateUtils.allOf;
import static be.objectify.deadbolt.java.utils.TemplateUtils.allOfGroup;
import static be.objectify.deadbolt.java.utils.TemplateUtils.anyOf;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class MyAuthorizedRoutes extends AuthorizedRoutes
{

    private static final Optional<String> GET = Optional.of("GET");

    @Inject
    public MyAuthorizedRoutes(final Provider<FilterConstraints> filterConstraints)
    {
        super(filterConstraints);
    }

    @Override
    public List<AuthorizedRoute> routes()
    {
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
                                                                           true)),
                             new AuthorizedRoute(GET,
                                                 "/rbp/rp/index",
                                                 filterConstraints.roleBasedPermissions("foo")));
    }
}
