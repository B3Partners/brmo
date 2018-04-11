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
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * testcases voor {@link LogfileUtil}.
 *
 * @author Mark Prins
 */
public class LogfileUtilTest {

    @Test
    public void testGetLogFile() {
        String l = LogfileUtil.getLogfile();
        assertNotNull(l);
        assertEquals(getTestFileName(), l);
    }

    @Test
    public void getLogfileList() {
        List<String> l = LogfileUtil.getLogfileList();
        assertNotNull(l);
        assertFalse(l.isEmpty());
        assertEquals(getTestFileName(), l.get(0));
    }
    
    private String getTestFileName() {
        String s = System.getProperty("java.io.tmpdir");
        if (s.endsWith(File.separator)) {
            s = s.substring(0, s.length() - 1);
        }
        return s + File.separator + "LogfileUtilTest.log";
    }
}
