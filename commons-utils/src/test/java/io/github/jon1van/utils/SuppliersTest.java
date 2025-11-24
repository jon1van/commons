package io.github.jon1van.utils;

import static io.github.jon1van.utils.Suppliers.environmentVarSupplier;
import static io.github.jon1van.utils.Suppliers.systemPropertySupplier;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.util.UUID;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SuppliersTest {

    @TempDir
    public File tempDir;

    @Test
    public void fileBasedSupplier_canReachFile() throws Exception {

        File tempFile = new File(tempDir, "putMyPropsInHere.cfg");
        String propertyValue = "iAmAValue";
        FileUtils.appendToFile(tempFile, "thisIsAPropertyKey=   " + propertyValue + "  ");

        Supplier<String> fbs = Suppliers.fileBasedSupplier(tempFile, "thisIsAPropertyKey");

        assertThat(fbs.get()).isEqualTo(propertyValue);
    }

    @Test
    public void fileBasedSupplier_butNoFile() {

        String randomFileName = UUID.randomUUID() + ".txt";

        Supplier<String> fbs = Suppliers.fileBasedSupplier(new File(randomFileName), "thisIsAPropertyKey");

        // this IllegalArgumentException is thrown because the "File parser" notices the File does not exist
        assertThrows(IllegalArgumentException.class, () -> fbs.get());
    }

    @Test
    public void fileBasedSupplier_canBeBuildWithNullFile() {
        // we don't want this to fail AT CONSTRUCTION...it can fail during use though
        assertDoesNotThrow(() -> Suppliers.fileBasedSupplier(null, "propertyKey"));
    }

    @Test
    public void fileBasedSupplier_builtWithNullFileFails() {

        Supplier<String> fbs = Suppliers.fileBasedSupplier(null, "propertyKey");

        assertThrows(NullPointerException.class, () -> fbs.get());
    }

    @Test
    public void canPullFromSystemProps_happyPath() {

        String key = UUID.randomUUID().toString();
        String value = UUID.randomUUID().toString();
        System.setProperty(key, value);

        Supplier<String> sysPropSup = systemPropertySupplier(key);

        assertThat(sysPropSup.get()).isEqualTo(value);
    }

    @Test
    public void canPullFromSystemProps_noValueComesBackNull() {

        // make a random key -- it obviously won't be in the system properties...so it should come back null
        String key = UUID.randomUUID().toString();
        Supplier<String> sysPropSup = systemPropertySupplier(key);
        assertThat(sysPropSup.get()).isNull();
    }

    @Test
    public void canPullFromEnvironmentVars_noValueComesBackNull() {

        // make a random key -- it obviously won't be in the system properties...so it should come back null
        String key = UUID.randomUUID().toString();
        Supplier<String> environmentVarSup = environmentVarSupplier(key);
        assertThat(environmentVarSup.get()).isNull();
    }
}
