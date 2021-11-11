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
import nl.b3p.brmo.sql.dialect.OracleDialect;
import nl.b3p.brmo.sql.dialect.PostGISDialect;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;

public class BAG2LoadActionBean implements ActionBean {
    private ActionBeanContext context;

    private static final String JSP = "/WEB-INF/jsp/bestand/bag2.jsp";

    private String files = "https://service.pdok.nl/kadaster/adressen/atom/v1_0/downloads/lvbag-extract-nl.zip";

    private DataSource rsgbbag;

    private NamingException namingException;

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

    public NamingException getNamingException() {
        return namingException;
    }

    public void setNamingException(NamingException namingException) {
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
            Context initContext = new InitialContext();
            Context context = (Context) initContext.lookup("java:comp/env");
            rsgbbag = (DataSource) context.lookup("jdbc/brmo/rsgbbag");
        } catch(NamingException e) {
            namingException = e;
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
}
