/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package nl.b3p.brmo.sql;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.translate.CharSequenceTranslator;

import java.io.IOException;
import java.io.Writer;

public class PostgresCopyEscapeUtils {
    /**
     * Escapes column values for use in the text-based PostgreSQL COPY format with the default column and row delimiters,
     * see https://www.postgresql.org/docs/current/sql-copy.html
     */
    public static final CharSequenceTranslator ESCAPE = new CharSequenceTranslator() {
        @Override
        public int translate(CharSequence charSequence, int start, Writer writer) throws IOException {
            for(int i = start; i < charSequence.length(); i++) {
                char c = charSequence.charAt(i);
                String subst = null;
                if (c == '\\') {
                    subst = "\\\\";
                } else if (c == '\t') {
                    subst = "\\t";
                } else if (c == '\n') {
                    subst = "\\n";
                }
                if (subst != null) {
                    if (i > start) {
                        writer.append(charSequence, start, i);
                    }
                    writer.append(subst);
                    return i - start + 1;
                }
            }
            writer.append(charSequence, start, charSequence.length());
            return charSequence.length() - start;
        }
    };

    public static StringEscapeUtils.Builder builder() {
        return StringEscapeUtils.builder(ESCAPE);
    }
}
