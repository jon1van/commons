package org.mitre.caasd.commons.testing;

import static java.lang.Math.abs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.Serializable;
import java.util.Random;

import org.mitre.caasd.commons.fileutil.FileUtils;

public class TestUtils {

    /**
     * Serializable an object to a File, then deserialize that file and return the results. This
     * sequence (1) confirms that the incoming object can indeed be serialized, and (2) gives the
     * caller an opportunity to inspect the result of the serialization/deserialization operation
     * chain.
     * <p>
     * This method also ensure the intermediate file created during serialization is deleted (even
     * if serialization fails). The user will not need, or even be able to, clean up that artifact.
     *
     * @param object A Serializable object.
     *
     * @return The results of serializing and then deserializing the input object.
     */
    public static Serializable serializeAndDeserialize(Serializable object) {

        String fileName = createRandomFileName();
        File targetFile = new File(fileName);

        confirmSerializability(object, targetFile);

        Serializable result = (Serializable) FileUtils.deserialize(targetFile);
        targetFile.delete();
        return result;
    }

    private static String createRandomFileName() {
        int randomNumber = abs((new Random()).nextInt());
        return randomNumber + "-serializableTest.ser";
    }

    /**
     * Run a unit test that confirms a Serializable object can be Serialized to a File. This
     * sequence (1) confirms that the incoming object can indeed be serialized, and (2) leaves the
     * targetFile containing the serialized form of the input object.
     * <p>
     * This method will delete the targetFile if serialization fails. This frees the caller from
     * needing to clean up output.
     *
     * @param object     A Serializable object.
     * @param targetFile The file where the input object will be serialized to. Note: this file
     *                   cannot exist yet.
     */
    public static void confirmSerializability(Serializable object, File targetFile) {

        assertThat(targetFile.exists(), is(false));

        try {
            FileUtils.serialize(object, targetFile);

            assertThat(targetFile.exists(), is(true));
        } catch (Exception ex) {
            // in the event of a serialization error delete the target file and return the exception.
            targetFile.delete();
            throw ex;
        }
    }
}
