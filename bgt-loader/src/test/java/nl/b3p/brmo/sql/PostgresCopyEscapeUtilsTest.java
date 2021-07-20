package nl.b3p.brmo.sql;

import org.apache.commons.text.StringEscapeUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PostgresCopyEscapeUtilsTest {
    /**
     * This class is used as a parametrized test parameter and displays the value escaped for Java so non-whitespace
     * characters are legible.
     */
    public static class EscapedForDisplayParam {
        String param;

        EscapedForDisplayParam(String param) {
            this.param = param;
        }

        public String getParam() {
            return param;
        }

        public String toString() {
            return StringEscapeUtils.escapeJava(param);
        }
    }

    @ParameterizedTest(name="[{index}] {0} == {1}")
    @MethodSource
    public void escape(EscapedForDisplayParam input, String expected) {
        String output = PostgresCopyEscapeUtils.ESCAPE.translate(input.getParam());
        assertEquals(expected, output);
    }

    private static Stream<Arguments> escape() {
        return Stream.of(
                Arguments.of(new EscapedForDisplayParam("test"), "test"),
                Arguments.of(new EscapedForDisplayParam("te\\st"),"te\\\\st"),
                Arguments.of(new EscapedForDisplayParam("t\nst"), "t\\nst"),
                Arguments.of(new EscapedForDisplayParam("\\t\nst"), "\\\\t\\nst"),
                Arguments.of(new EscapedForDisplayParam("t\nst\n"), "t\\nst\\n"),
                Arguments.of(new EscapedForDisplayParam("\ttest\\\n"), "\\ttest\\\\\\n")
        );
    }
}