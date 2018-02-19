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
package be.objectify.deadbolt.java.test.controllers.modes;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import be.objectify.deadbolt.java.actions.BeforeAccess;
import be.objectify.deadbolt.java.actions.DeferredDeadbolt;
import be.objectify.deadbolt.java.actions.Pattern;
import be.objectify.deadbolt.java.models.PatternType;
import play.mvc.Controller;
import play.mvc.Result;

/**
 * @DeferredDeadbolt and @BeforeAccess deadbolt annotations are not treated as constraints
 * 
 * @author Matthias Kurz (m.kurz@irregular.at)
 */
@DeferredDeadbolt // Just a placeholder for a deferred constraint (if there was one) - but itself doesn't mark anything as authorized
public class NoConstraintsController extends Controller
{
    /**
     * Doesn't matter if AND, OR or default mode, this action method will always be executed, because @DeferredDeadbolt doesn't do anything
     */
    public CompletionStage<Result> deferredDeadbolt()
    {
        return CompletableFuture.supplyAsync(() -> ok("Content accessible"));
    }

    /**
     * Will only fail when beforeAuthCheck(...) fails, otherwise action method will be executed
     */
    @BeforeAccess // Just triggers a beforeAuthCheck(...) - but doesn't take part in OR/AND mode au mark anything as authorized even when successful
    public CompletionStage<Result> beforeAccess()
    {
        return CompletableFuture.supplyAsync(() -> ok("Content accessible"));
    }

    /**
     * Of course when beforeAuthCheck(...) fails, the action method will be un-authorized, no matter which mode is used.
     * However an interesting test case is the OR mode now:
     * If beforeAuthCheck(...) is OK but the pattern constraint fails, the action method will be un-authorized because the @BeforeAccess
     * doesn't mark anything as authorized - again, it just triggers beforeAuthCheck(...) but nothing more.
     * For the default mode also only the pattern constraint matters (if beforeAuthCheck(...) was ok before)
     * The same for the AND mode.
     */
    @BeforeAccess // Just triggers a beforeAuthCheck(...) - but doesn't take part in OR/AND mode au mark anything as authorized even when successful
    @Pattern(value = "killer.undead.zombie", patternType = PatternType.EQUALITY)
    public CompletionStage<Result> beforeAccessAndPattern()
    {
        return CompletableFuture.supplyAsync(() -> ok("Content accessible"));
    }
}
