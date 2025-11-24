package io.github.jon1van.lambda;

/// A CheckedFunction is similar to a [Function] EXCEPT it throws a checked exception.
///
/// Unfortunately, CheckedFunctions obfuscate stream processing code because they require using
/// try-catch blocks. This class and the convenience functions in [Uncheck], allow you to
/// improve the readability of stream processing pipelines (assuming you are willing to demote all
/// checked exceptions to RuntimeExceptions)
///
/// For example:
/// ```java
/// //code WITHOUT these utilities -- is harder to read and write.
/// List<String> dataSet = loadData();
/// List<String> subset = dataSet.stream()
///     .map(str -> {
///         try {
///             return functionThatThrowsCheckedEx(str);
///         } catch (AnnoyingCheckedException ex) {
///             throw DemotedException.demote(ex);
///         }})
///     .filter(str -> str.length() < 5)
///     .toList();
///
/// //code WITH these utilities -- is easier to read and write.
/// List<String> dataSet = loadData();
/// List<String> subset = dataSet.stream()
///     .map(Uncheck.func(str -> functionThatThrowsCheckedEx(str))
///     .filter(str -> str.length() < 5)
///     .toList();
/// ```
@FunctionalInterface
public interface CheckedFunction<S, T> {

    T apply(S s) throws Exception;
}
