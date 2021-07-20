package nl.b3p.brmo.sql;

import nl.b3p.brmo.sql.dialect.PostGISDialect;
import nl.b3p.brmo.sql.dialect.SQLDialect;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.translate.CharSequenceTranslator;
import org.locationtech.jts.geom.Geometry;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyIn;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;

/**
 * The PostgreSQL JDBC driver does not support parallel copy operations, even with multiple connections. So this class
 * caches the copy stream in memory so only one copy operation is active at a time.
 */
public class PostGISCopyInsertBatch implements QueryBatch {
    protected Connection connection;
    protected String sql;
    protected PostGISDialect dialect;
    protected int batchSize;
    protected boolean linearizeCurves;

    protected int count = 0;
    protected ByteArrayOutputStream copyData = new ByteArrayOutputStream();

    public PostGISCopyInsertBatch(Connection connection, String sql, int batchSize, SQLDialect dialect, boolean linearizeCurves) {
        if (!(dialect instanceof PostGISDialect)) {
            throw new IllegalArgumentException();
        }
        this.connection = connection;
        this.sql = sql;
        this.dialect = (PostGISDialect)dialect;
        this.batchSize = batchSize;
        this.linearizeCurves = linearizeCurves;
    }

    @Override
    public boolean addBatch(Object[] params) throws Exception {
        StringBuilder s = new StringBuilder();
        for(int i = 0; i < params.length; i++) {
            if (i != 0) {
                s.append("\t");
            }
            Object param = params[i];
            String value;
            if (param == null) {
                s.append("\\N");
            } else if (param instanceof Geometry) {
                Geometry geometry = (Geometry) param;
                s.append(dialect.getEWkt(geometry, linearizeCurves));
            } else if (param instanceof Boolean) {
                s.append((Boolean)param ? "t" : "f");
            } else {
                // FIXME any more types need special conversion?
                quote(param.toString(), s);
            }
        }
        s.append("\n");
        // XXX PostgreSQL does not support 0 bytes in UTF8 string
        byte[] bytes = s.toString().getBytes(StandardCharsets.UTF_8);
        copyData.writeBytes(bytes);

        count++;
        if (count == batchSize) {
            this.executeBatch();
            return true;
        }
        return false;
    }

    private static void quote(String str, StringBuilder sb) {
        if (str.length() == 0) {
            return;
        }
        char[] s = str.toCharArray();
        int start = 0;
        int end = 0;
        int length = s.length;
        boolean haveEscaped = false;
        int sbStart = sb.length();
        do {
            char c;
            boolean escape;
            do {
                c = s[end++];
                escape = c == '\\' || c == '\n' || c == '\t';
            } while (end < length && !escape);
            if (escape) {
                haveEscaped = true;
                if(end > start) {
                    sb.append(s, start, end - start - 1);
                }
                if (c == '\\') {
                    sb.append("\\\\");
                } else if (c == '\t') {
                    sb.append("\\t");
                } else {
                    // escape newline
                    sb.append("\\n");
                }
            } else {
                sb.append(s, start, end-start);
            }
            start = end;
        } while(end < length);
        if (haveEscaped) {
            System.out.println("Escaped: " + StringEscapeUtils.escapeJava(str) + " -> " + sb.substring(sbStart));
        }
    }

    public static void main(String... args) {
        String[] t = {
                "test",
                "aap",
                "ggaa\\aap",
                "\\eerst",
                "laatst\\",
                "meer\\\nder\\eere\\",
                "\nnieuw\tline\\",
                "aanheteind\n"
        };
        for(String s: t) {
            test2(s);
        }
    }

    private static void test(String s) {
        StringBuilder sb = new StringBuilder();
        System.out.print(StringEscapeUtils.escapeJava(s) + " -> ");
        quote(s, sb);
        System.out.println(sb);
    }

    private static void test2(String s) {
        System.out.print(StringEscapeUtils.escapeJava(s) + " -> ");
        StringEscapeUtils.Builder sb = StringEscapeUtils.builder(new CharSequenceTranslator() {
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
        });
        sb.escape(s);
        System.out.println(sb);
    }

    @Override
    public void executeBatch() throws Exception {
        if (count > 0) {
            PGConnection pgConnection = connection.unwrap(PGConnection.class);
            CopyIn copyIn = pgConnection.getCopyAPI().copyIn(sql);
            copyIn.writeToCopy(copyData.toByteArray(), 0, copyData.size());
            copyIn.endCopy();
            count = 0;
            copyData = new ByteArrayOutputStream();
        }
    }

    @Override
    public void close() {
    }
}
