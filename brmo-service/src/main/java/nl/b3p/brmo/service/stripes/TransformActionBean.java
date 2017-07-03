
package nl.b3p.brmo.service.stripes;

import java.util.Date;
import javax.sql.DataSource;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.After;
import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.SimpleMessage;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.ProgressUpdateListener;
import nl.b3p.brmo.loader.RsgbProxy;
import nl.b3p.brmo.loader.RsgbProxy.BerichtSelectMode;
import nl.b3p.brmo.service.util.ConfigUtil;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stripesstuff.plugin.waitpage.WaitPage;

/**
 *
 * @author Matthijs Laan
 */
public class TransformActionBean implements ActionBean, ProgressUpdateListener {
    private static final Log log = LogFactory.getLog(TransformActionBean.class);

    private static final String JSP = "/WEB-INF/jsp/transform/transform.jsp";

    private ActionBeanContext context;

    private long[] selectedIds;

    private double progress;

    private long total;

    private long processed;

    private boolean complete;

    private Date start;

    private Date update;

    private String exceptionStacktrace;

    private long standBerichtenVerwerkingsLimiet;

    @Before
    public void before() {
        start = new Date();
    }

    @After
    public void completed() {
        complete = true;
    }

    public void total(long total) {
        this.total = total;
    }

    public void progress(long progress) {
        this.processed = progress;
        if(this.total != 0) {
            this.progress = (100.0/this.total) * this.processed;
        }
        this.update = new Date();
    }

    public void exception(Throwable t) {
//        StringWriter sw = new StringWriter();
//        PrintWriter pw = new PrintWriter(sw);
//        t.printStackTrace(new PrintWriter(sw));
//        this.exceptionStacktrace = sw.toString();
        this.exceptionStacktrace=t.getLocalizedMessage();
    }

    private Resolution doTransform(RsgbProxy.BerichtSelectMode mode, boolean orderBerichten) {
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
            
            boolean renewConnectionAfterCommit = "true".equals(getContext().getServletContext().getInitParameter("renewconnectionaftercommit"));
            brmo.setRenewConnectionAfterCommit(renewConnectionAfterCommit);

            brmo.setOrderBerichten(orderBerichten);
            String errorState = getContext().getServletContext().getInitParameter("error.state");
            if (errorState != null) {
                brmo.setErrorState(errorState);
            }

            String maxBerichten = getContext().getServletContext().getInitParameter("stand.transform.max");
            if (maxBerichten != null) {
                brmo.setLimitStandBerichtenToTransform(Integer.parseInt(maxBerichten));
                log.info("Maximum aantal stand berichten voor transformatie batch ingesteld op: " + maxBerichten
                        + ". Mogelijk dient stand transformatie meerdere keren gestart te worden.");
                this.standBerichtenVerwerkingsLimiet = Integer.parseInt(maxBerichten);
            }
            Thread t = null;
            switch(mode) {
                case BY_IDS:
                    t = brmo.toRsgb(BerichtSelectMode.BY_IDS, selectedIds, this);
                    break;
                case BY_STATUS:
                    t = brmo.toRsgb(this);
                    break;
                case RETRY_WAITING:
                    t = brmo.toRsgb(BerichtSelectMode.RETRY_WAITING, null, this);
                    break;
                case BY_LAADPROCES:
                    brmo.setDataSourceRsgbBgt(ConfigUtil.getDataSourceRsgbBgt());
                    brmo.setDataSourceTopNL(ConfigUtil.getDataSourceTopNL());
                    t = brmo.toRsgb(BerichtSelectMode.BY_LAADPROCES, selectedIds, this);
                    break;
            }
            if (t != null) {
                t.join();
            }

            if(this.exceptionStacktrace == null) {
                getContext().getMessages().add(new SimpleMessage("Transformatie afgerond, controleer berichtenstatus."));
            }
        } catch(Throwable t) {
            log.error("Fout bij transformeren berichten naar RSGB", t);
            String m = "Fout bij transformeren berichten naar RSGB: " + ExceptionUtils.getMessage(t);
            if(t.getCause() != null) {
                m += ", oorzaak: " + ExceptionUtils.getRootCauseMessage(t);
            }
            getContext().getMessages().add(new SimpleMessage(m));
        } finally {
            if (brmo!=null) {
                brmo.closeBrmoFramework();
            }
        }

        return new ForwardResolution(JSP);
    }

    @WaitPage(path = JSP, delay = 1000, refresh = 1000)
    public Resolution transformSelected() {
        return doTransform(RsgbProxy.BerichtSelectMode.BY_IDS, true);
    }

    @WaitPage(path=JSP, delay=1000, refresh=1000)
    public Resolution transformAll() {
        return doTransform(RsgbProxy.BerichtSelectMode.BY_STATUS, true);
    }

    @WaitPage(path=JSP, delay=1000, refresh=1000)
    public Resolution transformAllStand() {
        return doTransform(RsgbProxy.BerichtSelectMode.BY_STATUS, false);
    }
    
    @WaitPage(path=JSP, delay=1000, refresh=1000)
    public Resolution transformRetry() {
        return doTransform(RsgbProxy.BerichtSelectMode.RETRY_WAITING, true);
    }

    @WaitPage(path=JSP, delay=1000, refresh=1000)
    public Resolution transformRetryStand() {
        return doTransform(RsgbProxy.BerichtSelectMode.RETRY_WAITING, false);
    }

    @WaitPage(path=JSP, delay=1000, refresh=1000)
    public Resolution transformSelectedLaadprocessen() {
        return doTransform(RsgbProxy.BerichtSelectMode.BY_LAADPROCES, true);
    }
    
    @WaitPage(path=JSP, delay=1000, refresh=1000)
    public Resolution transformSelectedLaadprocessenStand() {
        return doTransform(RsgbProxy.BerichtSelectMode.BY_LAADPROCES, false);
    }

    // <editor-fold defaultstate="collapsed" desc="getters en setters">
    public ActionBeanContext getContext() {
        return context;
    }

    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    public long[] getSelectedIds() {
        return selectedIds;
    }

    public void setSelectedIds(long[] selectedIds) {
        this.selectedIds = selectedIds;
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

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public String getExceptionStacktrace() {
        return exceptionStacktrace;
    }

    public void setExceptionStacktrace(String exceptionStacktrace) {
        this.exceptionStacktrace = exceptionStacktrace;
    }

    public long getStandBerichtenVerwerkingsLimiet() {
        return standBerichtenVerwerkingsLimiet;
    }

    public void setStandBerichtenVerwerkingsLimiet(long standBerichtenVerwerkingsLimiet) {
        this.standBerichtenVerwerkingsLimiet = standBerichtenVerwerkingsLimiet;
    }

    // </editor-fold>
}
