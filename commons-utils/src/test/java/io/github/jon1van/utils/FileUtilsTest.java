package io.github.jon1van.utils;

import static io.github.jon1van.utils.FileUtils.deserialize;
import static io.github.jon1van.utils.FileUtils.getResourceFile;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.jupiter.api.Test;

public class FileUtilsTest {

    private static final String USER = System.getProperty("user.dir");

    private static final String GZIPPED_FILE = USER + "/src/test/resources/oneTrack.txt.gz";
    private static final String UNZIPPED_FILE = USER + "/src/test/resources/oneTrack.txt";

    @Test
    public void testIsGZipFile() throws Exception {
        assertTrue(FileUtils.isGZipFile(new File(GZIPPED_FILE)));
        assertFalse(FileUtils.isGZipFile(new File(UNZIPPED_FILE)));
    }

    @Test
    public void testWriteToNewGzFile() throws Exception {

        String fileName = "doesThisFileGetWrittenProperly.gz";
        String fileContents = "this text goes inside the file";

        assertThat(new File(fileName).exists()).isFalse();

        FileUtils.writeToNewGzFile(fileName, fileContents);

        assertThat(new File(fileName).exists()).isTrue();

        BufferedReader reader = FileUtils.createReaderFor(new File(fileName));

        assertThat(reader.readLine()).isEqualTo(fileContents);
        assertThat(reader.readLine()).isNull();

        reader.close();

        new File(fileName).delete(); // clean up
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDeserializeViaInputStream() {
        // This file can be found in the resource folder
        String TEST_FILE = "serializedArrayListOfTenDoubles.ser";
        InputStream stream = getClass().getResourceAsStream(TEST_FILE);

        Object obj = deserialize(stream);
        ArrayList<Double> list = (ArrayList<Double>) obj;

        assertEquals(10, list.size());

        double TOL = 0.000001;
        for (int i = 0; i < list.size(); i++) {
            assertEquals(i * 1.0, list.get(i), TOL);
        }
    }

    @Test
    public void testDeserializeFile_missingFile() {

        File file = new File("ThisFileDoesnotExist.txt");

        // should throw an IllegalArgumentException because the file didn't exist
        assertThrows(IllegalArgumentException.class, () -> deserialize(file));
    }

    @Test
    public void testGetResourceFile() throws Exception {
        // we cannot just use: "findMe.txt" because this method isn't based off a particular class
        File file = getResourceFile("findMe.txt");

        List<String> lines = Files.readAllLines(file.toPath());
        assertEquals(1, lines.size());
        assertEquals("hello", lines.get(0));
    }

    @Test
    public void testGetResourceFile_missingResource() {
        // "Should fail because the request resource doesnt exist
        assertThrows(IllegalArgumentException.class, () -> getResourceFile("thisFileDoesntExist.missing"));
    }

    @Test
    public void getPropertiesRejectsFilesWithDuplicateKeys() {

        File rejectFile = getResourceFile("rejectThisPropertiesFile.props");

        assertTrue(rejectFile.exists());

        assertThrows(IllegalArgumentException.class, () -> FileUtils.getProperties(rejectFile));
    }

    @Test
    public void getPropertiesWorksWhenDuplicateKeysAreInComments() throws Exception {

        File acceptFile = getResourceFile("acceptThisPropertiesFile.props");

        assertTrue(acceptFile.exists());

        Properties props = FileUtils.getProperties(acceptFile);

        assertThat(props.getProperty("key1")).isEqualTo("goodValue");
    }
}
