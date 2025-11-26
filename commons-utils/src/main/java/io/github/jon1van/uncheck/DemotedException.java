package io.github.jon1van.uncheck;

import java.io.Serial;

/// This class represents the middle ground between (A) rethrowing checked exceptions as simple
/// RuntimeExceptions using `throw new RuntimeException(someCheckedException);` and (B) full
/// scale Exception handling for exceptions that are frequently rethrown.
///
/// Option (A) is commonly used idiom that is considered bad practice because catching exceptions
/// thrown this way REQUIRES an overly broad `catch(RuntimeException re)` clause. On the other
/// hand, Option (B) is powerful but requires more verbose code that can make people want to opt for
/// Option (A).
///
/// DemotedExceptions, like Option (A), convert checked Exceptions to unchecked RuntimeException.
/// However, catching a DemotedException does not require a super generic catch clause. Also,
/// DemotedExceptions can ONLY be created from a checked Exceptions. Consequently, a
/// `catch(DemotedException de)` clause can only be used to handle checkedException.
///
/// This upside of using DemotedExceptions is that it cleans up messy method signatures that must
/// otherwise throw checked Exceptions. This helps to address the problem that makes people abuse the
/// idiom from Option (A).
///
/// The downside of using DemotedExceptions is that a layer of indirection was added when access to
/// the original checked Exception is required to properly handle the checked Exception.
///
/// A rule of thumb is to use DemotedExceptions when you expect calling code will frequently rethrow
/// or just fail anytime a checked Exception is thrown.
public class DemotedException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 9114768309613077242L;

    public DemotedException(Exception cause) {
        super(cause);
        throwOnRuntimeException(cause);
    }

    public DemotedException(String message, Exception cause) {
        super(message, cause);
        throwOnRuntimeException(cause);
    }

    /// A shortcut for new DemotedException(cause)
    public static DemotedException demote(Exception cause) {
        return new DemotedException(cause);
    }

    /// A shortcut for new DemotedException(message, cause)
    public static DemotedException demote(String message, Exception cause) {
        return new DemotedException(message, cause);
    }

    private static void throwOnRuntimeException(Exception cause) {

        /*
         * RuntimeException's should not be demoted, use this "preconditions" check to ensure:
         * (1) RuntimeExceptions don't get demoted and
         * (2) information about unexpected RuntimeExceptions get passed back to the caller
         */
        if (cause instanceof RuntimeException) {
            throw new IllegalArgumentException(
                    "Illegal Use of DemotedException, cannot demote RuntimeExceptions but "
                            + cause.getClass().getSimpleName() + " is an instance of RuntimeException",
                    cause);
        }
    }
}
