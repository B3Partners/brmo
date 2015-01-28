
package nl.b3p.brmo.service.stripes;

import java.io.File;
import java.util.Date;
import javax.sql.DataSource;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.DontValidate;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.SimpleMessage;
import net.sourceforge.stripes.action.StrictBinding;
import net.sourceforge.stripes.validation.Validate;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.ProgressUpdateListener;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.service.util.ConfigUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stripesstuff.plugin.waitpage.WaitPage;

/**
 *
 * @author Matthijs Laan
 */
@StrictBinding
public class BasisregistratieBigFileLoadActionBean implements ActionBean, ProgressUpdateListener {
    private static final Log log = LogFactory.getLog(BasisregistratieBigFileLoadActionBean.class);

    private ActionBeanContext context;

    @Validate(required=true)
    private String basisregistratie;

    @Validate(required=true)
    private String filename;

    private long totalBytes;

    private long processedBytes;

    private double progress;

    private String progressDisplay;

    private boolean complete;

    private Date start;

    private Date update;

    @DefaultHandler
    @DontValidate
    public Resolution form() {
        return new ForwardResolution("/WEB-INF/jsp/bestand/form.jsp");
    }

    public void total(long bytes) {
        this.totalBytes = bytes;
    }

    public void progress(long bytes) {
        this.processedBytes = bytes;
        this.progress = (100.0/totalBytes) * this.processedBytes;
        this.progressDisplay = FileUtils.byteCountToDisplaySize(this.processedBytes);
        update = new Date();
    }

    public void exception(Throwable t) {
    }

    @WaitPage(path="/WEB-INF/jsp/bestand/bigfile.jsp", delay=1000, refresh=1000)
    public Resolution load() throws BrmoException {
        start = new Date();

        try {
            File f = new File(filename);
            if(!f.exists() || !f.canRead() || f.length() == 0) {
                getContext().getMessages().add(new SimpleMessage("Bestand bestaat niet, is leeg of kan niet worden gelezen"));
                return new ForwardResolution("/WEB-INF/jsp/bestand/bigfile.jsp");
            }

            DataSource ds = ConfigUtil.getDataSourceStaging();
            BrmoFramework brmo = null;
            brmo = new BrmoFramework(ds, null);

            brmo.loadFromFile(basisregistratie, filename, this);

            getContext().getMessages().add(new SimpleMessage("Klaar met inladen"));
        } catch(Throwable t) {
            log.error("Fout bij inladen bestand van server", t);
            String m = "Fout bij inladen bestand: " + ExceptionUtils.getMessage(t);
            if(t.getCause() != null) {
                m += ", oorzaak: " + ExceptionUtils.getRootCauseMessage(t);
            }
            getContext().getMessages().add(new SimpleMessage(m));
        } finally {
            complete = true;
        }
        return new ForwardResolution("/WEB-INF/jsp/bestand/bigfile.jsp");
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

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getUpdate() {
        return update;
    }

    public void setUpdate(Date update) {
        this.update = update;
    }

    public long getTotalBytes() {
        return totalBytes;
    }

    public void setTotalBytes(long totalBytes) {
        this.totalBytes = totalBytes;
    }

    public long getProcessedBytes() {
        return processedBytes;
    }

    public void setProcessedBytes(long processedBytes) {
        this.processedBytes = processedBytes;
    }

    public String getProgressDisplay() {
        return progressDisplay;
    }

    public void setProgressDisplay(String progressDisplay) {
        this.progressDisplay = progressDisplay;
    }
    // </editor-fold>
}
