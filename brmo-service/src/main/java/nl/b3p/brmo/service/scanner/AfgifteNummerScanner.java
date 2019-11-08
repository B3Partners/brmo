/*
 * Copyright (C) 2019 B3Partners B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.b3p.brmo.service.scanner;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.persistence.Transient;
import javax.sql.DataSource;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.persistence.staging.AfgifteNummerScannerProces;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.ERROR;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.PROCESSING;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.WAITING;
import nl.b3p.brmo.service.util.ConfigUtil;
import nl.b3p.loader.jdbc.GeometryJdbcConverter;
import nl.b3p.loader.jdbc.GeometryJdbcConverterFactory;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stripesstuff.stripersist.Stripersist;

/**
 * Scan voor ontbrekende klantafgiftenummers voor een bepaald contractnummer in
 * de laadproces tabel.
 *
 * @author mprins
 */
public class AfgifteNummerScanner extends AbstractExecutableProces {

    private static final Log LOG = LogFactory.getLog(AfgifteNummerScanner.class);
    private AfgifteNummerScannerProces config;

    @Transient
    private ProgressUpdateListener listener;

    private boolean ontbrekendeNummersGevonden;

    /**
     * Zoek contractnummers op van de GDS2 jobs.
     *
     * @return lijst met contractnummers, de lijst kan leeg zijn
     */
    public static List<String> contractnummers() {
        try {
            final DataSource ds = ConfigUtil.getDataSourceStaging();
            final Connection conn = ds.getConnection();
            final GeometryJdbcConverter geomToJdbc = GeometryJdbcConverterFactory.getGeometryJdbcConverter(conn);
            String sql = "select distinct value from automatisch_proces_config where config_key = 'gds2_contractnummer'";
            List<String> contractnummers = new QueryRunner(geomToJdbc.isPmdKnownBroken()).query(conn, sql, new ColumnListHandler<>());
            contractnummers.sort(String::compareToIgnoreCase);
            DbUtils.closeQuietly(conn);
            return Collections.unmodifiableList(contractnummers);
        } catch (BrmoException | SQLException | ClassCastException | UnsupportedOperationException | IllegalArgumentException ex) {
            LOG.error("Ophalen contractnummers is mislukt.", ex);
            return Collections.EMPTY_LIST;
        }
    }

    public AfgifteNummerScanner(AfgifteNummerScannerProces config) {
        this.config = config;
    }

    /**
     *
     * @throws BrmoException als...
     */
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

        String msg = String.format("De afgiftenummer scanner met ID %d is gestart op %tc.", config.getId(), Calendar.getInstance());
        LOG.info(msg);
        listener.updateStatus(msg);
        listener.addLog(msg);

        String contractnummer = this.config.getContracNummer();
        String afgiftenummertype = this.config.getAfgifteNummerType();

        try {

            if (afgiftenummertype == null) {
                msg = "Geen afgiftenummertype opgegeven voor opzoeken van ontbrekende afgiftenummers.";
                LOG.warn(msg);
                listener.updateStatus(msg);
                listener.addLog(msg);
                afgiftenummertype = "";
            }

            if (contractnummer == null) {
                msg = "Geen contractnummer opgegeven voor opzoeken van ontbrekende afgiftenummers.";
                LOG.error(msg);
                listener.updateStatus(msg);
                listener.addLog(msg);
            }

            List<Map<String, Object>> afgiftenummers = getOntbrekendeAfgiftenummers(contractnummer, afgiftenummertype);

            if (!afgiftenummers.isEmpty()) {

                Iterator<Map<String, Object>> records = afgiftenummers.iterator();
                Map<String, Object> rec;

                msg = "Ontbrekende " + afgiftenummertype + "s voor contractnummer: " + contractnummer;
                LOG.info(msg);
                listener.addLog(msg);

                while (records.hasNext()) {
                    rec = records.next();

                    if (rec.get("eerst_ontbrekend").equals(rec.get("laatst_ontbrekend"))) {
                        msg = "    " + afgiftenummertype + " " + rec.get("eerst_ontbrekend") + " ontbreekt (tussen laadproces.id " + rec.get("laatste_aanwezige_id") + " en " + rec.get("eerst_opvolgende_id") + ")";
                    } else {
                        msg = "    Meerdere " + afgiftenummertype + "s ontbreken; eerst ontbrekende " + afgiftenummertype + " " + rec.get("eerst_ontbrekend") + ", laatst ontbrekende " + afgiftenummertype + " " + rec.get("laatst_ontbrekend") + " (tussen laadproces.id " + rec.get("laatste_aanwezige_id") + " en " + rec.get("eerst_opvolgende_id") + ")";
                    }
                    LOG.info(msg);
                    listener.addLog(msg);
                }
            }

            msg = "Klaar met bepalen ontbrekende " + afgiftenummertype + "s.";
            LOG.info(msg);
            listener.addLog(msg);
            listener.updateStatus(msg);

            config.setStatus(WAITING);
            config.setLastrun(new Date());
        } catch (BrmoException | SQLException e) {
            config.setStatus(ERROR);
            LOG.error("Fout tijdens scannen voor onbrekende afgiftenummers van contract nummer: " + contractnummer, e);
            listener.exception(e);
        } finally {
            Stripersist.getEntityManager().merge(config);
        }
    }

    /**
     *
     * @param contractnummer GDS2 contractnummer
     * @param afgiftenummertype naam van de kolom in de laadproces tabel,
     * "contractafgiftenummer" of "klantafgiftenummer", niet {code null}
     * @return lijst met ranges van onbrekende records
     * {@code ["eerst_ontbrekend_nr", laatst_ontbrekend_nr, "laatste_aanwezige_id", "eerst_opvolgende_id"]}
     * @throws BrmoException if any
     * @throws SQLException if any
     */
    /*package private tbv. unit test*/ List<Map<String, Object>> getOntbrekendeAfgiftenummers(String contractnummer, String afgiftenummertype) throws BrmoException, SQLException {
        if (contractnummer == null) {
            throw new BrmoException("Contractnummer voor bepalen van ontbrekende afgiftenummers ontbreekt.");
        }

        final DataSource ds = ConfigUtil.getDataSourceStaging();
        try (final Connection conn = ds.getConnection()) {;
            final GeometryJdbcConverter geomToJdbc = GeometryJdbcConverterFactory.getGeometryJdbcConverter(conn);
            final String sql;
            switch (afgiftenummertype) {
                case "contractafgiftenummer":
                    sql = "SELECT"
                            + "    laadproces.contractafgiftenummer + 1 AS eerst_ontbrekend,"
                            + "    MIN(fr.contractafgiftenummer) - 1    AS laatst_ontbrekend,"
                            + "    MIN(laadproces.id)                   AS laatste_aanwezige_id,"
                            + "    MIN(fr.id)                           AS eerst_opvolgende_id"
                            + " FROM"
                            + "    laadproces"
                            + " LEFT JOIN laadproces r ON"
                            + "    laadproces.contractafgiftenummer = r.contractafgiftenummer - 1"
                            + " LEFT JOIN laadproces fr ON"
                            + "    laadproces.contractafgiftenummer < fr.contractafgiftenummer"
                            + " WHERE"
                            + "    r.contractafgiftenummer IS NULL"
                            + "    AND fr.contractafgiftenummer IS NOT NULL"
                            + "    AND laadproces.contractnummer = ? "
                            + " GROUP BY"
                            + "    laadproces.contractafgiftenummer,"
                            + "    r.contractafgiftenummer";
                    break;
                case "klantafgiftenummer":
                default:
                    sql = "SELECT"
                            + "    laadproces.klantafgiftenummer + 1 AS eerst_ontbrekend,"
                            + "    MIN(fr.klantafgiftenummer) - 1    AS laatst_ontbrekend,"
                            + "    MIN(laadproces.id)                AS laatste_aanwezige_id,"
                            + "    MIN(fr.id)                        AS eerst_opvolgende_id"
                            + " FROM"
                            + "    laadproces"
                            + " LEFT JOIN laadproces r ON"
                            + "    laadproces.klantafgiftenummer = r.klantafgiftenummer - 1"
                            + " LEFT JOIN laadproces fr ON"
                            + "    laadproces.klantafgiftenummer < fr.klantafgiftenummer"
                            + " WHERE"
                            + "    r.klantafgiftenummer IS NULL"
                            + "    AND fr.klantafgiftenummer IS NOT NULL"
                            + "    AND laadproces.contractnummer = ? "
                            + " GROUP BY"
                            + "    laadproces.klantafgiftenummer,"
                            + "    r.klantafgiftenummer";
                    break;

            }

            List<Map<String, Object>> afgiftenummers = new QueryRunner(geomToJdbc.isPmdKnownBroken()).query(conn, sql, new MapListHandler(), contractnummer);

            this.ontbrekendeNummersGevonden = !afgiftenummers.isEmpty();
            LOG.debug("Ontbrekende " + afgiftenummertype + "s voor contractnummer " + contractnummer + ": " + afgiftenummers);

            DbUtils.closeQuietly(conn);
            return afgiftenummers;
        }
    }

    public boolean getOntbrekendeNummersGevonden() {
        return this.ontbrekendeNummersGevonden;
    }
}
