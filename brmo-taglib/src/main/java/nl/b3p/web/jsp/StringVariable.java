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

import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.VariableInfo;

/**
 *
 * @author mprins
 */
public class StringVariable extends TagExtraInfo {

    @Override
    public VariableInfo[] getVariableInfo(TagData data) {
        String id = data.getAttributeString("id");
        if (id == null) {
            id = TailTag.DEFAULT_ID;
        }

        return new VariableInfo[]{
            new VariableInfo(id, "java.lang.String", true, VariableInfo.NESTED)
        };
    }
}
