package be.objectify.deadbolt.java.test.security;

import be.objectify.deadbolt.java.ConfigKeys;
import be.objectify.deadbolt.java.ExecutionContextProvider;
import be.objectify.deadbolt.java.models.Subject;
import be.objectify.deadbolt.java.AbstractDeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import be.objectify.deadbolt.java.test.models.User;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.HashMap;
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
    private final DynamicResourceHandler dynamicHandler;

    @Inject
    public MyDeadboltHandler(final ExecutionContextProvider ecProvider)
    {
        super(ecProvider);
        Map<String, DynamicResourceHandler> delegates = new HashMap<>();
        delegates.put("niceName",
                      new NiceNameDynamicResourceHandler());
        this.dynamicHandler = new CompositeDynamicResourceHandler(delegates);
    }

    @Override
    public CompletionStage<Optional<? extends Subject>> getSubject(final Http.Context context)
    {
        final Http.Cookie userCookie = context.request().cookie("user");
        return CompletableFuture.supplyAsync(() -> Optional.ofNullable(User.findByUserName(userCookie.value())));
    }

    @Override
    public CompletionStage<Optional<Result>> beforeAuthCheck(final Http.Context context)
    {
        return CompletableFuture.completedFuture(Optional.empty());
    }

    @Override
    public CompletionStage<Optional<DynamicResourceHandler>> getDynamicResourceHandler(final Http.Context context)
    {
        return CompletableFuture.supplyAsync(() -> Optional.of(dynamicHandler));
    }

    @Override
    public String handlerName()
    {
        return ConfigKeys.DEFAULT_HANDLER_KEY;
    }
}
