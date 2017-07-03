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
package be.objectify.deadbolt.java.test;

import be.objectify.deadbolt.java.filters.DeadboltRouteCommentFilter;
import play.http.HttpFilters;
import play.mvc.EssentialFilter;

import java.util.Arrays;
import java.util.List;

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
    public List<EssentialFilter> getFilters()
    {
        return Arrays.asList(deadbolt);
    }
}
