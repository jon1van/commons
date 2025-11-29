package org.mitre.caasd.commons.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

public class TranslatorTest {

    Translator<String, Double> toNumber = new Translator<String, Double>() {
        @Override
        public Double to(String string) {
            return Double.parseDouble(string);
        }

        @Override
        public String from(Double item) {
            return item.toString();
        }
    };

    Translator<Double, Double> plus12 = new Translator<Double, Double>() {
        @Override
        public Double to(Double num) {
            return num + 12.0;
        }

        @Override
        public Double from(Double num) {
            return num - 12.0;
        }
    };

    @Test
    public void canComposeTwoTranslators() {
        Translator<String, Double> composition = toNumber.compose(plus12);

        double forward = composition.to("13.0");
        assertThat(forward, closeTo(25.0, 0.000001));

        String back = composition.from(11.0);
        assertThat(back, is("-1.0"));
    }
}
