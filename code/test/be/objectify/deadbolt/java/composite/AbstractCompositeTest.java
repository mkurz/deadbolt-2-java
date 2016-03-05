package be.objectify.deadbolt.java.composite;

import be.objectify.deadbolt.java.AbstractDeadboltHandler;
import be.objectify.deadbolt.java.DeadboltHandler;
import be.objectify.deadbolt.java.DynamicResourceHandler;
import be.objectify.deadbolt.java.models.Permission;
import be.objectify.deadbolt.java.models.Role;
import be.objectify.deadbolt.java.models.Subject;
import play.mvc.Http;
import play.mvc.Result;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public abstract class AbstractCompositeTest
{

    protected DeadboltHandler withDrh(final DynamicResourceHandler drh)
    {
        return new AbstractDeadboltHandler()
        {
            @Override
            public CompletionStage<Optional<Result>> beforeAuthCheck(Http.Context context)
            {
                return CompletableFuture.completedFuture(Optional.empty());
            }

            @Override
            public CompletionStage<Optional<DynamicResourceHandler>> getDynamicResourceHandler(Http.Context context)
            {
                return CompletableFuture.completedFuture(Optional.of(drh));
            }
        };
    }

    protected DeadboltHandler withSubject(final Supplier<Subject> subject)
    {
        return new AbstractDeadboltHandler()
        {
            @Override
            public CompletionStage<Optional<Result>> beforeAuthCheck(final Http.Context context)
            {
                return CompletableFuture.completedFuture(Optional.empty());
            }

            @Override
            public CompletionStage<Optional<Subject>> getSubject(final Http.Context context)
            {
                return CompletableFuture.completedFuture(Optional.ofNullable(subject.get()));
            }
        };
    }

    protected Subject subject()
    {
        return subject(Collections::emptyList,
                       Collections::emptyList);
    }

    protected Subject subject(final Permission... permissions)
    {
        return subject(Collections::emptyList,
                       () -> Arrays.asList(permissions));
    }

    protected Subject subject(final Role... roles)
    {
        return subject(() -> Arrays.asList(roles),
                       Collections::emptyList);
    }

    protected boolean toBoolean(final CompletionStage<Boolean> cs) throws Exception
    {
        return ((CompletableFuture<Boolean>)cs).get();
    }

    protected Subject subject(Supplier<List<? extends Role>> roles,
                              Supplier<List<? extends Permission>> permissions)
    {
        return new Subject()
        {
            @Override
            public List<? extends Role> getRoles()
            {
                return roles.get();
            }

            @Override
            public List<? extends Permission> getPermissions()
            {
                return permissions.get();
            }

            @Override
            public String getIdentifier()
            {
                return "foo";
            }
        };
    }
}
