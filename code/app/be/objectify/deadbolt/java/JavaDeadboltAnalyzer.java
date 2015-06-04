package be.objectify.deadbolt.java;

import be.objectify.deadbolt.core.models.Subject;
import play.libs.F;
import play.mvc.Http;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public interface JavaDeadboltAnalyzer
{
    /**
     * Checks a custom pattern using the {@link DynamicResourceHandler} obtained via the handler.
     *
     * @param handler the handler
     * @param context the context
     * @param value the pattern value
     * @return true iff the custom check succeeds
     */
    F.Promise<Boolean> checkCustomPattern(DeadboltHandler handler,
                                          Http.Context context,
                                          String value);

    /**
     * Checks if the subject has all the role names.  In other words, this gives AND support.
     *
     * @param subjectOption an option for the subject
     * @param roleNames  the role names.  Any role name starting with ! will be negated.
     * @return true if the subject meets the restrictions (so access will be allowed), otherwise false
     */
    boolean checkRole(final Optional<Subject> subjectOption,
                             final String[] roleNames);

    /**
     * Gets the role name of each role held.
     *
     * @param subjectOption an option for the subject
     * @return a non-null list containing all role names
     */
    List<String> getRoleNames(Optional<Subject> subjectOption);

    /**
     * Check if the subject has the given role.
     *
     * @param subjectOption an option for the subject
     * @param roleName the name of the role
     * @return true iff the subject has the role represented by the role name
     */
    boolean hasRole(Optional<Subject> subjectOption,
                    String roleName);

    /**
     * Check if the {@link Subject} has all the roles given in the roleNames array.  Note that while a Subject must
     * have all the roles, it may also have other roles.
     *
     * @param subjectOption an option for the subject
     * @param roleNames the names of the required roles
     * @return true iff the subject has all the roles
     */
    boolean hasAllRoles(Optional<Subject> subjectOption,
                        String[] roleNames);

    /**
     * Check the pattern for a match against the {@link be.objectify.deadbolt.core.models.Permission}s of the user.
     *
     * @param subjectOption an option for the subject
     * @param patternOption an option for the pattern
     * @return true iff the pattern matches at least one of the subject's permissions
     */
    boolean checkRegexPattern(Optional<Subject> subjectOption,
                              Optional<Pattern> patternOption);

    /**
     * Check the pattern for equality against the {@link be.objectify.deadbolt.core.models.Permission}s of the user.
     *
     * @param subjectOption an option for the subject
     * @param patternValueOption an option for the pattern value
     * @return true iff the pattern is equal to at least one of the subject's permissions
     */
    boolean checkPatternEquality(Optional<Subject> subjectOption,
                                 Optional<String> patternValueOption);
}
