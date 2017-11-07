
package nl.b3p.brmo.service.stripes;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.validation.Validate;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.service.util.ConfigUtil;
import nl.b3p.web.stripes.DirectResponseResolution;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Endpoint voor het ontvangen van mutatieberichten van basisregistraties in de
 * request body.
 *

 * @author Matthijs Laan
 */
@UrlBinding("/post/{basisregistratie}")
@StrictBinding
public class BasisregistratieServiceActionBean implements ActionBean {
    private static final Log log = LogFactory.getLog(BasisregistratieServiceActionBean.class);

    private ActionBeanContext context;

    @Validate
    private String basisregistratie;

    // <editor-fold defaultstate="collapsed" desc="getters en setters">
    public ActionBeanContext getContext() {
        return context;
    }

    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    public String getBasisregistratie() {
        return basisregistratie;
    }

    public void setBasisregistratie(String basisregistratie) {
        this.basisregistratie = basisregistratie;
    }
    // </editor-fold>

    private static final int ZIP_HEADER = 0x504b0304;

    @DefaultHandler
    public Resolution read() {
        HttpServletRequest req = getContext().getRequest();

        if(!req.getMethod().equals("POST")) {
            return new DirectResponseResolution(405, "Only POST allowed\n");
        }

        if(req.getContentLength() > ConfigUtil.MAX_UPLOAD_SIZE) {
            return new DirectResponseResolution(413, String.format("Maximum upload size of %s exceeded\n", FileUtils.byteCountToDisplaySize(ConfigUtil.MAX_UPLOAD_SIZE)));
        }

        BrmoFramework brmo = null;
        try {
            DataSource ds = ConfigUtil.getDataSourceStaging();
            brmo = new BrmoFramework(ds, null);

            // Check of bestand begint met ZIP header
            
            InputStream in = new BufferedInputStream(req.getInputStream());
            in.mark(4);
            int header = new DataInputStream(in).readInt();
            in.reset();

            String unzippedFile = null;

            if(header == ZIP_HEADER) {
                // Pak eerste .xml bestand in ZIP uit en verwerk deze

                // Gebruik niet bestandsnaam van ZIP entries, altijd "MUTBX01.xml" oid (iig niet uniek)

                ZipInputStream zip = new ZipInputStream(in);
                ZipEntry entry = zip.getNextEntry();
                while(entry != null && !entry.getName().toLowerCase().endsWith(".xml")) {
                    log.warn("Overslaan zip entry geen XML: " + entry.getName());
                    entry = zip.getNextEntry();
                }
                if(entry == null) {
                    throw new BrmoException("Geen geschikt XML bestand gevonden in zip bestand ");
                }
                log.debug("Lezen XML bestand uit zip: " + entry.getName());
                unzippedFile = entry.getName();
                brmo.loadFromStream(basisregistratie, zip, getUniqueFilename(brmo));

            } else {
                // Geen ZIP, direct BR XML laden
                brmo.loadFromStream(basisregistratie, in, getUniqueFilename(brmo));
            }

            log.info(String.format("Stored %s data received via service endpoint", basisregistratie));
            return new DirectResponseResolution(200, "Data successfully received" + (header == ZIP_HEADER ? ", unzipped \"" + unzippedFile + "\"": "") + " and stored\n");
            } catch(Exception e) {
            log.error("Fout bij ontvangen basisregistratiegegevens via directe POST", e);

            return new DirectResponseResolution(500, String.format("Error receiving data: %s\n", ExceptionUtils.getRootCauseMessage(e)));
        } finally {
            if(brmo != null) {
                brmo.closeBrmoFramework();
            }
        }
    }

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    private String getUniqueFilename(BrmoFramework brmo) throws Exception {
        // Unieke bestandsnaam nodig

        String name;
        boolean wait = false;
        do {
            name = "Upload op " + sdf.format(new Date());
            if(!wait) {
                wait = true;
            } else {
                Thread.sleep(10);
            }
            // Check of in dezelfde milliseconde gePOST laadproces al bestaat...
        } while(brmo.getLaadProcesIdByFileName(name) != null);

        return name;
    }
}
