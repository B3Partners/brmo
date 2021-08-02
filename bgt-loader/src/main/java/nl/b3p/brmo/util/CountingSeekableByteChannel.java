/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;

/**
 * Analogous to a CountingInputStream, this wraps a channel and records the number of bytes read and written in addition
 * to the number of non-consecutive reads or writes.
 * <p>
 * Optionally the bytes read can be printed to System.out (requires commons-codec).
 *
 * @author Matthijs Laan
 */
public class CountingSeekableByteChannel implements SeekableByteChannel {
    private final SeekableByteChannel channel;
    private int nonConsecutiveIops = 0;
    private boolean seeked = false;
    private long bytesRead = 0;
    private long bytesWritten = 0;
    private boolean loggingEnabled = false;

    public CountingSeekableByteChannel(SeekableByteChannel channel) {
        this.channel = channel;
    }

    public int getNonConsecutiveIops() {
        return nonConsecutiveIops;
    }

    public long getBytesRead() {
        return bytesRead;
    }

    public long getBytesWritten() {
        return bytesWritten;
    }

    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    public void setLoggingEnabled(boolean loggingEnabled) {
        this.loggingEnabled = loggingEnabled;
    }

    public CountingSeekableByteChannel withLogging(boolean loggingEnabled) {
        this.loggingEnabled = loggingEnabled;
        return this;
    }

    @Override
    public int read(ByteBuffer byteBuffer) throws IOException {
        if (seeked) {
            seeked = false;
            nonConsecutiveIops++;
            if (loggingEnabled) {
                System.out.printf("new position %15d: read into buffer %s", channel.position(), byteBuffer);
            }
        } else if (loggingEnabled) {
            System.out.printf("consecutive  %15d: read into buffer %s", channel.position(), byteBuffer);
        }
        int startOffset = byteBuffer.arrayOffset();
        int read = channel.read(byteBuffer);
        if (loggingEnabled) {
            System.out.printf(" read %s, contents: %s\n", read, new String(org.apache.commons.codec.binary.Hex.encodeHex(byteBuffer.array(), startOffset, read, true)));
        }
        bytesRead += read;
        return read;
    }

    @Override
    public int write(ByteBuffer byteBuffer) throws IOException {
        int written = channel.write(byteBuffer);
        if (written > 0) {
            bytesWritten += written;
            if (seeked) {
                seeked = false;
                nonConsecutiveIops++;
            }
        }
        return written;
    }

    @Override
    public long position() throws IOException {
        return channel.position();
    }

    @Override
    public SeekableByteChannel position(long newPosition) throws IOException {
        if (channel.position() != newPosition) {
            channel.position(newPosition);
            seeked = true;
        }
        return this;
    }

    @Override
    public long size() throws IOException {
        return channel.size();
    }

    @Override
    public SeekableByteChannel truncate(long newSize) throws IOException {
        return channel.truncate(newSize);
    }

    @Override
    public boolean isOpen() {
        return channel.isOpen();
    }

    @Override
    public void close() throws IOException {
        if (channel.isOpen()) {
            channel.close();
            if (loggingEnabled) {
                System.out.printf("Total non-consecutive iops: %d, bytes read: %d, written: %d\n",
                        nonConsecutiveIops, bytesRead, bytesWritten);
            }
        }
    }
}
