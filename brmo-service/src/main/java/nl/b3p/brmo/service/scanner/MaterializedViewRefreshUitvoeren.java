/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.service.scanner;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import javax.persistence.Transient;
import javax.sql.DataSource;
import nl.b3p.brmo.loader.util.BrmoException;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.ERROR;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.PROCESSING;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.WAITING;
import nl.b3p.brmo.persistence.staging.MaterializedViewRefresh;
import nl.b3p.brmo.service.util.ConfigUtil;
import nl.b3p.loader.jdbc.GeometryJdbcConverter;
import nl.b3p.loader.jdbc.GeometryJdbcConverterFactory;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.stripesstuff.stripersist.Stripersist;

/**
 * Proces om materilzed views te verversen.
 *
 * @author mprins
 */
public class MaterializedViewRefreshUitvoeren extends AbstractExecutableProces {

    private static final Log LOG = LogFactory.getLog(MaterializedViewRefreshUitvoeren.class);

    private final MaterializedViewRefresh config;

    @Transient
    private ProgressUpdateListener listener;

    public MaterializedViewRefreshUitvoeren(MaterializedViewRefresh config) {
        this.config = config;
    }

    @Override
    public void execute() throws BrmoException {
        this.execute(new ProgressUpdateListener() {
            @Override
            public void total(long total) {
            }

            @Override
            public void progress(long progress) {
            }

            @Override
            public void exception(Throwable t) {
                LOG.error(t);
            }

            @Override
            public void updateStatus(String status) {
            }

            @Override
            public void addLog(String log) {
            }
        });
    }

    @Override
    public void execute(ProgressUpdateListener listener) {
        this.listener = listener;
        config.setStatus(PROCESSING);
        config.setLastrun(new Date());
        Stripersist.getEntityManager().merge(config);
        Stripersist.getEntityManager().flush();

        String msg = String.format("De materialized view ververser met ID %d is gestart op %tc.", config.getId(), Calendar.getInstance());
        LOG.info(msg);
        listener.updateStatus(msg);
        listener.addLog(msg);

        String mview = "onbekend";
        try {
            mview = this.config.getMView();
            final DataSource ds = ConfigUtil.getDataSourceRsgb();
            final Connection conn = ds.getConnection();
            conn.setAutoCommit(true);
            final GeometryJdbcConverter geomToJdbc = GeometryJdbcConverterFactory.getGeometryJdbcConverter(conn);
            // "update" gebruiken omdat we een oracle stored procedure benaderen
            Object o = new QueryRunner(geomToJdbc.isPmdKnownBroken()).update(conn, geomToJdbc.getMViewRefreshSQL(mview));
            LOG.debug("mview update resultaat: " + o);
            String resultaat = null;
            // oracle geeft 1 terug als resultaat, postgresql geeft 0 terug...
            if (geomToJdbc.getGeotoolsDBTypeName().equalsIgnoreCase("oracle")) {
                resultaat = (o.toString().equals("1") ? "OK" : "NOT OK");
            } else {
                resultaat = (o.toString().equals("0") ? "OK" : "NOT OK");
            }
            DbUtils.closeQuietly(conn);

            msg = String.format("De materialized view %s is ververst met resultaat %s om %tc", mview, resultaat, Calendar.getInstance());
            LOG.info(msg);
            listener.updateStatus(msg);
            listener.addLog(msg);

            config.setStatus(WAITING);
            config.setLastrun(new Date());
        } catch (BrmoException | SQLException e) {
            config.setStatus(ERROR);
            LOG.error("Fout tijdens verversen materialized view: " + mview, e);
            listener.exception(e);
        } finally {
            Stripersist.getEntityManager().merge(config);
        }
    }

    /**
     * Zoek materialized views in rsgb schema.
     *
     * @return lijst met materialized views, de lijst kan leeg zijn
     */
    public static List<String> mviews() {
        try {
            final DataSource ds = ConfigUtil.getDataSourceRsgb();
            final Connection conn = ds.getConnection();
            final GeometryJdbcConverter geomToJdbc = GeometryJdbcConverterFactory.getGeometryJdbcConverter(conn);
            List<String> mviews = new QueryRunner(geomToJdbc.isPmdKnownBroken()).query(conn, geomToJdbc.getMViewsSQL(), new ColumnListHandler<String>());
            mviews.sort(String::compareToIgnoreCase);
            DbUtils.closeQuietly(conn);
            return Collections.unmodifiableList(mviews);
        } catch (BrmoException | SQLException | ClassCastException | UnsupportedOperationException | IllegalArgumentException ex) {
            LOG.error("Ophalen materialized views is mislukt.", ex);
            return Collections.EMPTY_LIST;
        }
    }

}
