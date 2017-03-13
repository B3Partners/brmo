package nl.b3p.brmo.service.stripes;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.sql.DataSource;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.validation.Validate;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.ProgressUpdateListener;
import nl.b3p.brmo.loader.util.BrmoDuplicaatLaadprocesException;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.loader.util.BrmoLeegBestandException;
import nl.b3p.brmo.service.util.ConfigUtil;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.mutable.MutableLong;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Action voor het handmatig uploaden van bestanden met basisregistratiegegevens
 * via een formulier.
 */
@StrictBinding
public class BasisregistratieFileUploadActionBean implements ActionBean {
    private static final int ZIP_HEADER = 0x504b0304;

    private static final Log log = LogFactory.getLog(BasisregistratieFileUploadActionBean.class);

    private ActionBeanContext context;

    @Validate(required=true)
    private String basisregistratie;

    @Validate(required=true)
    private FileBean bestand;

    @DefaultHandler
    @DontValidate
    public Resolution form() {
        return new ForwardResolution("/WEB-INF/jsp/bestand/form.jsp");
    }

    public Resolution upload() throws BrmoException {
        DataSource ds = ConfigUtil.getDataSourceStaging();
        BrmoFramework brmo = null;
        brmo = new BrmoFramework(ds, null);
        ZipInputStream zip = null;
        ZipEntry entry = null;
        int errors = 0;
        int empty = 0;
        int duplicate = 0;
        final int ERROR_LIMIT = 5;
        try {
            final MutableLong theTotal = new MutableLong(0);
            int extractedFiles = 0;
            final ProgressUpdateListener totalAdder = new ProgressUpdateListener() {

                @Override
                public void total(long total) {
                    theTotal.setValue(theTotal.getValue() + total);
                }

                @Override
                public void progress(long progress) {
                }

                @Override
                public void exception(Throwable t) {
                }
            };
            // Check of bestand begint met ZIP header

            InputStream in = new BufferedInputStream(bestand.getInputStream());
            in.mark(4);
            int header = new DataInputStream(in).readInt();
            in.reset();

            if(header == ZIP_HEADER) {
                // Pak .xml bestanden in ZIP uit en verwerk deze

                zip = new ZipInputStream(in);
                try {
                    entry = zip.getNextEntry();
                    while(entry != null) {
                        if(!entry.getName().toLowerCase().endsWith(".xml")) {
                            log.warn("Overslaan zip entry geen XML: " + entry.getName());
                        } else {
                            log.debug("Lezen XML bestand uit zip: " + entry.getName());
                            try {
                                brmo.loadFromStream(basisregistratie, new CloseShieldInputStream(zip), bestand.getFileName() + "/" + entry.getName(), totalAdder);
                            } catch(BrmoLeegBestandException e) {
                                log.info("Negeer ZIP entry " + entry.getName() + ": " + e.getMessage());
                                empty++;
                            } catch(BrmoDuplicaatLaadprocesException e) {
                                log.info("Negeer ZIP entry " + entry.getName() + ": duplicaat laadproces");
                                duplicate++;
                            } catch(BrmoException e) {
                                errors++;
                                log.error("BrmoException bij laden ZIP entry " + entry.getName() + (errors > ERROR_LIMIT ? "; afbreken" : "; doorgaan met volgende"), e);
                                if(errors > ERROR_LIMIT) {
                                    throw new BrmoException("Maximum aantal fouten (" + ERROR_LIMIT + ") bereikt bij inladen ZIP bestanden, inladen afgebroken. Controleer brmo-service.log", e);
                                }
                            }
                            extractedFiles++;
                        }
                        entry = zip.getNextEntry();
                    }
                } finally {
                    zip.close();
                }
            } else {
                // Geen ZIP, direct BR XML laden
                brmo.loadFromStream(basisregistratie, in, bestand.getFileName(), totalAdder);
            }

            List warnings = new ArrayList();
            if(errors > 0) {
                warnings.add("let op: " + errors + " bericht" + (errors > 1 ? "en" : "") + " niet ingeladen wegens fouten");
            }
            if(empty > 0) {
                warnings.add("let op: " + empty + (empty > 1 ? " lege berichten" : " leeg bericht") + " overgeslagen");
            }
            if(duplicate > 0) {
                warnings.add("let op: " + duplicate + (duplicate > 1 ? " duplicate berichten" : " duplicaat bericht") + " overgeslagen");
            }

            String warning = warnings.isEmpty() ? "" : ", " + String.join(", ", (String[])warnings.toArray(new String[] {})) + ", zie log voor bestandsnamen uit ZIP en details!";

            getContext().getMessages().add(new SimpleMessage("Bestand " + bestand.getFileName() + " is ingelezen, " + theTotal.getValue() + " berichten" + (extractedFiles > 0 ? " (uit " + extractedFiles + " uitgepakte XML bestanden)" : "") + warning));
            log.info(String.format("Stored %s data from file \"%s\" uploaded via form", basisregistratie, bestand.getFileName()));
        } catch(Exception ex) {
            String msg = "Fout bij inlezen bestand " + (zip != null && entry != null ? entry.getName() + " uit ZIP bestand " + bestand.getFileName() : bestand.getFileName());
            log.error(msg, ex);

            getContext().getMessages().add(new SimpleMessage(msg + ": " + ExceptionUtils.getRootCauseMessage(ex)));
        } finally {
            try {
                bestand.delete();
            } catch(IOException e) {
                // ignore
            }
            brmo.closeBrmoFramework();
        }

        return new ForwardResolution("/WEB-INF/jsp/bestand/form.jsp");
    }

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

    public FileBean getBestand() {
        return bestand;
    }

    public void setBestand(FileBean bestand) {
        this.bestand = bestand;
    }
    // </editor-fold>
}
