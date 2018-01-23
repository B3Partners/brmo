/*
 * Copyright (C) 2016 - 2017 B3Partners B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.b3p.topnl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Meine Toonen
 */
public enum TopNLType {
    TOP10NL("Top10NL"),
    TOP50NL("Top50NL"),
    TOP100NL("Top100NL"),
    TOP250NL("Top250NL");

    private final String type;

    private static final ArrayList<String> typen = new ArrayList();

    static {
        for (TopNLType s : values()) {
            typen.add(s.getType());
        }
    }

    TopNLType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static List<String> typen() {
        return Collections.unmodifiableList(typen);
    }

    public static boolean isTopNLType(String type) {
        for (TopNLType value : TopNLType.values()) {
            if (value.getType().equalsIgnoreCase(type)) {
                return true;
            }
        }
        return false;
    }

}
