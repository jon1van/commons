package io.github.jon1van.utils;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;

public class SequentialFileWriterTest {

    @Test
    public void testCreateWarningFileWithException() {
        /*
         * Ensure that the warning file is made correctly. Specifically, confirm that (1) a new
         * directory can be made if necessary and (2) that the warning file itself is written to the
         * target directory.
         */

        String directory = "testDir2";
        String message = "simpleMessage";
        Exception ex = new RuntimeException("hello");

        SequentialFileWriter writer = new SequentialFileWriter(directory);
        writer.handle(message, ex);

        File wasThisfileMade = new File(directory + File.separator + "error_0.txt");

        assertTrue(wasThisfileMade.exists());

        wasThisfileMade.delete();
        File dirAsFile = new File(directory);
        dirAsFile.delete();
    }

    @Test
    public void willNotFailOnNullMessage() {
        /*
         * Ensure that the warning file is made correctly even though a null message is
         * provided.Confirm that (1) a new directory can be made if necessary and (2) that the
         * warning file itself is written to the target directory.
         */

        String directory = "testDir3";
        String nullMessage = null;
        Exception ex = new RuntimeException("hello");

        SequentialFileWriter writer = new SequentialFileWriter(directory);
        writer.handle(nullMessage, ex);

        File wasThisfileMade = new File(directory + File.separator + "error_0.txt");

        assertTrue(wasThisfileMade.exists());

        wasThisfileMade.delete();
        File dirAsFile = new File(directory);
        dirAsFile.delete();
    }
}
