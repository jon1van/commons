package io.github.jon1van.utils;

import static io.github.jon1van.utils.Exceptions.stackTraceOf;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.Test;

public class ExceptionsTest {

    @Test
    public void stackTraceString_producesGoodResult() {

        IndexOutOfBoundsException someException = new IndexOutOfBoundsException("hello");

        // catch the result of `someException.printStackTrace()` so we
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        someException.printStackTrace(new PrintStream(os));
        String actualTrace = os.toString();

        assertThat(stackTraceOf(someException)).isEqualTo(actualTrace);
    }
}
