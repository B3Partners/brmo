/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.util.http;

import nl.b3p.brmo.util.ResumingInputStream;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A {@code SeekableByteChannel} backed by a HTTP(S) URI that uses the Content-Length response header to provide total
 * size and HTTP Range requests to for read-only random access, especially optimized for ZIP archive readers using only
 * the central directory at the end of the stream supporting a {@code SeekableByteChannel} input such as
 * <a href="https://commons.apache.org/proper/commons-compress/">Commons Compress.</a>
 * <p>
 * This class assumes most reads to be consecutive like a normal {@code InputStream} and sends a HTTP request starting
 * from the position until the end. When a seek is done using the {@link #position()} method, the HTTP request is
 * aborted and a new request is started from the new position until the end, unless it is more optimal to discard some
 * bytes for a small forward seek. This means that the server may receive an error writing its' response with a
 * "Connection reset" error. This strategy is optimal when an application performs reads in consecutive chunks.
 * <p>
 * Random-access reading code may assume seeks are fast. Even compared to disks using physical platters, HTTP seeks
 * are slow and should be minimized. Aborting a HTTP request should be handled gracefully by a HTTP server but may lead
 * to unwanted "Connection reset" errors. When reading ZIP entries especially, try to avoid reading the local file
 * header or data descriptor following archive entry data and use the central directory only. With Commons Compress this
 * means setting the {@code ZipFile} constructor parameter {@code ignoreLocalFileHeader} to true.
 * <p>
 * This class can be used with any synchronous HTTP client implementations that support sending headers, reading
 * response headers and reading from the response using an InputStream, such as Apache HttpComponents, OkHttp, Spring
 * RestTemplate, etc by providing a {@link HttpClientWrapper} to the constructor. This classes uses the Java 11
 * {@link java.net.http.HttpClient} when using Java 11 or higher and {@link java.net.URLConnection} when using Java 8 if
 * no wrapper is passed to the constructor. These are configured to follow redirects by default.
 * <p>
 * If the {@link #size()} method is called before a Content-Length HTTP response header has been received, the content
 * length is read using GET request with a 0 byte range instead of a HEAD request. This is conceptually similar, but it
 * immediately tests for Range header support and supports redirects if the original URL does not support HEAD
 * requests.
 * <p>
 * The HTTP client should be configured by the user of this class whether it follows redirects. Because this class may
 * do many requests for certain ranges, a redirect may happen each time. You can optionally update the URI after the
 * first request to the last redirect location using {@link #setURI(URI)}. For Apache HttpComponents this can be
 * retrieved using {@code HttpClientContext.getRedirectLocations()}.
 * <p>
 * This class automatically uses a {@link ResumingInputStream} to retry reading the HTTP response when reads fail until
 * a maximum number of tries.
 * <p>
 * The web server must send {@code Etag} or {@code Last-Modified} headers to use in {@code If-Range} request headers. If
 * a weak Etag is sent the {@code Last-Modified} header must be present and is used instead of the Etag for the
 * {@code If-Range} header.
 * <p>
 * This class is not thread-safe.
 *
 * @author Matthijs Laan
 */
public class HttpSeekableByteChannel implements SeekableByteChannel {
    private static final int DEFAULT_SEEK_BUFFER_SIZE = 16 * 1024;
    private static final int DEFAULT_MAX_DISCARD_SIZE = 16 * 1024;
    private static final int READ_BUFFER_SIZE = 8 * 1024;

    private URI uri;
    private final HttpClientWrapper httpClientWrapper;
    private final UnaryOperator<ResumingInputStream> resumableInputStreamWrapper;
    private final int seekBufferSize;
    private byte[] seekBuffer;
    private byte[] buffer;
    private final int maxDiscardSize;

    private long position = 0;
    private int httpRequestCount = 0;
    private long bytesRead = 0;
    private Long newPosition;
    private Long contentLength;

    private ResumingInputStream currentHttpResponseBodyInputStream;

    private boolean debug = false;

    public HttpSeekableByteChannel(URI uri) {
        this(uri, HttpClientWrappers.getDefault());
    }

    public HttpSeekableByteChannel(URI uri, HttpClientWrapper httpClientWrapper) {
        this(uri, httpClientWrapper, UnaryOperator.identity(), DEFAULT_SEEK_BUFFER_SIZE, DEFAULT_MAX_DISCARD_SIZE);
    }

    public HttpSeekableByteChannel(URI uri, HttpClientWrapper httpClientWrapper, UnaryOperator<ResumingInputStream> resumableInputStreamWrapper, int seekBufferSize, int maxDiscardSize) {
        this.uri = uri;
        this.httpClientWrapper = httpClientWrapper;
        this.resumableInputStreamWrapper = resumableInputStreamWrapper;
        this.seekBufferSize = seekBufferSize;
        this.maxDiscardSize = maxDiscardSize;
    }

    /**
     * @return The number of HTTP range requests done.
     */
    public int getHttpRequestCount() {
        return httpRequestCount;
    }

    /**
     * @return The total number of bytes read. This may be larger than the size when seeking backwards and reading
     * content multiple times.
     */
    public long getBytesRead() {
        return bytesRead;
    }

    public void setURI(URI uri) {
        this.uri = uri;
    }

    public HttpSeekableByteChannel withDebug(boolean debug) {
        this.debug = debug;
        return this;
    }

    @Override
    public long position() throws IOException {
        return newPosition != null ? newPosition : position;
    }

    @Override
    public SeekableByteChannel position(long newPosition) throws IOException {
        this.newPosition = newPosition;
        return this;
    }

    @Override
    public long size() throws IOException {
        if (contentLength == null) {
            // Do a GET request using zero range instead of a HEAD request, to test for Range header support and to
            // avoid HEAD requests to a location that redirects but does not support the HEAD method.

            HttpResponseWrapper response = null;
            try {
                response = httpClientWrapper.request(uri, "Range", "bytes=0-0");
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
            if (response.getStatusCode() != 206) {
                throw new IOException("Expected 206 Partial Content, but got status code " + response.getStatusCode() + " getting content length for " + uri);
            }
            String contentRange = response.getFirstHeader("Content-Range");
            if (contentRange == null) {
                throw new IOException("Missing Content-Range response header getting content length for " + uri);
            }
            Matcher m = Pattern.compile("bytes\\s+\\d+-\\d+/(\\d+)").matcher(contentRange);
            if (!m.matches()) {
                throw new IOException("Invalid Content-Range response header value \"" + contentRange + "\" getting content length for " + uri);
            }
            contentLength = Long.parseLong(m.group(1));
            if (contentLength < 0) {
                throw new IOException("Invalid Content-Range response header value \"" + contentRange + "\" getting content length for " + uri);
            }
        }
        return contentLength;
    }

    @Override
    public int read(ByteBuffer byteBuffer) throws IOException {
        if (newPosition != null && newPosition != position) {
            if (currentHttpResponseBodyInputStream != null) {
                // Determine if we should close the stream or read and discard
                if (newPosition > position && newPosition - position <= maxDiscardSize) {
                    if (debug) System.out.printf(" [Discard %d bytes to seek forward from position %d to %d ] ", newPosition - position, position, newPosition);
                    if (seekBuffer == null) {
                        seekBuffer = new byte[seekBufferSize];
                    }
                    int remaining = (int) (newPosition - position);
                    do {
                        remaining -= currentHttpResponseBodyInputStream.read(seekBuffer, 0, Math.min(remaining, seekBuffer.length));
                    } while(remaining > 0);
                    // Input stream is at correct position
                } else {
                    try {
                        currentHttpResponseBodyInputStream.close();
                    } catch(IOException ignored) {
                    }
                    currentHttpResponseBodyInputStream = null;
                }
            }
            position = newPosition;
            newPosition = null;
        }

        if (currentHttpResponseBodyInputStream == null) {
            if (debug) System.out.print(" [GET at position " + position + "] ");
            httpRequestCount++;
            currentHttpResponseBodyInputStream = resumableInputStreamWrapper.apply(
                    new ResumingInputStream(new HttpStartRangeInputStreamProvider(uri, httpClientWrapper), position)
            );
        }

        int read;
        if (byteBuffer.hasArray()) {
            read = currentHttpResponseBodyInputStream.read(byteBuffer.array(), byteBuffer.arrayOffset() + byteBuffer.position(), byteBuffer.remaining());
            if (read > 0) {
                byteBuffer.position(byteBuffer.position() + read);
            }
        } else {
            if (buffer == null) {
                buffer = new byte[READ_BUFFER_SIZE];
            }
            read = currentHttpResponseBodyInputStream.read(buffer, 0, Math.min(byteBuffer.remaining(), buffer.length));
            if (read > 0) {
                byteBuffer.put(buffer, 0, read);
            }
        }
        if (read > 0) {
            position += read;
            bytesRead += read;
        }
        return read;
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void close() throws IOException {
        if (currentHttpResponseBodyInputStream != null) {
            currentHttpResponseBodyInputStream.close();
        }
    }

    @Override
    public int write(ByteBuffer byteBuffer) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public SeekableByteChannel truncate(long l) throws IOException {
        throw new UnsupportedOperationException();
    }
}
