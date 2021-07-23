/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

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
        String output = PostgresCopyEscapeUtils.builder().escape(input.getParam()).toString();
        assertEquals(expected, output);
    }

    private static Stream<Arguments> escape() {
        return Stream.of(new String[][] {
                {"test", "test"},
                {"te\\st","te\\\\st",},
                {"t\nst", "t\\nst",},
                {"\\t\nst", "\\\\t\\nst"},
                {"t\nst\n", "t\\nst\\n"},
                {"\ttest\\\n", "\\ttest\\\\\\n"},
        }).map(arguments -> Arguments.of(new EscapedForDisplayParam(arguments[0]), arguments[1]));
    }
}