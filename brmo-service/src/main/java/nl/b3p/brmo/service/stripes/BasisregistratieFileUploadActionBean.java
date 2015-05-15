package nl.b3p.brmo.service.stripes;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.sql.DataSource;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.validation.Validate;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.ProgressUpdateListener;
import nl.b3p.brmo.loader.util.BrmoException;
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

            String unzippedFile = null;

            if(header == ZIP_HEADER) {
                // Pak .xml bestanden in ZIP uit en verwerk deze

                ZipInputStream zip = new ZipInputStream(in);
                try {
                    ZipEntry entry = zip.getNextEntry();
                    while(entry != null) {
                        if(!entry.getName().toLowerCase().endsWith(".xml")) {
                            log.warn("Overslaan zip entry geen XML: " + entry.getName());
                        } else {
                            log.debug("Lezen XML bestand uit zip: " + entry.getName());
                            brmo.loadFromStream(basisregistratie, new CloseShieldInputStream(zip), bestand.getFileName() + "/" + entry.getName(), totalAdder);
                            extractedFiles++;
                        }
                        entry = zip.getNextEntry();
                    }
                } finally {
                    zip.close();
                }
            } else {
                // Geen ZIP, direct BR XML laden
                brmo.loadFromStream(basisregistratie, in, bestand.getFileName());
            }

            getContext().getMessages().add(new SimpleMessage("Bestand " + bestand.getFileName() + " is ingelezen, " + theTotal.getValue() + " berichten" + (extractedFiles > 0 ? " (uit " + extractedFiles + " uitgepakte XML bestanden)" : "")));
            log.info(String.format("Stored %s data from file \"%s\" uploaded via form", basisregistratie, bestand.getFileName()));
        } catch(Exception ex) {
            log.error("Fout bij inlezen bestand", ex);

            getContext().getMessages().add(new SimpleMessage("Fout bij inlezen bestand: " + ExceptionUtils.getRootCauseMessage(ex)));
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
