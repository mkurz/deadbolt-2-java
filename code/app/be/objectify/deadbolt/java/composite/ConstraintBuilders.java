/*
 * Copyright 2010-2016 Steve Chaloner
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
package be.objectify.deadbolt.java.composite;

import be.objectify.deadbolt.java.ConstraintLogic;
import be.objectify.deadbolt.java.models.PatternType;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@Singleton
public class ConstraintBuilders
{
    private final ConstraintLogic constraintLogic;

    @Inject
    public ConstraintBuilders(final ConstraintLogic constraintLogic)
    {
        this.constraintLogic = constraintLogic;
    }

    public SubjectPresentBuilder subjectPresent()
    {
        return new SubjectPresentBuilder(constraintLogic);
    }

    public SubjectNotPresentBuilder subjectNotPresent()
    {
        return new SubjectNotPresentBuilder(constraintLogic);
    }

    public DynamicBuilder dynamic(final String name)
    {
        return new DynamicBuilder(name,
                                  constraintLogic);
    }

    public PatternBuilder pattern(final String value,
                                  final PatternType patternType)
    {
        return new PatternBuilder(value,
                                  patternType,
                                  constraintLogic);
    }

    public RestrictBuilder restrict(final List<String[]> roleGroups)
    {
        return new RestrictBuilder(roleGroups,
                                   constraintLogic);
    }

    public String[] allOf(String... roleNames)
    {
        return roleNames;
    }

    public List<String[]> anyOf(String[]... roleGroups)
    {
        return Arrays.asList(roleGroups);
    }

    public static final class SubjectPresentBuilder
    {
        private final ConstraintLogic constraintLogic;
        private Optional<String> content = Optional.empty();

        private SubjectPresentBuilder(final ConstraintLogic constraintLogic)
        {
            this.constraintLogic = constraintLogic;
        }

        public SubjectPresentBuilder content(final Optional<String> content)
        {
            this.content = content == null ? Optional.empty()
                                           : content;
            return this;
        }

        public SubjectPresentConstraint build()
        {
            return new SubjectPresentConstraint(content,
                                                constraintLogic);
        }
    }

    public static final class SubjectNotPresentBuilder
    {
        private final ConstraintLogic constraintLogic;
        private Optional<String> content = Optional.empty();

        private SubjectNotPresentBuilder(final ConstraintLogic constraintLogic)
        {
            this.constraintLogic = constraintLogic;
        }

        public SubjectNotPresentBuilder content(final Optional<String> content)
        {
            this.content = content == null ? Optional.empty()
                                           : content;
            return this;
        }

        public SubjectNotPresentConstraint build()
        {
            return new SubjectNotPresentConstraint(content,
                                                   constraintLogic);
        }
    }

    public static final class RestrictBuilder
    {
        private final List<String[]> roleGroups = new LinkedList<>();
        private final ConstraintLogic constraintLogic;
        private Optional<String> content = Optional.empty();

        private RestrictBuilder(final List<String[]> roleGroups,
                                final ConstraintLogic constraintLogic)
        {
            if (roleGroups != null)
            {
                roleGroups.stream()
                          .filter(Objects::nonNull)
                          .collect(Collectors.toCollection(() -> this.roleGroups));
            }
            this.constraintLogic = constraintLogic;
        }

        public RestrictBuilder content(final Optional<String> content)
        {
            this.content = content == null ? Optional.empty()
                                           : content;
            return this;
        }

        public RestrictConstraint build()
        {
            return new RestrictConstraint(roleGroups,
                                          content,
                                          constraintLogic);
        }
    }

    public static final class PatternBuilder
    {
        private final String value;
        private final PatternType patternType;
        private final ConstraintLogic constraintLogic;
        private boolean invert = false;
        private Optional<String> meta = Optional.empty();
        private Optional<String> content = Optional.empty();

        private PatternBuilder(final String value,
                               final PatternType patternType,
                               final ConstraintLogic constraintLogic)
        {
            this.value = value;
            this.patternType = patternType;
            this.constraintLogic = constraintLogic;
        }

        public PatternBuilder meta(final Optional<String> meta)
        {
            this.meta = meta == null ? Optional.empty()
                                     : meta;
            return this;
        }

        public PatternBuilder invert(final boolean invert)
        {
            this.invert = invert;
            return this;
        }

        public PatternBuilder content(final Optional<String> content)
        {
            this.content = content == null ? Optional.empty()
                                           : content;
            return this;
        }

        public PatternConstraint build()
        {
            return new PatternConstraint(value,
                                         patternType,
                                         meta,
                                         invert,
                                         content,
                                         constraintLogic);
        }
    }

    public static final class DynamicBuilder
    {
        private final String name;
        private final ConstraintLogic constraintLogic;
        private Optional<String> meta = Optional.empty();
        private Optional<String> content = Optional.empty();

        private DynamicBuilder(final String name,
                               final ConstraintLogic constraintLogic)
        {
            this.name = name;
            this.constraintLogic = constraintLogic;
        }

        public DynamicBuilder meta(final Optional<String> meta)
        {
            this.meta = meta == null ? Optional.empty()
                                     : meta;
            return this;
        }

        public DynamicBuilder content(final Optional<String> content)
        {
            this.content = content == null ? Optional.empty()
                                           : content;
            return this;
        }

        public DynamicConstraint build()
        {
            return new DynamicConstraint(name,
                                         meta,
                                         content,
                                         constraintLogic);
        }
    }
}
