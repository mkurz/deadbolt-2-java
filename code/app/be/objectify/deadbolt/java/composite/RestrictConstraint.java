/*
 * Copyright 2012-2016 Steve Chaloner
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

import be.objectify.deadbolt.java.DeadboltAnalyzer;
import be.objectify.deadbolt.java.DeadboltHandler;
import play.mvc.Http;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class RestrictConstraint implements Constraint
{
    private final List<String[]> roleGroups = new LinkedList<>();
    private final DeadboltAnalyzer analyzer;

    public RestrictConstraint(final List<String[]> roleGroups,
                              final DeadboltAnalyzer analyzer) {
        roleGroups.stream()
                  .filter(group -> group != null)
                  .collect(Collectors.toCollection(() -> this.roleGroups));
        this.analyzer = analyzer;
    }

    @Override
    public CompletionStage<Boolean> test(final Http.Context context,
                                         final DeadboltHandler handler,
                                         final Executor executor) {
        return handler.getSubject(context)
                      .thenApplyAsync(maybeSubject -> {
                          boolean roleOk = false;
                          for (int i = 0; !roleOk && i < roleGroups.size(); i++)
                          {
                              roleOk = analyzer.checkRole(maybeSubject,
                                                          roleGroups.get(i));
                          }
                          return roleOk;
                      },
                                      executor);
    }
}
