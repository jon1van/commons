package io.github.jon1van.utils;

import static java.util.Objects.isNull;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Exceptions {

    /// Captures and returns the result of `ex.printStackTrace()`
    ///
    /// @param ex Any Throwable
    ///
    /// @return The stack trace as a String
    public static String stackTraceOf(Throwable ex) {
        if (isNull(ex)) {
            return "Throwable is null: no stack trace available";
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        return sw.toString();
    }
}
