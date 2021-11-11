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
import nl.b3p.brmo.bag2.loader.cli.BAG2DatabaseOptions;
import nl.b3p.brmo.bag2.loader.cli.BAG2LoadOptions;
import nl.b3p.brmo.bag2.loader.cli.BAG2LoaderMain;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.service.util.ConfigUtil;
import nl.b3p.brmo.sql.dialect.OracleDialect;
import nl.b3p.brmo.sql.dialect.PostGISDialect;
import nl.b3p.jdbc.util.converter.PGConnectionUnwrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Arrays;

public class BAG2LoadActionBean implements ActionBean {
    private static final Log LOG = LogFactory.getLog(BAG2LoadActionBean.class);

    private ActionBeanContext context;

    private static final String JSP = "/WEB-INF/jsp/bestand/bag2.jsp";
    private static final String JSP_PROGRESS = "/WEB-INF/jsp/bestand/bag2progress.jsp";

    //private String files = "https://service.pdok.nl/kadaster/adressen/atom/v1_0/downloads/lvbag-extract-nl.zip";
    private String files = "https://extracten.bag.kadaster.nl/lvbag/extracten/Gemeente%20LVC/0344/BAGGEM0344L-15102021.zip\n" +
            "https://extracten.bag.kadaster.nl/lvbag/extracten/Gemeente%20LVC/0310/BAGGEM0310L-15102021.zip\n";

    private DataSource rsgbbag;

    private Throwable namingException;

    private boolean connectionOk = false;

    private String connectionString;
    private String databaseName;
    private String databaseUserName;

    private String databaseDialect;

    private Exception connectionException;

    private BAG2Database bag2Database;

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

    public BAG2Database getBag2Database() {
        return bag2Database;
    }

    public void setBag2Database(BAG2Database bag2Database) {
        this.bag2Database = bag2Database;
    }

    public String getFiles() {
        return files;
    }

    public void setFiles(String files) {
        this.files = files;
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
                bag2Database = new BAG2Database(new BAG2DatabaseOptions(), c);

                if (bag2Database.getDialect() instanceof PostGISDialect) {
                    databaseDialect = "postgis";
                } else if (bag2Database.getDialect() instanceof OracleDialect) {
                    databaseDialect = "oracle";
                } else {
                    throw new IllegalArgumentException("Unknown database dialect");
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

    public Resolution load() throws Exception {
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
            bag2Database = new BAG2Database(databaseOptions, connection) {
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
            main.loadFiles(bag2Database, databaseOptions, loadOptions, Arrays.stream(files.split("\n")).map(String::trim).toArray(String[]::new));
        }
        return new ForwardResolution(JSP_PROGRESS);
    }
}
