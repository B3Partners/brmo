/*
 * Copyright (C) 2017 B3Partners B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.b3p;

import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.ProgressUpdateListener;
import nl.b3p.brmo.loader.RsgbProxy;
import nl.b3p.brmo.loader.entity.Bericht;
import nl.b3p.brmo.loader.entity.LaadProces;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.test.util.database.dbunit.CleanUtil;
import nl.b3p.jdbc.util.converter.OracleConnectionUnwrapper;
import nl.b3p.topnl.TopNLType;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.mssql.MsSqlDataTypeFactory;
import org.dbunit.ext.oracle.Oracle10DataTypeFactory;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

/**
  * Draaien met:
 * {@code mvn -Dit.test=TopNLIntegrationTest -Dtest.onlyITs=true verify -Ppostgresql -pl brmo-loader > /tmp/postgresql.log}
 * voor bijvoorbeeld PostgreSQL of
 * {@code mvn -Dit.test=TopNLIntegrationTest -Dtest.onlyITs=true verify -Poracle > target/oracle.log}
 * voor Oracle.
 *
 *
 * @author Mark Prins
 */
// overslaan op mssql; daar is geen topnl ondersteuning, zie ook https://github.com/B3Partners/brmo/issues/773
@Tag("skip-mssql")
public class TopNLIntegrationTest extends AbstractDatabaseIntegrationTest {

    private final static Log LOG = LogFactory.getLog(TopNLIntegrationTest.class);

    static Stream<Arguments> argumentsProvider() {
        return Stream.of(
                // {"bestandType","filename", aantalBerichten, aantalProcessen, "lpGebied"},
                arguments(TopNLType.TOP250NL, "/topnl/TOP250NL.gml", 0, 1, "Nederland"),
                arguments(TopNLType.TOP100NL, "/topnl/Top100NL_000001.gml", 0, 1, "Top100NL blad 000001"),
                arguments(TopNLType.TOP50NL, "/topnl/Top50NL_07W.gml", 0, 1, "Top50NL blad 07W"),
                arguments(TopNLType.TOP10NL, "/topnl/TOP10NL_07W.gml", 0, 1, "TOP10NL blad 07W")
        );
    }

    private final Lock sequential = new ReentrantLock(true);
    private IDatabaseConnection staging;
    private IDatabaseConnection topnl;
    private BrmoFramework brmo;

    @BeforeEach
    public void setUp() throws Exception {
        BasicDataSource dsStaging = new BasicDataSource();
        dsStaging.setUrl(params.getProperty("staging.jdbc.url"));
        dsStaging.setUsername(params.getProperty("staging.user"));
        dsStaging.setPassword(params.getProperty("staging.passwd"));
        dsStaging.setAccessToUnderlyingConnectionAllowed(true);
        staging = new DatabaseDataSourceConnection(dsStaging);

        BasicDataSource dsTopnl = new BasicDataSource();
        dsTopnl.setUrl(params.getProperty("topnl.jdbc.url"));
        dsTopnl.setUsername(params.getProperty("topnl.user"));
        dsTopnl.setPassword(params.getProperty("topnl.passwd"));
        dsTopnl.setAccessToUnderlyingConnectionAllowed(true);
        topnl = new DatabaseDataSourceConnection(dsTopnl);

        brmo = new BrmoFramework(dsStaging, null, null, dsTopnl);

        if (this.isMsSQL) {
            // TODO https://github.com/B3Partners/brmo/issues/773
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MsSqlDataTypeFactory());
            topnl.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new MsSqlDataTypeFactory());
        } else if (this.isOracle) {
            staging = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsStaging.getConnection()), params.getProperty("staging.user").toUpperCase());
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            staging.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);

            topnl = new DatabaseConnection(OracleConnectionUnwrapper.unwrap(dsTopnl.getConnection()), params.getProperty("topnl.user").toUpperCase());
            topnl.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new Oracle10DataTypeFactory());
            topnl.getConfig().setProperty(DatabaseConfig.FEATURE_SKIP_ORACLE_RECYCLEBIN_TABLES, true);
        } else if (this.isPostgis) {
            staging.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
            topnl.getConfig().setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new PostgresqlDataTypeFactory());
        }
        FlatXmlDataSetBuilder fxdb = new FlatXmlDataSetBuilder();
        fxdb.setCaseSensitiveTableNames(false);
        // lege bericht en laadproces tabellen
        IDataSet stagingDataSet = fxdb.build(new FileInputStream(new File(TopNLIntegrationTest.class.getResource("/staging-empty-flat.xml").toURI())));

        sequential.lock();

        DatabaseOperation.CLEAN_INSERT.execute(staging, stagingDataSet);

        assumeTrue(0L == brmo.getCountBerichten(null, null, null, "STAGING_OK"),
                "Er zijn STAGING_OK berichten");
        assumeTrue(0L == brmo.getCountLaadProcessen(null, null, null, "STAGING_OK"),
                "Er zijn STAGING_OK laadprocessen");
    }

    @AfterEach
    public void cleanup() throws Exception {
        brmo.closeBrmoFramework();

        CleanUtil.cleanSTAGING(staging, false);
        staging.close();

        topnl.close();
        try {
            sequential.unlock();
        } catch (IllegalMonitorStateException e) {
            // in geval van niet waar gemaakte assumptions
            LOG.debug("unlock van thread is mislukt, mogelijk niet ge-lock-ed of test overgeslagen.");
        }
    }

    @DisplayName("TopNL in Staging")
    @ParameterizedTest(name = "argumenten #{index}: type: {0}, bestand: {1}, gebied: {4}")
    @MethodSource("argumentsProvider")
    public void loadTopNLInStaging(TopNLType bestandType, String bestandNaam, long aantalBerichten, long aantalProcessen, String lpGebied) throws BrmoException {
        // omdat soms de bestandsnaam wordt aangepast moet dit een harde test fout zijn, dus geen assumeNotNull
        assertNotNull(TopNLIntegrationTest.class.getResource(bestandNaam), "Het test bestand moet er zijn.");

        brmo.loadFromFile(bestandType.getType(), TopNLIntegrationTest.class.getResource(bestandNaam).getFile(), null);

        final List<LaadProces> processen = brmo.listLaadProcessen();
        final List<Bericht> berichten = brmo.listBerichten();

        assertNotNull(berichten, "De verzameling berichten bestaat niet.");
        assertEquals(aantalBerichten, berichten.size(), "Het aantal berichten is niet als verwacht.");
        assertNotNull(processen, "De verzameling processen bestaat niet.");
        assertEquals(aantalProcessen, processen.size(), "Het aantal processen is niet als verwacht.");
        assertEquals(lpGebied, processen.get(0).getGebied(), "Het gebied moet als verwacht zijn.");
    }

    @DisplayName("verwerk TopNL")
    @ParameterizedTest(name = "argumenten rij {index}: type: {0}, bestand: {1}, gebied: {4}")
    @MethodSource("argumentsProvider")
    public void processTopNL(TopNLType bestandType, String bestandNaam, long aantalBerichten, long aantalProcessen, String lpGebied)
            throws Exception {
        // omdat soms de bestandsnaam wordt aangepast moet dit een harde test fout zijn, dus geen assumeNotNull
        assertNotNull(TopNLIntegrationTest.class.getResource(bestandNaam), "Het test bestand moet er zijn.");

        brmo.loadFromFile(bestandType.getType(), TopNLIntegrationTest.class.getResource(bestandNaam).getFile(), null);
        final List<LaadProces> processen = brmo.listLaadProcessen();
        assertEquals(aantalProcessen, processen.size(), "Er is 1 laadproces.");

        List<Long> ids = new ArrayList<>();
        for (TopNLType type : TopNLType.values()) {
            ids.addAll(Arrays.asList(brmo.getLaadProcessenIds("bestand_datum", "ASC", type.getType(), "STAGING_OK")));
        }

        LOG.debug("Te verwerken LP ids:" + ids);
        long[] lpIds = ArrayUtils.toPrimitive(ids.toArray(new Long[ids.size()]));

        Thread t = brmo.toRsgb(RsgbProxy.BerichtSelectMode.BY_LAADPROCES, lpIds, new ProgressUpdateListener() {
            @Override
            public void total(long total) {
                LOG.info("Totaal te verwerken: " + total);
            }

            @Override
            public void progress(long progress) {
                LOG.info("Verwerkt: " + progress);
            }

            @Override
            public void exception(Throwable t) {
                LOG.error(t);
                fail("Fout tijdens verwerken. " + t.getLocalizedMessage());
            }
        });
        t.join();
        assertEquals(LaadProces.STATUS.RSGB_TOPNL_OK.toString(), staging.createDataSet().getTable("laadproces").getValue(0, "status"),
                "Alles OK");
    }

}
