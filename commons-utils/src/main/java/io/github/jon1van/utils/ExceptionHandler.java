package io.github.jon1van.utils;

/// This interface is designed to make "exception handling behavior" a pluggable feature.
///
/// For example, one ExceptionHandler may write error stack traces to disk, while another
/// ExceptionHandler could print to System.err, while yet another ExceptionHandler sends the errors
/// to an external database. The point is: how Exceptions are handled should be an easily changed
/// feature. This interface helps make that flexibility a reality.
public interface ExceptionHandler {

    void handle(Exception ex);
}
