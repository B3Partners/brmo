package nl.b3p.brmo.service.stripes;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.sql.DataSource;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.validation.Validate;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.ProgressUpdateListener;
import nl.b3p.brmo.loader.updates.UpdateProcess;
import nl.b3p.brmo.service.util.ConfigUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stripesstuff.plugin.waitpage.WaitPage;

/**
 *
 * @author Matthijs Laan
 */
@StrictBinding
public class UpdatesActionBean implements ActionBean, ProgressUpdateListener {
    private static final Log log = LogFactory.getLog(UpdatesActionBean.class);

    private static final String JSP = "/WEB-INF/jsp/transform/updates.jsp";
    private static final String JSP_PROGRESS = "/WEB-INF/jsp/transform/updateprogress.jsp";

    private ActionBeanContext context;

    private List<UpdateProcess> updateProcesses;

    @Validate(required=true, on="update")
    private String updateProcessName;

    private double progress;
    private long total;
    private long processed;
    private boolean complete;
    private Date start;
    private Date update;
    private String exceptionStacktrace;

    // <editor-fold defaultstate="collapsed" desc="getters en setters">
    @Override
    public ActionBeanContext getContext() {
        return context;
    }

    @Override
    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getProcessed() {
        return processed;
    }

    public void setProcessed(long processed) {
        this.processed = processed;
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

    public String getExceptionStacktrace() {
        return exceptionStacktrace;
    }

    public void setExceptionStacktrace(String exceptionStacktrace) {
        this.exceptionStacktrace = exceptionStacktrace;
    }

    @Override
    public void total(long total) {
        this.total = total;
    }

    @Override
    public void progress(long progress) {
        this.processed = progress;
        if(this.total != 0) {
            this.progress = (100.0/this.total) * this.processed;
        }
        this.update = new Date();
    }

    @Override
    public void exception(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        this.exceptionStacktrace = sw.toString();
    }

    public List<UpdateProcess> getUpdateProcesses() {
        return updateProcesses;
    }

    public void setUpdateProcesses(List<UpdateProcess> updateProcesses) {
        this.updateProcesses = updateProcesses;
    }

    public String getUpdateProcessName() {
        return updateProcessName;
    }

    public void setUpdateProcessName(String updateProcessName) {
        this.updateProcessName = updateProcessName;
    }
    // </editor-fold>


    @Before(stages = LifecycleStage.BindingAndValidation)
    public void populateUpdateProcesses() {

        // XXX move to configuration file

        updateProcesses = Arrays.asList(new UpdateProcess[]{
            // bij een nieuw proces ook de wiki bijwerken: https://github.com/B3Partners/brmo/wiki/Snelle-updates
            new UpdateProcess("Toevoegen BSN aan ingeschreven natuurlijk persoon", BrmoFramework.BR_BRK, "/xsl/update-bsn.xsl"),
            new UpdateProcess("Toevoegen RSIN aan ingeschreven niet-natuurlijk persoon", BrmoFramework.BR_BRK, "/xsl/update-rsin.xsl"),
            new UpdateProcess("Bijwerken van omschrijving, datum en ref_id in brondocument", BrmoFramework.BR_BRK, "/xsl/update-brondocument.xsl"),
            new UpdateProcess("Bijwerken van onvolledig adres", BrmoFramework.BR_BRK, "/xsl/update-incompleetadres.xsl"),
            new UpdateProcess("Bijwerken van rechthebbende VVE op zakelijk recht", BrmoFramework.BR_BRK, "/xsl/update-zak_recht-vve.xsl")
        });
    }

    @DefaultHandler
    public Resolution form() {
        return new ForwardResolution(complete ? JSP_PROGRESS : JSP);
    }

    @WaitPage(path=JSP_PROGRESS, delay=1000, refresh=1000)
    public Resolution update() {
        UpdateProcess process = null;

        for(UpdateProcess p: updateProcesses) {
            if(p.getName().equals(updateProcessName)) {
                process = p;
                break;
            }
        }
        if(process == null) {
            getContext().getMessages().add(new SimpleMessage("Ongeldig proces"));
            return new ForwardResolution(JSP);
        }

        start = new Date();
        log.info("Start update process: " + process.getName());

        // Get berichten
        BrmoFramework brmo = null;
        try {
            DataSource dataSourceStaging = ConfigUtil.getDataSourceStaging();
            DataSource dataSourceRsgb = ConfigUtil.getDataSourceRsgb();
            brmo = new BrmoFramework(dataSourceStaging, dataSourceRsgb);

            boolean enableTransformPipeline = "true".equals(getContext().getServletContext().getInitParameter("pipelining.enabled"));
            brmo.setEnablePipeline(enableTransformPipeline);
            if(enableTransformPipeline) {
                String capacityString = getContext().getServletContext().getInitParameter("pipelining.capacity");
                if(capacityString != null) {
                    brmo.setTransformPipelineCapacity(Integer.parseInt(capacityString));
                }
            }
            String batchString = getContext().getServletContext().getInitParameter("batch.capacity");
            if(batchString != null) {
                brmo.setBatchCapacity(Integer.parseInt(batchString));
            }
            String errorState = getContext().getServletContext().getInitParameter("error.state");
            if (errorState != null) {
                brmo.setErrorState(errorState);
            }

            Thread t = brmo.toRsgb(process, this);
            t.join();

            if(this.exceptionStacktrace == null) {
                getContext().getMessages().add(new SimpleMessage("Transformatie afgerond, controleer berichtenstatus."));
            }
        } catch(Throwable t) {
            log.error("Fout bij updaten", t);
            String m = "Fout bij updaten: " + ExceptionUtils.getMessage(t);
            if(t.getCause() != null) {
                m += ", oorzaak: " + ExceptionUtils.getRootCauseMessage(t);
            }
            getContext().getMessages().add(new SimpleMessage(m));
        } finally {
            if (brmo!=null) {
                brmo.closeBrmoFramework();
            }
            complete = true;
        }
        return new ForwardResolution(JSP_PROGRESS);
    }
}
