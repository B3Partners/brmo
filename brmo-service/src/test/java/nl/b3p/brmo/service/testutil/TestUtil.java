/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.service.testutil;

import static java.lang.System.getProperty;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.*;

/**
 * @author mprins
 */
public abstract class TestUtil {

  protected static boolean haveSetupJNDI = false;

  private static final Log LOG = LogFactory.getLog(TestUtil.class);

  /**
   * properties uit {@code <DB smaak>.properties} en {@code local.<DB smaak>.properties}.
   *
   * @see #loadDBprop()
   */
  protected static final Properties DBPROPS = new Properties();

  /** {@code true} als we met een Oracle database bezig zijn. */
  protected static boolean isOracle;

  /** {@code true} als we met een Postgis database bezig zijn. */
  protected static boolean isPostgis;

  protected static BasicDataSource dsStaging;
  protected static BasicDataSource dsRsgb;
  protected static BasicDataSource dsRsgbBrk;

  /**
   * test of de database properties zijn aangegeven, zo niet dan skippen we alle tests in deze test.
   */
  @BeforeAll
  public static void checkDatabaseIsProvided() {
    assumeFalse(
        getProperty("database.properties.file") == null,
        "Verwacht database omgeving te zijn aangegeven.");
  }

  /**
   * subklassen dienen zelf een setup te hebben; vanwege de overerving gaat deze methode af na de
   * {@code @Before} methoden van de superklasse.
   *
   * @throws Exception if any
   */
  @BeforeEach
  public abstract void setUp() throws Exception;

  /**
   * initialize database props using the environment provided file.
   *
   * @throws java.io.IOException if loading the property file fails
   */
  @BeforeAll
  public static void loadDBprop() throws IOException {
    LOG.info("Loading database properties");
    // the `database.properties.file`  is set in the pom.xml or using the commandline
    DBPROPS.load(
        TestUtil.class
            .getClassLoader()
            .getResourceAsStream(System.getProperty("database.properties.file")));
    try {
      // see if a local version exists and use that to override
      DBPROPS.load(
          TestUtil.class
              .getClassLoader()
              .getResourceAsStream("local." + System.getProperty("database.properties.file")));
    } catch (IOException | NullPointerException e) {
      // ignore this
    }

    isOracle = "oracle".equalsIgnoreCase(DBPROPS.getProperty("dbtype"));
    isPostgis = "postgis".equalsIgnoreCase(DBPROPS.getProperty("dbtype"));

    try {
      Class.forName(DBPROPS.getProperty("jdbc.driverClassName"));
    } catch (ClassNotFoundException ex) {
      LOG.error("Database driver niet gevonden.", ex);
    }

    dsStaging = new BasicDataSource();
    dsStaging.setUrl(DBPROPS.getProperty("staging.url"));
    dsStaging.setUsername(DBPROPS.getProperty("staging.username"));
    dsStaging.setPassword(DBPROPS.getProperty("staging.password"));
    dsStaging.setDefaultSchema(DBPROPS.getProperty("staging.schema"));
    dsStaging.setAccessToUnderlyingConnectionAllowed(true);
    dsStaging.setInitialSize(5);
    dsStaging.setMaxTotal(100);
    dsStaging.setMaxIdle(1);
    dsStaging.setMaxConn(Duration.of(1000 * 120, ChronoUnit.MILLIS));
    dsStaging.setMinEvictableIdle(Duration.of(1000 * 10, ChronoUnit.MILLIS));
    dsStaging.setDurationBetweenEvictionRuns(Duration.of(1000 * 30, ChronoUnit.MILLIS));

    dsRsgb = new BasicDataSource();
    dsRsgb.setUrl(DBPROPS.getProperty("rsgb.url"));
    dsRsgb.setUsername(DBPROPS.getProperty("rsgb.username"));
    dsRsgb.setPassword(DBPROPS.getProperty("rsgb.password"));
    dsRsgb.setAccessToUnderlyingConnectionAllowed(true);
    dsRsgb.setInitialSize(1);
    dsRsgb.setMaxTotal(50);
    dsRsgb.setMaxIdle(1);
    dsRsgb.setMaxConn(Duration.of(1000 * 60, ChronoUnit.MILLIS));
    dsRsgb.setMinEvictableIdle(Duration.of(1000 * 10, ChronoUnit.MILLIS));
    dsRsgb.setPoolPreparedStatements(true);
    dsRsgb.setClearStatementPoolOnReturn(true);

    dsRsgbBrk = new BasicDataSource();
    dsRsgbBrk.setUrl(DBPROPS.getProperty("rsgbbrk.url"));
    dsRsgbBrk.setUsername(DBPROPS.getProperty("rsgbbrk.username"));
    dsRsgbBrk.setPassword(DBPROPS.getProperty("rsgbbrk.password"));
    dsRsgbBrk.setAccessToUnderlyingConnectionAllowed(true);
    dsRsgbBrk.setInitialSize(1);
    dsRsgbBrk.setMaxTotal(50);
    dsRsgbBrk.setMaxIdle(1);
    dsRsgbBrk.setMaxConn(Duration.of(1000 * 60, ChronoUnit.MILLIS));
    dsRsgbBrk.setMinEvictableIdle(Duration.of(1000 * 10, ChronoUnit.MILLIS));
    dsRsgbBrk.setPoolPreparedStatements(true);
    dsRsgbBrk.setClearStatementPoolOnReturn(true);

    setupJNDI();
  }

  /** Log de naam van de test als deze begint. */
  @BeforeEach
  public void startTest(TestInfo testInfo) {
    LOG.info("==== Start test methode: " + testInfo.getDisplayName());
  }

  /** Log de naam van de test als deze eindigt. */
  @AfterEach
  public void endTest(TestInfo testInfo) {
    LOG.info("==== Einde test methode: " + testInfo.getDisplayName());
  }

  @AfterAll
  public static void closeConnections() throws SQLException {
    // JNDI connectie pools niet sluiten!
    try {
      InitialContext ic = new InitialContext();
      ic.unbind("java:comp/env/jdbc/brmo/rsgb");
      ic.unbind("java:comp/env/jdbc/brmo/rsgbbrk");
      ic.unbind("java:comp/env/jdbc/brmo/staging");
      ic.destroySubcontext("java:comp/env/jdbc/brmo");
      ic.destroySubcontext("java:comp/env/jdbc");
      ic.destroySubcontext("java:comp/env");
      ic.destroySubcontext("java:comp");
      ic.destroySubcontext("java:");
    } catch (NamingException ex) {
      LOG.warn("Opruimen van jndi datasources is mislukt: " + ex.getLocalizedMessage());
      LOG.trace("Opruimen van jndi datasources is mislukt:", ex);
    } finally {
      haveSetupJNDI = false;
    }
  }

  /** setup jndi voor testcases. */
  protected static void setupJNDI() {
    System.setProperty(
        Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
    System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");

    if (!haveSetupJNDI) {
      try {
        InitialContext ic = new InitialContext();
        ic.createSubcontext("java:");
        ic.createSubcontext("java:comp");
        ic.createSubcontext("java:comp/env");
        ic.createSubcontext("java:comp/env/jdbc");
        ic.createSubcontext("java:comp/env/jdbc/brmo");
        ic.bind("java:comp/env/jdbc/brmo/rsgb", dsRsgb);
        ic.bind("java:comp/env/jdbc/brmo/rsgbbrk", dsRsgbBrk);
        ic.bind("java:comp/env/jdbc/brmo/staging", dsStaging);
      } catch (NamingException ex) {
        LOG.warn("Opzetten van jndi datasources is mislukt: " + ex.getLocalizedMessage());
        LOG.trace("Opzetten van jndi datasources is mislukt:", ex);
      } finally {
        haveSetupJNDI = true;
      }
    }
  }
}
