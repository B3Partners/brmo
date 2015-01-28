/*
 * Copyright (C) 2012-2013 B3Partners B.V.
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
package nl.b3p.web.stripes;

import javax.servlet.http.HttpServletResponse;
import net.sourceforge.stripes.action.StreamingResolution;
import org.json.JSONObject;

/**
 *
 * @author matthijsln
 */
public class DirectResponseResolution extends StreamingResolution {

    private int status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;

    public DirectResponseResolution(String message) {
        super("text/plain", message);
        setCharacterEncoding("UTF-8");
    }

    public DirectResponseResolution(int status, String message) {
        this(message);
        this.status = status;
    }

    public DirectResponseResolution(JSONObject json) {
        super("application/json", json.toString());
        setCharacterEncoding("UTF-8");
    }

    public DirectResponseResolution(int status, JSONObject json) {
        this(json);
        this.status = status;
    }

    @Override
    protected void stream(HttpServletResponse response) throws Exception {
        response.setStatus(status);
        super.stream(response);
    }
}