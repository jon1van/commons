package io.github.jon1van.utils;

import static io.github.jon1van.utils.ErrorCatchingTask.ignoreAndRethrow;
import static io.github.jon1van.utils.ErrorCatchingTask.killJvmOnError;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.jon1van.utils.ErrorCatchingTask.ErrorHandlingPolicy;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ErrorCatchingTaskTest {

    static class ExceptionCatcher implements ExceptionHandler {

        boolean caughtSomething = false;

        Throwable caught = null;

        @Override
        public void handle(Exception ex) {
            caughtSomething = true;
            caught = ex;
        }
    }

    static class ErrorCatcher implements ErrorHandlingPolicy {

        boolean gotError = false;

        @Override
        public void handleError(Error error) {
            gotError = true;
        }
    }

    @Test
    void exceptionsAreCaught() {

        ExceptionCatcher exceptionHandler = new ExceptionCatcher();

        ErrorCatchingTask ect = new ErrorCatchingTask(() -> throwIndexOutOfBounds(), exceptionHandler);
        ect.run();

        assertThat(exceptionHandler.caughtSomething).isTrue();
        assertThat(exceptionHandler.caught).isInstanceOf(ArrayIndexOutOfBoundsException.class);
    }

    @Test
    public void errorsAreRethrown() {

        ErrorCatchingTask protectedTask =
                new ErrorCatchingTask(() -> causeStackOverflowError(), new ExceptionCatcher(), new ErrorCatcher());

        assertThrows(StackOverflowError.class, () -> protectedTask.run());
    }

    @Test
    public void errorsArePassedToErrorHandlingPolicy() {

        ErrorCatcher errorHandler = new ErrorCatcher();

        ErrorCatchingTask protectedTask =
                new ErrorCatchingTask(() -> causeStackOverflowError(), new ExceptionCatcher(), errorHandler);

        assertThat(errorHandler.gotError).isFalse();
        assertThrows(Error.class, () -> protectedTask.run());
        assertThat(errorHandler.gotError).isTrue();
    }

    @Test
    public void ignoreAndRethrowPolicyIsAvailable() {
        ErrorHandlingPolicy policy = ignoreAndRethrow();

        assertDoesNotThrow(() -> policy.handleError(new StackOverflowError()));
    }

    @Disabled // because killing the JVM can't be part of your standard test workflow
    @Test
    public void killJvmPolicyIsAvailable() {
        ErrorHandlingPolicy policy = killJvmOnError(17);

        assertDoesNotThrow(() -> policy.handleError(new StackOverflowError()));
    }

    public static void throwIndexOutOfBounds() {
        throw new ArrayIndexOutOfBoundsException();
    }

    public static void causeStackOverflowError() {
        causeStackOverflowError(); // intentional
    }
}
