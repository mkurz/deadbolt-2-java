/*
 * Copyright 2012 Steve Chaloner
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
package be.objectify.deadbolt.java;

import be.objectify.deadbolt.core.models.Subject;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import play.mvc.SimpleResult;

/**
 * Abstract implementation of {@link DeadboltHandler} that gives a standard unauthorised result when access fails.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public abstract class AbstractDeadboltHandler extends Results implements DeadboltHandler
{
    /**
     * {@inheritDoc}
     */
    public Subject getSubject(Http.Context context)
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public F.Promise<SimpleResult> onAuthFailure(Http.Context context,
                                                 String content)
    {
        return F.Promise.promise(new F.Function0<SimpleResult>()
        {
            @Override
            public SimpleResult apply() throws Throwable
            {
                return unauthorized(views.html.defaultpages.unauthorized.render());
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    public DynamicResourceHandler getDynamicResourceHandler(Http.Context context)
    {
        return null;
    }
}