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
package be.objectify.deadbolt.java.actions;

import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.JavaAnalyzer;
import be.objectify.deadbolt.java.cache.HandlerCache;
import be.objectify.deadbolt.java.cache.SubjectCache;
import play.libs.F;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;

/**
 * A dynamic restriction is user-defined, and so completely arbitrary.  Hence, no checks on subjects, etc, occur
 * here.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public class DynamicAction extends AbstractRestrictiveAction<Dynamic>
{
    @Inject
    public DynamicAction(final JavaAnalyzer analyzer,
                         final SubjectCache subjectCache,
                         final HandlerCache handlerCache)
    {
        super(analyzer,
              subjectCache,
              handlerCache);
    }

    public DynamicAction(final JavaAnalyzer analyzer,
                         final SubjectCache subjectCache,
                         final HandlerCache handlerCache,
                         final Dynamic configuration,
                         final Action<?> delegate)
    {
        this(analyzer,
             subjectCache,
             handlerCache);
        this.configuration = configuration;
        this.delegate = delegate;
    }

    @Override
    public F.Promise<Result> applyRestriction(final Http.Context ctx,
                                              final DeadboltHandler deadboltHandler)
    {
        return deadboltHandler.getDynamicResourceHandler(ctx)
                              .map(option -> option.orElseThrow(() -> new RuntimeException("A dynamic resource is specified but no dynamic resource handler is provided")))
                              .flatMap(drh -> drh.isAllowed(getValue(),
                                                            getMeta(),
                                                            deadboltHandler,
                                                            ctx))
                              .flatMap(allowed -> {
                                  final F.Promise<Result> result;
                                  if (allowed)
                                  {
                                      markActionAsAuthorised(ctx);
                                      result = delegate.call(ctx);
                                  }
                                  else
                                  {
                                      markActionAsUnauthorised(ctx);
                                      result = onAuthFailure(deadboltHandler,
                                                             configuration.content(),
                                                             ctx);
                                  }
                                  return result;
                              });
    }

    public String getMeta()
    {
        return configuration.meta();
    }

    public String getValue()
    {
        return configuration.value();
    }

    @Override
    public String getHandlerKey()
    {
        return configuration.handlerKey();
    }
}
