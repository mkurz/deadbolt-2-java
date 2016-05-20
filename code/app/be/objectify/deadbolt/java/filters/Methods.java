package be.objectify.deadbolt.java.filters;

import java.util.Optional;

/**
 * HTTP methods used when defining {@link AuthorizedRoute}s.
 *
 * To apply a constraint to all methods for a given path, use ANY.
 *
 * @since 2.5.1
 * @author Steve Chaloner (steve@objectify.be)
 */
public final class Methods
{
    private Methods() {
        // no-op
    }

    public static final Optional<String> ANY = Optional.empty();
    public static final Optional<String> GET = Optional.of("GET");
    public static final Optional<String> POST = Optional.of("POST");
    public static final Optional<String> DELETE = Optional.of("DELETE");
    public static final Optional<String> PUT = Optional.of("PUT");
    public static final Optional<String> PATCH = Optional.of("PATCH");
    public static final Optional<String> OPTIONS = Optional.of("OPTIONS");
    public static final Optional<String> HEAD = Optional.of("HEAD");
}
