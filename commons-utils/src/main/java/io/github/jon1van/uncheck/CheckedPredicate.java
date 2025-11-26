package io.github.jon1van.uncheck;

/// A CheckedPredicate is similar to a [Predicate] EXCEPT it throws a checked exception.
///
/// Unfortunately, a CheckedPredicate obfuscates stream processing code because they require using
/// try-catch blocks. This class and the convenience functions in [Uncheck], allow you to
/// improve the readability of stream processing pipelines (assuming you are willing to demote all
/// checked exceptions to RuntimeExceptions)
///
/// For example:
/// ```java
/// //code WITHOUT these utilities -- is harder to read and write
/// List<String> dataSet = loadData();
/// List<String> subset = dataSet.stream()
///     .filter(str -> {
///         try {
///             return predicateThatThrowsCheckedEx(str);
///         } catch (AnnoyingCheckedException ex) {
///             throw DemotedException.demote(ex);
///         }})
///     .map(str -> str.toUpperCase())
///     .toList();
///
/// //code WITH these utilities -- is easier to read and write
/// List<String> dataSet = loadData();
/// List<String> subset = dataSet.stream()
///     .filter(Uncheck.pred(str -> predicateThatThrowsCheckedEx(str))
///     .map(str -> str.toUpperCase())
///     .toList();
/// ```
@FunctionalInterface
public interface CheckedPredicate<T> {

    boolean test(T t) throws Exception;
}
