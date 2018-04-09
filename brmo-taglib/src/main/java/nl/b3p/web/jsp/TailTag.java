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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.text.StringEscapeUtils;

/**
 *
 * @author mprins
 */
public class TailTag extends BodyTagSupport {

    private static final Log LOG = LogFactory.getLog(TailTag.class);
    public static final String DEFAULT_ID = "line";
    public static final int DEFAULT_COUNT = 50;
    private int count = DEFAULT_COUNT;
    private String file = null;
    private String id = DEFAULT_ID;
    private String[] buffer = null;
    private int current = 0;
    private int last = 0;

    public int doStartTag() throws JspException {
        if (this.count <= 0) {
            throw new JspException("Ongeldig aantal regels: " + this.count);
        } else {
            if (this.id == null) {
                this.id = DEFAULT_ID;
            }

            this.buffer = new String[this.count];
            this.current = -1;

            if (this.file == null) {
                this.file = LogfileUtil.getLogfile();
            }

            File f = new File(this.file);
            f = f.isAbsolute() ? f : new File(this.pageContext.getServletContext().getRealPath("/"), this.file);

            try (InputStreamReader inputStreamReader = new InputStreamReader(new FileInputStream(f))) {
                String line;
                for (BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                        (line = bufferedReader.readLine()) != null;
                        this.buffer[this.current] = StringEscapeUtils.escapeHtml4(line)) {

                    ++this.current;
                    if (this.current >= this.count) {
                        for (int lines = 0; lines < this.count - 1; ++lines) {
                            this.buffer[lines] = this.buffer[lines + 1];
                        }
                        this.current = this.count - 1;
                    }
                }
            } catch (IOException ignore) {
                LOG.debug("Probleem met benaderen van logfile: " + this.file, ignore);
            }

            if (this.current < 0) {
                return SKIP_BODY;
            } else {
                this.pageContext.setAttribute(this.id, this.buffer[0], 1);
                this.last = this.current;
                this.current = 0;
                return EVAL_BODY_BUFFERED;
            }
        }
    }

    public int doAfterBody() throws JspException {
        ++this.current;
        if (this.current > this.last) {
            try {
                this.getBodyContent().writeOut(this.getPreviousOut());
                return SKIP_BODY;
            } catch (Exception e) {
                throw new JspException(e);
            }
        } else {
            this.pageContext.setAttribute(this.id, this.buffer[this.current], 1);
            return EVAL_BODY_BUFFERED;
        }
    }

    @Override
    public int doEndTag() throws JspException {
        this.reset();
        return EVAL_PAGE;
    }

    @Override
    public void release() {
        this.reset();
    }

    /**
     * cleanup en reset.
     */
    private void reset() {
        this.count = DEFAULT_COUNT;
        this.file = null;
        this.id = DEFAULT_ID;
        this.buffer = null;
        this.current = 0;
        this.last = 0;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getFile() {
        return this.file;
    }

    public void setFile(String filename) {
        this.file = filename;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }
}
