/*
 * Copyright (C) 2018 B3Partners B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.b3p.web.jsp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author mprins
 */
public class DownloadTag extends BodyTagSupport {

    private static final Log LOG = LogFactory.getLog(TailTag.class);

    private String file = null;

    public int doEndTag() throws JspException {
        if (this.file == null) {
            this.file = LogfileUtil.getLogfile();
        }
        File f = new File(this.file);
        f = f.isAbsolute() ? f : new File(this.pageContext.getServletContext().getRealPath("/"), this.file);
        sendFile(f, (HttpServletResponse) this.pageContext.getResponse(), (HttpServletRequest) this.pageContext.getRequest());
        return EVAL_PAGE;
    }

    private void sendFile(File f, HttpServletResponse response, HttpServletRequest request) throws JspException {
        final String filename = FilenameUtils.getName(f.getAbsolutePath());
        final boolean compress = request.getHeader("Accept-Encoding").contains("gzip");
        OutputStream outputStream;

        try {
            this.pageContext.getOut().clearBuffer();
        } catch (IOException ignore) {
            LOG.trace(ignore);
        }

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment;filename=\"" + filename + "\"");
        if (compress) {
            response.setHeader("Content-Encoding", "gzip");
        } else{
            response.setContentLength((int) f.length());
        }

        try {
            outputStream = response.getOutputStream();
            if (compress) {
                outputStream = new GZIPOutputStream(response.getOutputStream());
            }
        } catch (IOException e) {
            LOG.debug(e);
            throw new JspException(e.getLocalizedMessage());
        }

        if (!this.streamFile(f, outputStream)) {
            throw new JspException("Bestand kon niet worden geladen");
        } else {
            try {
                outputStream.flush();
                outputStream.close();
            } catch (IOException ignore) {
                LOG.trace(ignore);
            }

        }
    }

    private boolean streamFile(File file, OutputStream outputStream) {
        byte[] bytes = new byte[4096];
        boolean success = true;

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            int length;
            while ((length = fileInputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, length);
            }
        } catch (IOException e) {
            LOG.trace(e);
            success = false;
        }
        return success;
    }

    public String getFile() {
        return this.file;
    }

    public void setFile(String filename) {
        this.file = filename;
    }
}
