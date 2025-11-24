package io.github.jon1van.utils;

import static io.github.jon1van.utils.DemotedException.demote;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileNotFoundException;

import org.junit.jupiter.api.Test;

public class DemotedExceptionTest {

    @Test
    public void canAccessCause() {
        FileNotFoundException fnfe1 = new FileNotFoundException();
        DemotedException de = new DemotedException(fnfe1);
        assertThat(de.getCause()).isEqualTo(fnfe1);

        FileNotFoundException fnfe2 = new FileNotFoundException();
        DemotedException de2 = new DemotedException("message", fnfe2);
        assertThat(de2.getMessage()).isEqualTo("message");
        assertThat(de2.getCause()).isEqualTo(fnfe2);
    }

    @Test
    public void cannotMakeDemotedExceptionFromRuntimeException() {
        assertThrows(IllegalArgumentException.class, () -> new DemotedException(new ArrayIndexOutOfBoundsException()));
    }

    @Test
    public void cannotMakeDemotedExceptionFromRuntimeException_2() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new DemotedException("message", new ArrayIndexOutOfBoundsException()));
    }

    @Test
    public void cannotDemoteARuntimeException() {
        assertThrows(IllegalArgumentException.class, () -> demote(new ArrayIndexOutOfBoundsException()));
    }

    @Test
    public void cannotDemoteARuntimeException_2() {
        assertThrows(IllegalArgumentException.class, () -> demote("message", new ArrayIndexOutOfBoundsException()));
    }

    @Test
    public void whenARuntimeExceptionIsDemotedYouFailAndGetCause() {

        try {
            demote(new ArrayIndexOutOfBoundsException(5)); // This line SHOULD throw an IllegalArgumentException
            fail(); // FAIL if this statement is ever reached
        } catch (IllegalArgumentException iae) {
            assertThat(iae.getMessage())
                    .isEqualTo(
                            "Illegal Use of DemotedException, cannot demote RuntimeExceptions but ArrayIndexOutOfBoundsException is an instance of RuntimeException");
            assertThat(iae.getCause().getMessage()).isEqualTo("Array index out of range: 5");
        }
    }

    @Test
    public void canDemoteCheckedExceptions() {
        FileNotFoundException fnfe1 = new FileNotFoundException();
        DemotedException de = demote(fnfe1);
        assertThat(de.getCause()).isEqualTo(fnfe1);
    }

    @Test
    public void canDemoteCheckedExceptions_2() {
        FileNotFoundException fnfe1 = new FileNotFoundException();
        DemotedException de = demote("message", fnfe1);
        assertThat(de.getCause()).isEqualTo(fnfe1);
        assertThat(de.getMessage()).isEqualTo("message");
    }
}
