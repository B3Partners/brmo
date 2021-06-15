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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import javax.persistence.Transient;
import javax.sql.DataSource;

import nl.b3p.brmo.loader.entity.LaadProces;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.persistence.staging.AfgifteNummerScannerProces;

import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.ERROR;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.PROCESSING;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.WAITING;

import nl.b3p.brmo.service.util.ConfigUtil;
import nl.b3p.jdbc.util.converter.GeometryJdbcConverter;
import nl.b3p.jdbc.util.converter.GeometryJdbcConverterFactory;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
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
            try (final Connection conn = ds.getConnection()) {
                final GeometryJdbcConverter geomToJdbc = GeometryJdbcConverterFactory.getGeometryJdbcConverter(conn);
                String sql = "select distinct cast(value as varchar(15)) from automatisch_proces_config where config_key = 'gds2_contractnummer'";
                List<String> contractnummers = new QueryRunner(geomToJdbc.isPmdKnownBroken()).query(conn, sql, new ColumnListHandler<>());
                // omdat sql server geen distinct op een 'text' kolom kan doen halen we alles op en doen we distinct + sorteren aan de java kant
                //contractnummers = new ArrayList<>(new HashSet(contractnummers));
                contractnummers.sort(String::compareToIgnoreCase);
                DbUtils.closeQuietly(conn);
                return Collections.unmodifiableList(contractnummers);
            }
        } catch (BrmoException | SQLException | ClassCastException | UnsupportedOperationException | IllegalArgumentException ex) {
            LOG.error("Ophalen contractnummers is mislukt.", ex);
            return Collections.EMPTY_LIST;
        }
    }

    public AfgifteNummerScanner(AfgifteNummerScannerProces config) {
        this.config = config;
    }

    /**
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
                LOG.info(msg);
                listener.updateStatus(msg);
                listener.addLog(msg);
            }

            List<Map<String, Object>> afgiftenummers = getOntbrekendeAfgiftenummers(contractnummer, afgiftenummertype);

            if (!afgiftenummers.isEmpty()) {

                Iterator<Map<String, Object>> records = afgiftenummers.iterator();
                Map<String, Object> rec;

                msg = "Ontbrekende " + afgiftenummertype + "s voor contractnummer: " + contractnummer;
                if (contractnummer == null) {
                    msg = "Ontbrekende " + afgiftenummertype + "s";
                }
                LOG.info(msg);
                listener.addLog(msg);

                long totaalToegevoegd = 0L;
                while (records.hasNext()) {
                    rec = records.next();
                    if (rec.get("eerst_ontbrekend").equals(rec.get("laatst_ontbrekend"))) {
                        msg = afgiftenummertype + " " + rec.get("eerst_ontbrekend") + " ontbreekt (tussen laadproces.id " + rec.get("laatste_aanwezige_id") + " en " + rec.get("eerst_opvolgende_id") + ")";
                    } else {
                        msg = "Meerdere " + afgiftenummertype + "s ontbreken; eerst ontbrekende " + afgiftenummertype + " " + rec.get("eerst_ontbrekend") + ", laatst ontbrekende " + afgiftenummertype + " " + rec.get("laatst_ontbrekend") + " (tussen laadproces.id " + rec.get("laatste_aanwezige_id") + " en " + rec.get("eerst_opvolgende_id") + ")";
                    }
                    LOG.info(msg);
                    listener.addLog(msg);

                    if (this.config.getOntbrekendeAfgiftenummersToevoegen()) {
                        msg = "Toevoegen ontbrekende " + afgiftenummertype + "(s)";
                        LOG.debug(msg);
                        listener.addLog(msg);
                        listener.updateStatus(msg);
                        long aantalToegevoegd = insertOntbrekendeAfgiftenummers(
                                ((Number) rec.get("eerst_ontbrekend")).longValue(),
                                ((Number) rec.get("laatst_ontbrekend")).longValue(),
                                contractnummer, afgiftenummertype
                        );
                        totaalToegevoegd += aantalToegevoegd;
                        msg = aantalToegevoegd + " ontbrekende " + afgiftenummertype + "(s) toegevoegd";
                        LOG.info(msg);
                        listener.addLog(msg);
                        listener.updateStatus(msg);
                    }
                }
                listener.total(totaalToegevoegd);
                msg = totaalToegevoegd + " ontbrekende " + afgiftenummertype + "(s) toegevoegd";
                LOG.info(msg);
                listener.addLog(msg);
                listener.updateStatus(msg);
            }

            msg = "Klaar met bepalen ontbrekende " + afgiftenummertype + "s.";
            LOG.info(msg);
            listener.addLog(msg);
            listener.updateStatus(msg);

            config.setStatus(WAITING);
            config.setLastrun(new Date());
        } catch (BrmoException | SQLException e) {
            config.setStatus(ERROR);
            LOG.error("Fout tijdens scannen voor ontbrekende afgiftenummers van contract nummer: " + contractnummer, e);
            listener.exception(e);
        } finally {
            Stripersist.getEntityManager().merge(config);
        }
    }

    long insertOntbrekendeAfgiftenummers(long eersteNummer, long laatsteNummer, final String contractnummer, final String afgiftenummertype) throws BrmoException {
        final DataSource ds = ConfigUtil.getDataSourceStaging();
        final String sql = "insert into laadproces (status, opmerking, soort, contractafgiftenummer, contractnummer, klantafgiftenummer) values (?,?,?,?,?,?)";
        Number contractafgiftenummer = null;
        Number klantafgiftenummer = null;
        long added = 0L;

        for (long number = eersteNummer; number <= laatsteNummer; number++) {
            switch (afgiftenummertype) {
                case "contractafgiftenummer":
                    contractafgiftenummer = number;
                    break;
                case "klantafgiftenummer":
                default:
                    klantafgiftenummer = number;
                    break;
            }
            
            LOG.debug("toevoegen " + afgiftenummertype + ": " + number);

            try (final Connection conn = ds.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"id"});) {

                final GeometryJdbcConverter geomToJdbc = GeometryJdbcConverterFactory.getGeometryJdbcConverter(conn);
                QueryRunner queryRunner = new QueryRunner(geomToJdbc.isPmdKnownBroken());

                queryRunner.fillStatement(stmt,
                        LaadProces.STATUS.STAGING_MISSING.name(),
                        "Toegevoegd vanwege ontbrekend " + afgiftenummertype,
                        "onbekend",
                        contractafgiftenummer,
                        contractnummer,
                        klantafgiftenummer
                );
                stmt.executeUpdate();
                ResultSetHandler<Number> rsh = new ScalarHandler<>();
                Number lpId = rsh.handle(stmt.getGeneratedKeys());
                added++;
                String msg = String.format("Toegevoegd laadproces voor %s %s (contractnummer %s) heeft id: %s.", afgiftenummertype, number, contractnummer, lpId);
                this.listener.addLog(msg);
                this.listener.progress(added);
                LOG.info(msg);
            } catch (SQLException s) {
                LOG.error(s);
                this.listener.exception(s);
            }
        }
        return added;
    }

    /**
     * @param contractnummer    GDS2 contractnummer
     * @param afgiftenummertype naam van de kolom in de laadproces tabel,
     *                          "contractafgiftenummer" of "klantafgiftenummer", niet {code null}
     * @return lijst met ranges van onbrekende records
     * {@code ["eerst_ontbrekend_nr", laatst_ontbrekend_nr, "laatste_aanwezige_id", "eerst_opvolgende_id"]}
     * @throws BrmoException if any
     * @throws SQLException  if any
     */
    /*package private tbv. unit test*/ List<Map<String, Object>> getOntbrekendeAfgiftenummers(final String contractnummer, final String afgiftenummertype) throws BrmoException, SQLException {
        if (contractnummer == null && afgiftenummertype.equalsIgnoreCase("contractafgiftenummer")) {
            throw new BrmoException("Contractnummer voor bepalen van ontbrekende afgiftenummers ontbreekt.");
        }

        final DataSource ds = ConfigUtil.getDataSourceStaging();
        try (final Connection conn = ds.getConnection()) {
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
                            + "    AND r.contractnummer = ? "
                            + " LEFT JOIN laadproces fr ON"
                            + "    laadproces.contractafgiftenummer < fr.contractafgiftenummer "
                            + "    AND fr.contractnummer = ? "
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
                            + (contractnummer == null ? "" : "    AND r.contractnummer = ? ")
                            + " LEFT JOIN laadproces fr ON"
                            + "    laadproces.klantafgiftenummer < fr.klantafgiftenummer"
                            + (contractnummer == null ? "" : "    AND fr.contractnummer = ? ")
                            + " WHERE"
                            + "    r.klantafgiftenummer IS NULL"
                            + "    AND fr.klantafgiftenummer IS NOT NULL"
                            + (contractnummer == null ? "" : "    AND laadproces.contractnummer = ? ")
                            + " GROUP BY"
                            + "    laadproces.klantafgiftenummer,"
                            + "    r.klantafgiftenummer";
                    break;

            }

            List<Map<String, Object>> afgiftenummers;
            if (contractnummer == null) {
                afgiftenummers = new QueryRunner(geomToJdbc.isPmdKnownBroken()).query(conn, sql, new MapListHandler());
            } else {
                afgiftenummers = new QueryRunner(geomToJdbc.isPmdKnownBroken()).query(conn, sql, new MapListHandler(), contractnummer, contractnummer, contractnummer);
            }

            this.ontbrekendeNummersGevonden = !afgiftenummers.isEmpty();
            LOG.debug("Ontbrekende " + ("".equals(afgiftenummertype) ? "klantafgiftenummer" : afgiftenummertype) + "s voor contractnummer " + contractnummer + ": " + afgiftenummers);

            DbUtils.closeQuietly(conn);
            return afgiftenummers;
        }
    }

    public boolean getOntbrekendeNummersGevonden() {
        return this.ontbrekendeNummersGevonden;
    }
}
