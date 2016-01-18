package nl.b3p.brmo.service.stripes;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.sql.DataSource;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.validation.Validate;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.ProgressUpdateListener;
import nl.b3p.brmo.loader.exports.ExportProcess;
import nl.b3p.brmo.loader.jdbc.GeometryJdbcConverter;
import nl.b3p.brmo.loader.jdbc.GeometryJdbcConverterFactory;
import nl.b3p.brmo.loader.util.StagingRowHandler;
import nl.b3p.brmo.persistence.staging.Bericht;
import nl.b3p.brmo.service.util.ConfigUtil;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.RowProcessor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stripesstuff.plugin.waitpage.WaitPage;

/**
 *
 * @author Chris van Lith
 */
@StrictBinding
public class ExportActionBean implements ActionBean, ProgressUpdateListener {

    private static final Log log = LogFactory.getLog(ExportActionBean.class);

    private static final String JSP = "/WEB-INF/jsp/transform/exports.jsp";
    private static final String JSP_PROGRESS = "/WEB-INF/jsp/transform/exportsprogress.jsp";

    private ActionBeanContext context;

    private List<ExportProcess> exportProcesses;

    @Validate(required = true, on = "export")
    private String exportProcessName;

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
        if (this.total != 0) {
            this.progress = (100.0 / this.total) * this.processed;
        }
        this.update = new Date();
    }

    @Override
    public void exception(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        this.exceptionStacktrace = sw.toString();
    }

    public List<ExportProcess> getExportProcesses() {
        return exportProcesses;
    }

    public void setExportProcesses(List<ExportProcess> exportProcesses) {
        this.exportProcesses = exportProcesses;
    }

    public String getExportProcessName() {
        return exportProcessName;
    }

    public void setExportProcessName(String exportProcessName) {
        this.exportProcessName = exportProcessName;
    }
    // </editor-fold>

    @Before(stages = LifecycleStage.BindingAndValidation)
    public void populateExportProcesses() {

        // XXX move to configuration file
        exportProcesses = Arrays.asList(new ExportProcess[]{
            new ExportProcess("Exporteren BRK mutaties", BrmoFramework.BR_BRK, "/tmp")
        });
    }

    @DefaultHandler
    public Resolution form() {
        return new ForwardResolution(complete ? JSP_PROGRESS : JSP);
    }

    @WaitPage(path = JSP_PROGRESS, delay = 1000, refresh = 1000)
    public Resolution export() {
        ExportProcess process = null;

        for (ExportProcess p : exportProcesses) {
            if (p.getName().equals(exportProcessName)) {
                process = p;
                break;
            }
        }
        if (process == null) {
            getContext().getMessages().add(new SimpleMessage("Ongeldig proces"));
            return new ForwardResolution(JSP);
        }

        start = new Date();
        log.info("Start export process: " + process.getName());
        processed = 0;

        // Get berichten
        try {

            exportMutatieBerichten(process.getExportpad());

            if (this.exceptionStacktrace == null) {
                getContext().getMessages().add(new SimpleMessage("Export afgerond."));
            }
        } catch (Throwable t) {
            log.error("Fout bij exporteren", t);
            String m = "Fout bij exporteren: " + ExceptionUtils.getMessage(t);
            if (t.getCause() != null) {
                m += ", oorzaak: " + ExceptionUtils.getRootCauseMessage(t);
            }
            getContext().getMessages().add(new SimpleMessage(m));
        } finally {
            complete = true;
        }
        return new ForwardResolution(JSP_PROGRESS);
    }

    public void exportMutatieBerichten(String locatie) throws Exception {
        int offset = 0;
        int batch = 250;
        final MutableInt processed = new MutableInt(0);
        final DataSource dataSourceStaging = ConfigUtil.getDataSourceStaging();
        final Connection conn = dataSourceStaging.getConnection();
        final GeometryJdbcConverter geomToJdbc = GeometryJdbcConverterFactory.getGeometryJdbcConverter(conn);
        final RowProcessor processor = new StagingRowHandler();

        do {
            log.debug(String.format("Ophalen mutatieberichten batch met offset %d, limit %d", offset, batch));
            String sql = "select * from " + BrmoFramework.BERICHT_TABLE + " where br_orgineel_xml is not null order by id ";
            sql = geomToJdbc.buildPaginationSql(sql, offset, batch);
            log.debug("SQL voor ophalen berichten batch: " + sql);

            processed.setValue(0);
            Exception e = new QueryRunner(geomToJdbc.isPmdKnownBroken()).query(conn, sql, new ResultSetHandler<Exception>() {
                @Override
                public Exception handle(ResultSet rs) throws SQLException {

                    while (rs.next()) {
                        try {
                            Bericht bericht = processor.toBean(rs, Bericht.class);
            
                            //TODO schrijf naar bestand
                            
                        } catch (Exception e) {
                            return e;
                        }
                        processed.increment();
                    }
                    return null;
                }
            });

            offset += processed.intValue();

            progress(offset);

            // If handler threw exception processing row, rethrow it
            if (e != null) {
                throw e;
            }
        } while (processed.intValue() > 0);

    }

}
