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

import be.objectify.deadbolt.java.ConstraintAnnotationMode;
import be.objectify.deadbolt.java.ConstraintLogic;
import be.objectify.deadbolt.java.ConstraintPoint;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.cache.BeforeAuthCheckCache;
import be.objectify.deadbolt.java.cache.HandlerCache;
import com.typesafe.config.Config;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Convenience class for checking if an action has already been authorised before applying the restrictions.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public abstract class AbstractRestrictiveAction<T> extends AbstractDeadboltAction<T>
{
    final ConstraintLogic constraintLogic;

    public AbstractRestrictiveAction(final HandlerCache handlerCache,
                                     final BeforeAuthCheckCache beforeAuthCheckCache,
                                     final Config config,
                                     final ConstraintLogic constraintLogic)
    {
        super(handlerCache,
              beforeAuthCheckCache,
              config);
        this.constraintLogic = constraintLogic;
    }

    @Override
    public CompletionStage<Result> execute(final Http.Context ctx) throws Exception
    {
        final DeadboltHandler deadboltHandler = getDeadboltHandler(getHandlerKey());
        return preAuth(true,
                         ctx,
                         getContent(),
                         deadboltHandler)
                .thenCompose(preAuthResult -> preAuthResult.map(value -> (CompletionStage<Result>) CompletableFuture.completedFuture(value))
                                                           .orElseGet(() -> applyRestriction(ctx,
                                                                                             deadboltHandler)));
    }

    public abstract Optional<String> getContent();

    /**
     * Get the key of a specific DeadboltHandler instance.
     *
     * @return a key.  May be null.
     */
    public abstract String getHandlerKey();

    public abstract CompletionStage<Result> applyRestriction(Http.Context ctx,
                                                             DeadboltHandler deadboltHandler);

    protected CompletionStage<Result> applyRestriction(final Http.Context ctx,
                                       final DeadboltHandler deadboltHandler,
                                       final int valueIndex,
                                       final Pattern configuration) {
    return constraintLogic.pattern(ctx,
                deadboltHandler,
                getContent(),
                configuration.value()[valueIndex],
                configuration.patternType(),
                Optional.ofNullable(configuration.meta()),
                configuration.invert(),
                context -> { // the current check passed
                    if(ConstraintAnnotationMode.AND.equals(configuration.mode())) { // AND mode
                        if(configuration.value().length > (valueIndex + 1)) {
                            // there is at least another check left - that one has to pass to in AND mode
                            return this.applyRestriction(ctx, deadboltHandler, valueIndex + 1, configuration);
                        } else {
                            // there is no other check left - we can finish ;)
                            return this.authorizeAndExecute(context);
                        }
                    } else { // OR mode
                        // No need to look at the next (if there would be any), just finish ;)
                        return this.authorizeAndExecute(context);
                    }
                },
                (context, handler, content) -> { // the current check failed
                    if(ConstraintAnnotationMode.AND.equals(configuration.mode())) { // AND mode
                        // No need to look at the next (if there would be any), just fail ;(
                        return this.unauthorizeAndFail(context, handler, content);
                    } else { // OR mode
                        if(configuration.value().length > (valueIndex + 1)) {
                            // there is at least another check left - give that one the chance to pass
                            return this.applyRestriction(ctx, deadboltHandler, valueIndex + 1, configuration);
                        } else {
                            // if lastone else look at the next one....
                            return this.unauthorizeAndFail(context, handler, content);
                        }
                    }
                },
                ConstraintPoint.CONTROLLER);
    }
}
