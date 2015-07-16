package be.objectify.deadbolt.java.cache;

import be.objectify.deadbolt.core.models.Subject;
import be.objectify.deadbolt.java.ConfigKeys;
import be.objectify.deadbolt.java.DeadboltHandler;
import play.Configuration;
import play.libs.F;
import play.mvc.Http;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
@Singleton
public class DefaultSubjectCache implements SubjectCache
{
    private final boolean cacheUserPerRequestEnabled;

    private final boolean blocking;

    private final long blockingTimeout;

    @Inject
    public DefaultSubjectCache(final Configuration configuration)
    {
        this.cacheUserPerRequestEnabled = configuration.getBoolean(ConfigKeys.CACHE_DEADBOLT_USER,
                                                                   false);
        this.blocking = configuration.getBoolean(ConfigKeys.BLOCKING,
                false);

        this.blockingTimeout = configuration.getLong(ConfigKeys.DEFAULT_BLOCKING_TIMEOUT, 1000L);

    }

    @Override
    public F.Promise<Optional<Subject>> apply(final DeadboltHandler deadboltHandler,
                                              final Http.Context context)
    {
        F.Promise<Optional<Subject>> promise;
        if (cacheUserPerRequestEnabled)
        {
            final Optional<Subject> cachedUser = Optional.ofNullable((Subject) context.args.get(ConfigKeys.CACHE_DEADBOLT_USER));
            if (cachedUser.isPresent())
            {
                promise = F.Promise.pure(cachedUser);
            }
            else
            {
                promise = deadboltHandler.getSubject(context)
                                   .map(subjectOption -> {
                                       subjectOption.ifPresent(subject -> context.args.put(ConfigKeys.CACHE_DEADBOLT_USER,
                                                                                           subject));
                                       return subjectOption;
                                   });
            }
        }
        else
        {
            promise = deadboltHandler.getSubject(context);
        }
        
        if(this.blocking) {
            promise = F.Promise.pure(promise.get(this.blockingTimeout, TimeUnit.MILLISECONDS));
        }
        
        return promise;
    }
}
