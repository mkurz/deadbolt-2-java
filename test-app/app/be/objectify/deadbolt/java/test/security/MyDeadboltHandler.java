/*
 * Copyright 2010-2017 Steve Chaloner
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
package be.objectify.deadbolt.java.test.security;

import be.objectify.deadbolt.java.ConfigKeys;
import be.objectify.deadbolt.java.ConstraintPoint;
import be.objectify.deadbolt.java.ExecutionContextProvider;
import be.objectify.deadbolt.java.models.Permission;
import be.objectify.deadbolt.java.models.Subject;
import be.objectify.deadbolt.java.AbstractDeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import be.objectify.deadbolt.java.test.dao.UserDao;
import be.objectify.deadbolt.java.test.models.SecurityPermission;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@HandlerQualifiers.MainHandler
public class MyDeadboltHandler extends AbstractDeadboltHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MyDeadboltHandler.class);

    private final DynamicResourceHandler dynamicHandler;
    private final UserDao userDao;

    @Inject
    public MyDeadboltHandler(final ExecutionContextProvider ecProvider,
                             final UserDao userDao)
    {
        super(ecProvider);
        Map<String, DynamicResourceHandler> delegates = new HashMap<>();
        delegates.put("niceName",
                      new NiceNameDynamicResourceHandler());
        this.dynamicHandler = new CompositeDynamicResourceHandler(delegates);
        this.userDao = userDao;
    }

    @Override
    public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context)
    {
        final Optional<Http.Cookie> maybeUserCookie = Optional.ofNullable(context.request().cookie("user"));
        return CompletableFuture.supplyAsync(() -> maybeUserCookie.flatMap(cookie -> userDao.getByUserName(cookie.value())));
    }

    @Override
    public CompletionStage<Optional<Result>> beforeAuthCheck(final Http.Context context, final Optional<String> content)
    {
        return CompletableFuture.completedFuture(Optional.empty());
    }

    @Override
    public CompletionStage<Optional<DynamicResourceHandler>> getDynamicResourceHandler(final Http.Context context)
    {
        return CompletableFuture.supplyAsync(() -> Optional.of(dynamicHandler));
    }

    @Override
    public CompletionStage<List<? extends Permission>> getPermissionsForRole(final String roleName) {
        return CompletableFuture.completedFuture(Collections.singletonList(new SecurityPermission("killer.undead.*")));
    }

    @Override
    public String handlerName()
    {
        return ConfigKeys.DEFAULT_HANDLER_KEY;
    }

    @Override
    public void onAuthSuccess(Http.Context context,
                              String constraintType,
                              ConstraintPoint constraintPoint)
    {
        LOGGER.info("[{} - {}] - authorization succeeded for [{}]",
                    constraintPoint,
                    constraintType,
                    context.args);
    }
}
