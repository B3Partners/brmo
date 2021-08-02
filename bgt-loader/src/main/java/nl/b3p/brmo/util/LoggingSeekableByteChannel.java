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

public class LoggingSeekableByteChannel implements SeekableByteChannel {
    private final SeekableByteChannel channel;
    private Long size = null;
    private int seeks = 0;
    private  long bytesRead = 0;
    private boolean repositioned = false;
    private boolean loggingEnabled = false;
    private boolean open = true;

    public LoggingSeekableByteChannel(SeekableByteChannel channel) {
        this.channel = channel;
    }

    public int getSeeks() {
        return seeks;
    }

    public void setSeeks(int seeks) {
        this.seeks = seeks;
    }

    public long getBytesRead() {
        return bytesRead;
    }

    public void setBytesRead(long bytesRead) {
        this.bytesRead = bytesRead;
    }

    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    public void setLoggingEnabled(boolean loggingEnabled) {
        this.loggingEnabled = loggingEnabled;
    }

    @Override
    public int read(ByteBuffer byteBuffer) throws IOException {
        if (repositioned) {
            if(loggingEnabled) System.out.printf("new position %15d: read into buffer %s", channel.position(), byteBuffer);
            repositioned = false;
        } else {
            if(loggingEnabled) System.out.printf("consecutive  %15d: read into buffer %s", channel.position(), byteBuffer);
        }
        int startOffset = byteBuffer.arrayOffset();
        int read = channel.read(byteBuffer);
        if(loggingEnabled) {
            System.out.printf(", read %s, contents: %s\n", read, new String(org.apache.commons.codec.binary.Hex.encodeHex(byteBuffer.array(), startOffset, read, true)));
        }
        bytesRead += read;
        return read;
    }

    @Override
    public int write(ByteBuffer byteBuffer) throws IOException {
        return 0;
    }

    @Override
    public long position() throws IOException {
        return channel.position();
    }

    @Override
    public SeekableByteChannel position(long l) throws IOException {
        if (channel.position() != l) {
            channel.position(l);
            seeks++;
            repositioned = true;
        }
        return this;
    }

    @Override
    public long size() throws IOException {
        return channel.size();
    }

    @Override
    public SeekableByteChannel truncate(long l) throws IOException {
        return null;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public void close() throws IOException {
        if (open) {
            channel.close();
            if (loggingEnabled) {
                System.out.printf("Total channel seeks: %d, bytes read: %d\n", seeks, bytesRead);
            }
            open = false;
        }
    }
}
