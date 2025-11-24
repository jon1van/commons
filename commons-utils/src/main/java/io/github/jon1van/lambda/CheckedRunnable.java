package io.github.jon1van.lambda;

/// A CheckedRunnable is similar to a [Runnable] EXCEPT it throws a checked exception.
///
/// Unfortunately, CheckedRunnables can obfuscate code because they require using try-catch blocks.
/// This class and the convenience functions in [Uncheck], allow you to improve the readability
/// of some code (assuming you are willing to demote all checked exceptions to RuntimeExceptions)
///
/// For example:
/// ```java
/// //code WITHOUT these utilities -- is harder to read and write.
/// try {
///     myThrowingFunction();
/// } catch (AnnoyingCheckedException ex) {
///     throw DemotedException.demote(ex);
/// }})
///
/// //code WITH these utilities -- is easier to read and write.
/// Uncheck.run(() -> myThrowingFunction());
/// ```
@FunctionalInterface
public interface CheckedRunnable {

    void run() throws Exception;
}
