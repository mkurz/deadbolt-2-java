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
import be.objectify.deadbolt.java.ExecutionContextProvider;
import be.objectify.deadbolt.java.JavaAnalyzer;
import be.objectify.deadbolt.java.cache.HandlerCache;
import be.objectify.deadbolt.java.cache.SubjectCache;
import play.Configuration;
import play.libs.concurrent.HttpExecution;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;

/**
 * Implements the {@link Restrict} functionality, i.e. within an {@link Group} roles are ANDed, and between
 * {@link Group}s the role groups are ORed.
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public class RestrictAction extends AbstractRestrictiveAction<Restrict>
{
    @Inject
    public RestrictAction(final JavaAnalyzer analyzer,
                          final SubjectCache subjectCache,
                          final HandlerCache handlerCache,
                          final Configuration config,
                          final ExecutionContextProvider ecProvider)
    {
        super(analyzer,
              subjectCache,
              handlerCache,
              config,
              ecProvider);
    }

    public RestrictAction(final JavaAnalyzer analyzer,
                          final SubjectCache subjectCache,
                          final HandlerCache handlerCache,
                          final Configuration config,
                          final Restrict configuration,
                          final Action<?> delegate,
                          final ExecutionContextProvider ecProvider)
    {
        this(analyzer,
             subjectCache,
             handlerCache,
             config,
             ecProvider);
        this.configuration = configuration;
        this.delegate = delegate;
    }

    @Override
    public CompletionStage<Result> applyRestriction(final Http.Context ctx,
                                                    final DeadboltHandler deadboltHandler)
    {
        return getSubject(ctx,
                          deadboltHandler)
                .thenApplyAsync(subjectOption -> {
                    boolean roleOk = false;
                    if (subjectOption.isPresent())
                    {
                        final List<String[]> roleGroups = getRoleGroups();

                        for (int i = 0; !roleOk && i < roleGroups.size(); i++)
                        {
                            roleOk = checkRole(subjectOption,
                                               roleGroups.get(i));
                        }
                    }
                    return roleOk;
                }, HttpExecution.defaultContext())
                .thenComposeAsync(allowed -> {
                    final CompletionStage<Result> result;
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
                }, HttpExecution.defaultContext());
    }

    public List<String[]> getRoleGroups()
    {
        final List<String[]> roleGroups = new ArrayList<>();
        for (Group group : configuration.value())
        {
            roleGroups.add(group.value());
        }
        return roleGroups;
    }

    @Override
    public String getHandlerKey()
    {
        return configuration.handlerKey();
    }
}
