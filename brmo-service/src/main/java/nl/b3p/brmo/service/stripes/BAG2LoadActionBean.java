/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.service.stripes;

import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.Before;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import nl.b3p.brmo.bag2.loader.BAG2Database;
import nl.b3p.brmo.bag2.loader.BAG2ProgressReporter;
import nl.b3p.brmo.bag2.loader.cli.BAG2DatabaseOptions;
import nl.b3p.brmo.bag2.loader.cli.BAG2LoadOptions;
import nl.b3p.brmo.bag2.loader.cli.BAG2LoaderMain;
import nl.b3p.brmo.bag2.schema.BAG2SchemaMapper;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.service.util.ConfigUtil;
import nl.b3p.brmo.sql.dialect.OracleDialect;
import nl.b3p.brmo.sql.dialect.PostGISDialect;
import nl.b3p.jdbc.util.converter.PGConnectionUnwrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stripesstuff.plugin.waitpage.WaitPage;

import javax.sql.DataSource;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class BAG2LoadActionBean implements ActionBean {
    private static final Log LOG = LogFactory.getLog(BAG2LoadActionBean.class);

    private ActionBeanContext context;

    private static final String JSP = "/WEB-INF/jsp/bestand/bag2.jsp";
    private static final String JSP_LOAD = "/WEB-INF/jsp/bestand/bag2load.jsp";

    private String files = "https://service.pdok.nl/kadaster/adressen/atom/v1_0/downloads/lvbag-extract-nl.zip";

    private DataSource rsgbbag;
    private Throwable namingException;
    private boolean connectionOk = false;
    private String connectionString;
    private String databaseName;
    private String databaseUserName;
    private String databaseDialect;
    private Exception connectionException;

    private Date standLoadTechnischeDatum;
    private Date currentTechnischeDatum;
    private Date standLoadTime;

    private boolean loading;
    private Date loadStart;
    private boolean loadResult;
    private Exception loadException;
    private String loadingLog;

    //<editor-fold desc="getters en setters">
    @Override
    public ActionBeanContext getContext() {
        return context;
    }

    @Override
    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    public DataSource getRsgbbag() {
        return rsgbbag;
    }

    public void setRsgbbag(DataSource rsgbbag) {
        this.rsgbbag = rsgbbag;
    }

    public Throwable getNamingException() {
        return namingException;
    }

    public void setNamingException(Throwable namingException) {
        this.namingException = namingException;
    }

    public boolean isConnectionOk() {
        return connectionOk;
    }

    public void setConnectionOk(boolean connectionOk) {
        this.connectionOk = connectionOk;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getDatabaseUserName() {
        return databaseUserName;
    }

    public void setDatabaseUserName(String databaseUserName) {
        this.databaseUserName = databaseUserName;
    }

    public String getDatabaseDialect() {
        return databaseDialect;
    }

    public void setDatabaseDialect(String databaseDialect) {
        this.databaseDialect = databaseDialect;
    }

    public Exception getConnectionException() {
        return connectionException;
    }

    public void setConnectionException(Exception connectionException) {
        this.connectionException = connectionException;
    }

    public Date getStandLoadTechnischeDatum() {
        return standLoadTechnischeDatum;
    }

    public void setStandLoadTechnischeDatum(Date standLoadTechnischeDatum) {
        this.standLoadTechnischeDatum = standLoadTechnischeDatum;
    }

    public Date getCurrentTechnischeDatum() {
        return currentTechnischeDatum;
    }

    public void setCurrentTechnischeDatum(Date currentTechnischeDatum) {
        this.currentTechnischeDatum = currentTechnischeDatum;
    }

    public Date getStandLoadTime() {
        return standLoadTime;
    }

    public void setStandLoadTime(Date standLoadTime) {
        this.standLoadTime = standLoadTime;
    }

    public String getFiles() {
        return files;
    }

    public void setFiles(String files) {
        this.files = files;
    }

    public boolean isLoading() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public boolean isLoadResult() {
        return loadResult;
    }

    public void setLoadResult(boolean loadResult) {
        this.loadResult = loadResult;
    }

    public String getLoadingLog() {
        return loadingLog;
    }

    public void setLoadingLog(String loadingLog) {
        this.loadingLog = loadingLog;
    }

    public Date getLoadStart() {
        return loadStart;
    }

    public void setLoadStart(Date loadStart) {
        this.loadStart = loadStart;
    }

    public Date getUpdateTime() {
        return new Date();
    }

    public void setUpdateTime(Date updateTime) {
    }

    public Exception getLoadException() {
        return loadException;
    }

    public void setLoadException(Exception loadException) {
        this.loadException = loadException;
    }
    //</editor-fold>

    @Before(on = "form")
    public void checkDatabase() {
        try {
            rsgbbag = ConfigUtil.getDataSourceRsgbBag(false);
        } catch(Exception e) {
            if (e instanceof BrmoException) {
                namingException = e.getCause();
            } else {
                namingException = e;
            }
        }

        if (rsgbbag != null) {
            try (Connection c = rsgbbag.getConnection()) {
                connectionOk = true;
                connectionString = c.getMetaData().getURL();
                databaseName = c.getMetaData().getDatabaseProductVersion();
                if (!databaseName.contains(c.getMetaData().getDatabaseProductName())) {
                    databaseName = c.getMetaData().getDatabaseProductName() + " " + databaseName;
                }
                databaseUserName = c.getMetaData().getUserName();
                BAG2Database bag2Database = new BAG2Database(new BAG2DatabaseOptions(), c);

                if (bag2Database.getDialect() instanceof PostGISDialect) {
                    databaseDialect = "postgis";
                } else if (bag2Database.getDialect() instanceof OracleDialect) {
                    databaseDialect = "oracle";
                } else {
                    throw new IllegalArgumentException("Unknown database dialect");
                }

                String s = bag2Database.getMetadata(BAG2SchemaMapper.Metadata.STAND_LOAD_TECHNISCHE_DATUM);
                if (s != null) {
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    currentTechnischeDatum = df.parse(s);
                    standLoadTechnischeDatum = df.parse(bag2Database.getMetadata(BAG2SchemaMapper.Metadata.STAND_LOAD_TECHNISCHE_DATUM));
                    standLoadTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(bag2Database.getMetadata(BAG2SchemaMapper.Metadata.STAND_LOAD_TIME));
                }

            } catch (Exception e) {
                connectionOk = false;
                connectionException = e;
            }
        }
    }

    @DefaultHandler
    public Resolution form() {
        return new ForwardResolution(JSP);
    }

    @WaitPage(path= JSP_LOAD, delay=1000, refresh=5000)
    public Resolution load() throws Exception {
        loading = true;
        loadStart = new Date();
        try(Connection rsgbBagConnection = ConfigUtil.getDataSourceRsgbBag(true).getConnection()) {
            BAG2DatabaseOptions databaseOptions = new BAG2DatabaseOptions();
            // Niet nodig (rsgbbagConnection wordt gebruikt), maar voor de duidelijkheid
            databaseOptions.setConnectionString(rsgbBagConnection.getMetaData().getURL());
            Connection connection = rsgbBagConnection;
            if (databaseOptions.getConnectionString().startsWith("jdbc:postgresql:")) {
                // Voor gebruik van pgCopy is unwrappen van de connectie nodig.
                // Ook al doet PostGISCopyInsertBatch zelf ook een unwrap, de PGConnectionUnwrapper kan ook Tomcat JNDI
                // connection pool unwrapping aan welke niet met een normale Connection.unwrap() werkt.
                connection = (Connection) PGConnectionUnwrapper.unwrap(rsgbBagConnection);
                databaseOptions.setUsePgCopy(true);
            }
            BAG2Database bag2Database = new BAG2Database(databaseOptions, connection) {
                /**
                 * connectie niet sluiten; dat doen we later als we helemaal klaar zijn
                 */
                @Override
                public void close() {
                    LOG.debug("Had de BAG database connectie kunnen sluiten... maar niet gedaan.");
                }
            };
            BAG2LoaderMain.configureLogging(false);
            BAG2LoaderMain main = new BAG2LoaderMain();
            BAG2LoadOptions loadOptions = new BAG2LoadOptions();
            main.loadFiles(bag2Database, databaseOptions, loadOptions, new BAG2ProgressReporter(), Arrays.stream(files.split("\n")).map(String::trim).toArray(String[]::new), null);
            this.loadResult = true;
        } catch(Exception e) {
            this.loadResult = false;
            this.loadException = e;
            LOG.error("Error loading BAG 2.0", e);
        } finally {
            this.loading = false;
        }
        return new ForwardResolution(JSP_LOAD);
    }
}
