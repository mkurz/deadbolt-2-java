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
package be.objectify.deadbolt.java.actions;

import be.objectify.deadbolt.java.ConstraintLogic;
import be.objectify.deadbolt.java.ConstraintPoint;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.cache.HandlerCache;
import play.Configuration;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class PatternAction extends AbstractRestrictiveAction<Pattern>
{
    @Inject
    public PatternAction(final HandlerCache handlerCache,
                         final Configuration config,
                         final ConstraintLogic constraintLogic)
    {
        super(handlerCache,
              config,
              constraintLogic);
    }

    public PatternAction(final HandlerCache handlerCache,
                         final Configuration config,
                         final Pattern configuration,
                         final Action<?> delegate,
                         final ConstraintLogic constraintLogic)
    {
        this(handlerCache,
             config,
             constraintLogic);
        this.configuration = configuration;
        this.delegate = delegate;
    }

    @Override
    public CompletionStage<Result> applyRestriction(final Http.Context ctx,
                                                    final DeadboltHandler deadboltHandler)
    {
        return constraintLogic.pattern(ctx,
                                       deadboltHandler,
                                       Optional.ofNullable(configuration.content()),
                                       getValue(),
                                       configuration.patternType(),
                                       getMeta(),
                                       configuration.invert(),
                                       this::authorizeAndExecute,
                                       this::unauthorizeAndFail,
                                       ConstraintPoint.CONTROLLER);
    }

    public String getValue()
    {
        return configuration.value();
    }

    public Optional<String> getMeta()
    {
        return Optional.ofNullable(configuration.meta());
    }

    @Override
    public String getHandlerKey()
    {
        return configuration.handlerKey();
    }
}
