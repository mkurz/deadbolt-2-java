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
import be.objectify.deadbolt.java.cache.BeforeAuthCheckCache;
import be.objectify.deadbolt.java.cache.HandlerCache;
import com.typesafe.config.Config;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * A dynamic restriction is user-defined, and so completely arbitrary.  Hence, no checks on subjects, etc, occur
 * here.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public class DynamicAction extends AbstractRestrictiveAction<Dynamic>
{
    @Inject
    public DynamicAction(final HandlerCache handlerCache,
                         final BeforeAuthCheckCache beforeAuthCheckCache,
                         final Config config,
                         final ConstraintLogic constraintLogic)
    {
        super(handlerCache,
              beforeAuthCheckCache,
              config,
              constraintLogic);
    }

    public DynamicAction(final HandlerCache handlerCache,
                         final BeforeAuthCheckCache beforeAuthCheckCache,
                         final Config config,
                         final Dynamic configuration,
                         final Action<?> delegate,
                         final ConstraintLogic constraintLogic)
    {
        this(handlerCache,
             beforeAuthCheckCache,
             config,
             constraintLogic);
        this.configuration = configuration;
        this.delegate = delegate;
    }

    @Override
    public CompletionStage<Result> applyRestriction(final Http.Context ctx,
                                                    final DeadboltHandler deadboltHandler)
    {
        return constraintLogic.dynamic(ctx,
                                       deadboltHandler,
                                       Optional.ofNullable(configuration.content()),
                                       getValue(),
                                       getMeta(),
                                       this::authorizeAndExecute,
                                       this::unauthorizeAndFail,
                                       ConstraintPoint.CONTROLLER);
    }

    public Optional<String> getMeta()
    {
        return Optional.ofNullable(configuration.meta());
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
