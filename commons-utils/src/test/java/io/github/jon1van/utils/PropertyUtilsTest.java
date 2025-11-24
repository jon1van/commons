package io.github.jon1van.utils;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static io.github.jon1van.utils.PropertyUtils.*;
import static java.lang.reflect.Modifier.isPublic;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

public class PropertyUtilsTest {

    @Test
    public void testParseProperties() {

        String allLines =
                """
                #shouldIgnoreThisComment = 5
                variable1 : 12
                what = valueForWhat
                var : 18
                """;

        Properties props = parseProperties(allLines);

        assertThat(props.getProperty("variable1")).isEqualTo("12");
        assertThat(props.getProperty("what")).isEqualTo("valueForWhat");
        assertThat(props.getProperty("var")).isEqualTo("18");
        assertThat(props.size()).isEqualTo(3); // "The comment should have been ignored"
    }

    @Test
    public void canBuildMissingPropertyExceptions() {
        MissingPropertyException mpe = new MissingPropertyException("PROP_NAME");
        assertThat(mpe.getMessage()).isEqualTo("The property PROP_NAME is missing");
    }

    @Test
    public void missingPropertyExceptionConstructorIsPublic() {
        /* Confirm a constructor is public so external packages can use this Exception type. */
        var constructorArray = MissingPropertyException.class.getConstructors();

        var constructorWithOneStringParam = Stream.of(constructorArray)
                .filter(c -> c.getParameterCount() == 1)
                .filter(c -> c.getParameterTypes()[0] == String.class)
                .findFirst()
                .get();

        assertThat(isPublic(constructorWithOneStringParam.getModifiers())).isTrue();
    }

    @Test
    public void getRequiredProperty_trimsWhiteSpace() {
        Properties props = new Properties();
        props.setProperty("key", " value ");

        assertThat(getString("key", props)).isEqualTo("value");
    }

    @Test
    public void getRequiredProperty_throwsMissingPropertyException() {
        assertThrows(MissingPropertyException.class, () -> getString("missingKey", new Properties()));
    }

    @Test
    public void getOptionalProperty_trimsWhiteSpace() {

        Properties props = new Properties();
        props.setProperty("key", " value ");

        assertThat(getOptionalString("key", props).get()).isEqualTo("value");
    }

    @Test
    public void getOptionalProperty_returnsEmptyOptional() {
        Optional<String> result = getOptionalString("missingKey", new Properties());

        assertThat(result.isPresent()).isFalse();
    }

    @Test
    public void getOptionalProperty_defaultValueIsReturnedWhenKeyIsMissing() {
        String result = getOptionalString("missingKey", new Properties(), "defaultValue");
        assertThat(result).isEqualTo("defaultValue");
    }

    @Test
    public void getOptionalProperty_withDefault_trimsWhiteSpace() {

        Properties props = new Properties();
        props.setProperty("key", "   value   ");

        assertThat(getOptionalString("key", props, "someDefaultValue")).isEqualTo("value");
    }

    @Test
    public void getRequiredByte_throwsMissingPropertyException() {
        assertThrows(MissingPropertyException.class, () -> getByte("missingKey", new Properties()));
    }

    @Test
    public void getRequiredByte_parsesByte() {
        Properties props = new Properties();
        props.setProperty("key", "   22   ");
        assertThat(getByte("key", props)).isEqualTo((byte) 22);
    }

    @Test
    public void getRequiredShort_throwsMissingPropertyException() {
        assertThrows(MissingPropertyException.class, () -> getShort("missingKey", new Properties()));
    }

    @Test
    public void getRequiredShort_parsesShort() {
        Properties props = new Properties();
        props.setProperty("key", "   22   ");
        assertThat(getShort("key", props)).isEqualTo((short) 22);
    }

    @Test
    public void getRequiredInt_throwsMissingPropertyException() {
        assertThrows(MissingPropertyException.class, () -> getInt("missingKey", new Properties()));
    }

    @Test
    public void getRequiredInt_parsesInt() {
        Properties props = new Properties();
        props.setProperty("key", "   22   ");
        assertThat(getInt("key", props)).isEqualTo(22);
    }

    @Test
    public void getRequiredLong_throwsMissingPropertyException() {
        assertThrows(MissingPropertyException.class, () -> getLong("missingKey", new Properties()));
    }

    @Test
    public void getRequiredLong_parsesLong() {
        Properties props = new Properties();
        props.setProperty("key", "   22   ");
        assertThat(getLong("key", props)).isEqualTo(22L);
    }

    @Test
    public void getRequiredFloat_throwsMissingPropertyException() {
        assertThrows(MissingPropertyException.class, () -> getFloat("missingKey", new Properties()));
    }

    @Test
    public void getRequiredFloat_parsesFloat() {
        Properties props = new Properties();
        props.setProperty("key", "   22.123   ");
        assertThat(getFloat("key", props)).isEqualTo(22.123f);
    }

    @Test
    public void getRequiredDouble_throwsMissingPropertyException() {
        assertThrows(MissingPropertyException.class, () -> getDouble("missingKey", new Properties()));
    }

    @Test
    public void getRequiredDouble_parsesDouble() {
        Properties props = new Properties();
        props.setProperty("key", "   22.123   ");
        assertThat(getDouble("key", props)).isEqualTo(22.123);
    }

    @Test
    public void getRequiredBoolean_throwsMissingPropertyException() {
        assertThrows(MissingPropertyException.class, () -> getBoolean("missingKey", new Properties()));
    }

    @Test
    public void getRequiredBoolean_parsesBoolean() {
        Properties props = new Properties();
        props.setProperty("key", "   false   ");
        assertThat(getBoolean("key", props)).isEqualTo(false);
    }

    @Test
    public void getOptionalByte_withDefault() {

        Properties props = new Properties();
        props.setProperty("key", "   22   ");

        byte retrievedValue = getOptionalByte("key", props, (byte) 33);
        assertThat(retrievedValue).isEqualTo((byte) 22);

        byte defaultValue = getOptionalByte("missingKey", props, (byte) 52);
        assertThat(defaultValue).isEqualTo((byte) 52);
    }

    @Test
    public void getOptionalShort_withDefault() {

        Properties props = new Properties();
        props.setProperty("key", "   22   ");

        short retrievedValue = getOptionalShort("key", props, (short) 33);
        assertThat(retrievedValue).isEqualTo((short) 22);

        short defaultValue = getOptionalShort("missingKey", props, (short) 52);
        assertThat(defaultValue).isEqualTo((short) 52);
    }

    @Test
    public void getOptionalInt_withDefault() {

        Properties props = new Properties();
        props.setProperty("key", "   22   ");

        int retrievedValue = getOptionalInt("key", props, 33);
        assertThat(retrievedValue).isEqualTo(22);

        int defaultValue = getOptionalInt("missingKey", props, 52);
        assertThat(defaultValue).isEqualTo(52);
    }

    @Test
    public void getOptionalLong_withDefault() {

        Properties props = new Properties();
        props.setProperty("key", "   22   ");

        long retrievedValue = getOptionalLong("key", props, 33);
        assertThat(retrievedValue).isEqualTo(22L);

        long defaultValue = getOptionalLong("missingKey", props, 52);
        assertThat(defaultValue).isEqualTo(52L);
    }

    @Test
    public void getOptionalFloat_withDefault() {

        Properties props = new Properties();
        props.setProperty("key", "   22.123   ");

        float retrievedValue = getOptionalFloat("key", props, 33);
        assertThat(retrievedValue).isEqualTo(22.123f);

        float defaultValue = getOptionalFloat("missingKey", props, 52);
        assertThat(defaultValue).isEqualTo(52f);
    }

    @Test
    public void getOptionalDouble_withDefault() {

        Properties props = new Properties();
        props.setProperty("key", "   22.123   ");

        double retrievedValue = getOptionalDouble("key", props, 33);
        assertThat(retrievedValue).isEqualTo(22.123);

        double defaultValue = getOptionalDouble("missingKey", props, 52);
        assertThat(defaultValue).isEqualTo(52.0);
    }

    @Test
    public void getOptionalBoolean_withDefault() {

        Properties props = new Properties();
        props.setProperty("key", "   false   ");

        boolean retrievedValue = getOptionalBoolean("key", props, true);
        assertThat(retrievedValue).isEqualTo(false);

        boolean defaultValue = getOptionalBoolean("missingKey", props, true);
        assertThat(defaultValue).isEqualTo(true);
    }

    @Test
    public void tokenizeAndValidate_acceptsEmptyString() {
        List<String> tokens = tokenizeAndValidate("", newArrayList("OPTION_1", "OPTION_2"));

        assertThat(tokens.isEmpty()).isTrue();
    }

    @Test
    public void tokenizeAndValidate_rejectsNullString() {
        assertThrows(NullPointerException.class, () -> tokenizeAndValidate(null, newArrayList("OPTION_1", "OPTION_2")));
    }

    @Test
    public void tokenizeAndValidate_rejectsTokenNotInList() {
        // CORE FUNCTIONALITY -- REJECTS UNKNOWN TOKENS
        assertThrows(
                IllegalArgumentException.class,
                () -> tokenizeAndValidate("OPTION_3", newArrayList("OPTION_1", "OPTION_2")));
    }

    @Test
    public void tokenizeAndValidate_acceptsTokensFoundInList() {
        Set<String> validTokens = newHashSet("OPT_1", "OPT_2");
        List<String> tokens = tokenizeAndValidate("OPT_2, OPT_2, OPT_1", validTokens);

        assertThat(tokens.size()).isEqualTo(3);

        assertThat(tokens.get(0)).isEqualTo("OPT_2");
        assertThat(tokens.get(1)).isEqualTo("OPT_2");
        assertThat(tokens.get(2)).isEqualTo("OPT_1");
    }
}
