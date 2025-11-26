package io.github.jon1van.uncheck;

/// A CheckedConsumer is similar to a [Consumer] EXCEPT it throws a checked exception.
///
/// Unfortunately, a CheckedConsumer obfuscates stream processing code because they require using
/// try-catch blocks. This class and the convenience functions in [Uncheck], allow you to
/// improve the readability of stream processing pipelines (assuming you are willing to demote all
/// checked exceptions to RuntimeExceptions)
///
/// For example:
/// ```java
/// //code WITHOUT these utilities -- is harder to read and write
/// List<String> dataSet = loadData();
/// List<String> subset = dataSet.stream()
///     .filter(str -> str.contains("abcde"))
///     .forEach(str -> {
///         try {
///             return consumerThatThrowsCheckedEx(str);
///         } catch (AnnoyingCheckedException ex) {
///             throw DemotedException.demote(ex);
///         }});
///
/// //code WITH these utilities -- is easier to read and write
/// List<String> dataSet = loadData();
/// List<String> subset = dataSet.stream()
///     .filter(str -> str.contains("abcde"))
///     .forEach(Uncheck.cons(str -> consumerThatThrowsCheckedEx(str));
/// ```
///
@FunctionalInterface
public interface CheckedConsumer<T> {

    void accept(T t) throws Exception;
}
