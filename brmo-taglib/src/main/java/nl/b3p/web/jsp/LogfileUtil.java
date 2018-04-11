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
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;

/**
 *
 * @author mprins
 */
public final class LogfileUtil {
    
    private static final Log LOG = LogFactory.getLog(LogfileUtil.class);

    /**
     * opzoeken van log file, de logger heeft de naam 'file' in de log4j
     * properties.
     *
     * @return volledig pad naar actuele logfile 'file', mogelijk null. Het pad
     * is de string die geconfigureerd is in de log4j properties
     */
    public static String getLogfile() {
        String file = null;
        Enumeration e = Logger.getRootLogger().getAllAppenders();
        while (e.hasMoreElements()) {
            Appender a = (Appender) e.nextElement();
            if (a instanceof FileAppender && a.getName() != null && a.getName().equals("file")) {
                LOG.debug("Gevonden logfile (naam): " + ((FileAppender) a).getFile());
                file = ((FileAppender) a).getFile();
                break;
            }
        }
        File f = new File(file);
        return f.getAbsolutePath();
    }

    /**
     * zoek alle logfiles, ook geroteerde, op aan de hand van de basename van de
     * actuele logfile.
     *
     * @return lijst met logfile namen incl. pad
     * @see #getLogfile()
     */
    public static List<String> getLogfileList() {
        List<String> files = new ArrayList<>();
        // opzoeken van brmo log files, de logger heeft de naam 'file' in de log4j properties
        final File f = new File(getLogfile());

        // filter op basename van actuele logfile zodat geroteerde files ook in de lijst komen
        DirectoryStream.Filter<Path> filter = (Path p) -> (p.getFileName().toString().startsWith(FilenameUtils.getBaseName(f.getName())));

        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(Paths.get(f.getParent()), filter)) {
            for (Path path : directoryStream) {
                files.add(path.toString());
            }
        } catch (IOException ioe) {
            LOG.warn("Lijst van logfiles ophalen is mislukt.", ioe);
        }
        return files;
    }
    
    private LogfileUtil() {
    }
}
