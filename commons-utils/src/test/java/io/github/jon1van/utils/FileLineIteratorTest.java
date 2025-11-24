package io.github.jon1van.utils;

import static io.github.jon1van.utils.FileUtils.getResourceFile;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

public class FileLineIteratorTest {

    @Test
    public void canIterateOverFileContents() {
        File testFile = getResourceFile("textFile.txt");
        FileLineIterator iter = new FileLineIterator(testFile);

        assertThat(iter.next()).isEqualTo("line 1");
        assertThat(iter.next()).isEqualTo("line 2");
        assertThat(iter.hasNext()).isFalse();
    }

    private FileLineIterator makeIterator() {
        File testFile = getResourceFile("textFile.txt");
        return new FileLineIterator(testFile);
    }

    @Test
    public void iteratorHasNextBecomesFalse() {

        FileLineIterator iter = makeIterator();
        assertThat(iter.hasNext()).isTrue();
        iter.next();
        assertThat(iter.hasNext()).isTrue();
        iter.next();
        assertThat(iter.hasNext()).isFalse();
    }

    @Test
    public void tooManyNextCallGeneratesException() {
        FileLineIterator iter = makeIterator();
        iter.next();
        iter.next();
        assertThrows(NoSuchElementException.class, () -> iter.next());
    }

    @Test
    public void iteratorDoesNotSupportRemove() {

        FileLineIterator iter = makeIterator();
        iter.next();

        assertThrows(UnsupportedOperationException.class, () -> iter.remove());
    }

    @Test
    public void canCloseIterator() {
        FileLineIterator iter = makeIterator();
        iter.next();

        assertDoesNotThrow(() -> iter.close());
    }

    @Test
    public void closedIteratorsGiveNoData() throws IOException {
        FileLineIterator iter = makeIterator();
        iter.next();
        iter.close();
        assertThat(iter.hasNext()).isFalse();

        assertThrows(NoSuchElementException.class, () -> iter.next());
    }

    @Test
    public void handleEmptyFilesCorrectly() {

        FileLineIterator iter = new FileLineIterator(getResourceFile("emptyTextFile.txt"));

        assertThat(iter.hasNext()).isFalse();
    }

    @Test
    public void canHandleMissingFilesGracefully() {
        assertThrows(
                IllegalArgumentException.class, () -> new FileLineIterator(getResourceFile("missingTextFile.txt")));
    }

    @Test
    public void canOpenAndReadGzFile() {
        File testFile = getResourceFile("twoLinesHelloGoodbye.txt.gz");
        FileLineIterator iter = new FileLineIterator(testFile);

        assertThat(iter.next()).isEqualTo("Hello");
        assertThat(iter.next()).isEqualTo("Goodbye");
        assertThat(iter.hasNext()).isFalse();
    }
}
