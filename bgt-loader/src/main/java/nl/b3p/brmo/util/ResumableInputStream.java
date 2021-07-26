/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package nl.b3p.brmo.util;

import java.io.IOException;
import java.io.InputStream;

/**
 * An InputStream that wraps an InputStream that on a read error can be re-constructed with the current position as
 * start position (using a HTTP Range request for instance) and the read retried.
 */
public class ResumableInputStream extends InputStream {

    public interface StreamAtStartPositionProvider {
        InputStream get(long position, int totalRetries, Exception causeForRetry) throws IOException;
    }

    public static final int DEFAULT_MAX_TRIES = 5;

    private final StreamAtStartPositionProvider streamProvider;
    private long position = 0;
    private InputStream delegate = null;
    private int maxReadTries;
    private int totalRetries = 0;
    private int currentDelegatePosition = 0;

    public ResumableInputStream(StreamAtStartPositionProvider streamProvider) {
        this(streamProvider, DEFAULT_MAX_TRIES);
    }

    public ResumableInputStream(StreamAtStartPositionProvider streamProvider, int maxReadTries) {
        this.streamProvider = streamProvider;
        this.maxReadTries = maxReadTries;
    }

    /**
     * @return The number of bytes read so far.
     */
    public long getPosition() {
        return position;
    }

    /**
     * @return The number of bytes read since start or the last retry, if any.
     */
    public int getCurrentDelegatePosition() {
        return currentDelegatePosition;
    }

    /**
     * @return The total number of retries spanning all read() calls.
     */
    public int getTotalRetries() {
        return totalRetries;
    }

    public int getMaxReadTries() {
        return maxReadTries;
    }

    public void setMaxReadTries(int maxReadTries) {
        this.maxReadTries = maxReadTries;
    }

    private void ensureDelegateOpen() throws IOException {
        if (delegate == null) {
            delegate = streamProvider.get(position, totalRetries,null);
        }
    }

    @Override
    public int read() throws IOException {
        byte[] b = new byte[1];
        int read = this.read(b, 0, 1);
        if (read == 1) {
            return b[0];
        } else {
            // -1 for EOF
            return read;
        }
    }

    @Override
    public int available() throws IOException {
        ensureDelegateOpen();
        return delegate.available();
    }

    @Override
    public void close() throws IOException {
        if (delegate != null) {
            delegate.close();
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        // Opening the delegate is outside the (re)try block. Only read() calls are retried: this allows the input
        // stream to fail fast without retries on a fatal error (such as a HTTP 404 response code).
        ensureDelegateOpen();

        int tries = 0;
        while(true) {
            try {
                int count = delegate.read(b, off, len);
                if (count == -1) {
                    return count;
                }
                position += count;
                currentDelegatePosition += count;
                return count;
            } catch (IOException e) {
                onReadException(++tries, e);
                totalRetries++;
                retryingAfterReadException(tries, totalRetries);
                try {
                    delegate.close();
                } catch(IOException ignored) {
                }
                delegate = streamProvider.get(position, totalRetries, e);
                currentDelegatePosition = 0;
            }
        }
    }

    /**
     * Called when a read() on the delegate threw an exception.
     * @param tries Number of tries
     * @param cause The read exception
     * @throws IOException When the number of tries exceeds the maximum
     */
    protected void onReadException(int tries, IOException cause) throws IOException {
        if (tries > maxReadTries) {
            throw new IOException("Max tries (" + tries + ") exceeded", cause);
        }
    }

    /**
     * Called when going to retry after a read() call threw an Exception.
     * @param tries Number of tries this read() call
     */
    protected void retryingAfterReadException(int tries, int totalRetries) {
    }
}
