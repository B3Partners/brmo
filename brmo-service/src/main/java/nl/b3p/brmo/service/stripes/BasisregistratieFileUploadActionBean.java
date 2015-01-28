package nl.b3p.brmo.service.stripes;

import java.io.IOException;
import javax.sql.DataSource;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.validation.Validate;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.service.util.ConfigUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Action voor het handmatig uploaden van bestanden met basisregistratiegegevens
 * via een formulier.
 */
@StrictBinding
public class BasisregistratieFileUploadActionBean implements ActionBean {

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
            brmo.loadFromStream(basisregistratie, bestand.getInputStream(), bestand.getFileName());

            getContext().getMessages().add(new SimpleMessage("Bestand is ingelezen"));
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
