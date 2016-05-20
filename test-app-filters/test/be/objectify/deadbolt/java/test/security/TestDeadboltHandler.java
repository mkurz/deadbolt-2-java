package be.objectify.deadbolt.java.test.security;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import be.objectify.deadbolt.java.ExecutionContextProvider;
import be.objectify.deadbolt.java.models.Subject;
import be.objectify.deadbolt.java.test.models.User;
import play.mvc.Http;

/**
 * Extends the project's default DeadboltHandler to get the subject based on a cookie value - good for testing ONLY!
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public class TestDeadboltHandler extends MyDeadboltHandler
{
    public TestDeadboltHandler(ExecutionContextProvider ecProvider)
    {
        super(ecProvider);
    }

    @Override
    public CompletionStage<Optional<? extends Subject>> getSubject(Http.Context context)
    {
        final Http.Cookie userCookie = context.request().cookie("user");
        return CompletableFuture.supplyAsync(() -> Optional.ofNullable(User.findByUserName(userCookie.value())));
    }
}
