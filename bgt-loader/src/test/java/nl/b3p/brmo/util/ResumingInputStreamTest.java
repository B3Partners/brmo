/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BrokenInputStream;
import org.apache.commons.io.input.ProxyInputStream;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class ResumingInputStreamTest {
    private InputStream toStream(String s) {
        return IOUtils.toInputStream(s, Charset.defaultCharset());
    }

    private String fromStream(InputStream is) throws IOException {
        return IOUtils.toString(is, Charset.defaultCharset());
    }

    @Test
    void normalStream() throws IOException {
        ResumingInputStream is = new ResumingInputStream((position, totalRetries, causeForRetry)
                -> toStream("test"));
        assertEquals("test", fromStream(is));
        assertEquals(4, is.getPosition());
        assertEquals(4, is.getCurrentDelegatePosition());
    }

    @Test
    void readSingleBytes() throws IOException {
        // Test the abstract read() method that must be overridden is not usually called in favor of reading
        // byte arrays, test it for coverage
        ResumingInputStream is = new ResumingInputStream((position, totalRetries, causeForRetry)
                -> toStream("test"));
        assertEquals('t', is.read());
        assertEquals('e', is.read());
        assertEquals('s', is.read());
        assertEquals('t', is.read());
        assertEquals(-1, is.read());
    }

    @Test
    void delegateCloseCalled() throws IOException {
        MutableBoolean closed = new MutableBoolean(false);
        ResumingInputStream is = new ResumingInputStream((position, totalRetries, causeForRetry) -> new ProxyInputStream(toStream("test")) {
            @Override
            public void close() throws IOException {
                super.close();
                closed.setTrue();
            }
        });
        fromStream(is);
        is.close();
        assertEquals(true, closed.getValue());
    }

    @Test
    void resumeOnceAtStart() throws IOException {
        AtomicBoolean first = new AtomicBoolean(true);
        ResumingInputStream is = new ResumingInputStream((position, totalRetries, causeForRetry) -> {
            if (first.get()) {
                first.set(false);
                return new BrokenInputStream();
            } else {
                return toStream("test");
            }
        });
        assertEquals("test", fromStream(is));
        assertEquals(1, is.getTotalRetries());
    }

    @Test
    void resumeAfterOneByte() throws IOException {
        AtomicBoolean first = new AtomicBoolean(true);
        ResumingInputStream is = new ResumingInputStream((position, totalRetries, causeForRetry) -> {
            if (first.get()) {
                first.set(false);
                return new InputStream() {
                    boolean firstRead = true;
                    @Override
                    public int read() throws IOException {
                        if (firstRead) {
                            firstRead = false;
                            return 't';
                        }
                        throw new IOException("test");
                    }
                };
            } else {
                assertEquals(1, position);
                assertEquals(1, totalRetries);
                return toStream("est");
            }
        });
        assertEquals("test", fromStream(is));
        assertEquals(1, is.getTotalRetries());
    }
    @Test
    void testMaxTriesExceeded() throws IOException {
        ResumingInputStream is = new ResumingInputStream(
                (position, totalRetries, causeForRetry) -> new BrokenInputStream()
        );
        is.setMaxReadTries(3);
        assertEquals(3, is.getMaxReadTries());
        assertThrows(IOException.class, () -> fromStream(is));
        assertEquals(3, is.getTotalRetries());
    }

    @Test
    void delegateCloseExceptionIgnored() throws IOException {
        AtomicBoolean first = new AtomicBoolean(true);
        MutableBoolean closed = new MutableBoolean(false);
        ResumingInputStream is = new ResumingInputStream((position, totalRetries, causeForRetry) -> {
            if (first.get()) {
                first.set(false);
                return new ProxyInputStream(new BrokenInputStream()) {
                    @Override
                    public void close() throws IOException {
                        closed.setTrue();
                        throw new IOException("test");
                    }
                };
            } else {
                return toStream("test");
            }
        });
        assertEquals("test", fromStream(is));
        assertEquals(1, is.getTotalRetries());
        assertEquals(true, closed.getValue());
    }
}