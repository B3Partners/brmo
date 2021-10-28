package nl.b3p.brmo.service.scanner;

import nl.b3p.brmo.bgt.loader.BGTDatabase;
import nl.b3p.brmo.bgt.loader.cli.BGTLoaderMain;
import nl.b3p.brmo.bgt.loader.cli.CLIOptions;
import nl.b3p.brmo.bgt.loader.cli.DatabaseOptions;
import nl.b3p.brmo.bgt.loader.cli.DownloadCommand;
import nl.b3p.brmo.bgt.loader.cli.ExtractSelectionOptions;
import nl.b3p.brmo.bgt.loader.cli.FeatureTypeSelectionOptions;
import nl.b3p.brmo.bgt.loader.cli.LoadOptions;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.persistence.staging.BGTLoaderProces;
import nl.b3p.brmo.persistence.staging.ClobElement;
import nl.b3p.brmo.service.util.ConfigUtil;
import nl.b3p.brmo.sql.dialect.MSSQLDialect;
import nl.b3p.brmo.sql.dialect.OracleDialect;
import nl.b3p.brmo.sql.dialect.PostGISDialect;
import nl.b3p.brmo.sql.dialect.SQLDialect;
import nl.b3p.jdbc.util.converter.PGConnectionUnwrapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stripesstuff.stripersist.Stripersist;

import javax.persistence.Transient;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;

import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.ERROR;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.PROCESSING;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.WAITING;

public class BGTLoader extends AbstractExecutableProces {
    private static final Log LOG = LogFactory.getLog(BGTLoader.class);
    private final BGTLoaderProces config;
    @Transient
    private ProgressUpdateListener listener;

    public BGTLoader(BGTLoaderProces config) {
        this.config = config;
    }

    /**
     * Voert deze taak eenmalig uit.
     *
     * @throws BrmoException als er een fout optreed in het uitvoeren van het
     *                       proces.
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
                LOG.info(log);
            }
        });
    }

    /**
     * Voert de taak uit en rapporteert de voortgang.
     *
     * @param listener voortgangs listener
     */
    @Override
    public void execute(ProgressUpdateListener listener) {
        this.listener = listener;
        BGTLoaderMain.configureLogging(false);

        if (config.getStatus().equals(WAITING)) {
            int exitCode = -1;
            BGTDatabase bgtDatabase;
            try (Connection rsgbbgtConnection = ConfigUtil.getDataSourceRsgbBgt().getConnection()) {

                DatabaseOptions databaseOptions = new DatabaseOptions();
                // Zet connection string zodat database dialect bepaald kan worden, maar niet om connectie mee te maken
                databaseOptions.setConnectionString(rsgbbgtConnection.getMetaData().getURL());
                Connection connection = rsgbbgtConnection;
                if (databaseOptions.getConnectionString().startsWith("jdbc:postgresql:")) {
                    // Voor gebruik van pgCopy is unwrappen van de connectie nodig.
                    // Ook al doet PostGISCopyInsertBatch zelf ook een unwrap, de PGConnectionUnwrapper kan ook Tomcat JNDI
                    // connection pool unwrapping aan welke niet met een normale Connection.unwrap() werkt.
                    connection = (Connection) PGConnectionUnwrapper.unwrap(rsgbbgtConnection);
                    databaseOptions.setUsePgCopy(true);
                }
                bgtDatabase = new BGTDatabase(databaseOptions, connection) {
                    /**
                     * connectie niet sluiten; dat doen we later als we helemaal klaar zijn
                     */
                    @Override
                    public void close() {
                        LOG.debug("Had de BGT database kunnen sluiten... maar niet gedaan.");
                    }
                };

                LoadOptions loadOptions = new LoadOptions();
                loadOptions.setIncludeHistory(("true".equals(ClobElement.nullSafeGet(config.getConfig().get("include-history")))));
                loadOptions.setCreateSchema(("true".equals(ClobElement.nullSafeGet(config.getConfig().get("create-schema")))));
                loadOptions.setLinearizeCurves("true".equals(ClobElement.nullSafeGet(config.getConfig().get("linearize-curves"))));

                ExtractSelectionOptions extractSelectionOptions = new ExtractSelectionOptions();
                extractSelectionOptions.setGeoFilterWkt(ClobElement.nullSafeGet(config.getConfig().get("geo-filter")));
                boolean noGeoFilter = StringUtils.isEmpty(ClobElement.nullSafeGet(config.getConfig().get("geo-filter")));
                String fTypes = ClobElement.nullSafeGet(config.getConfig().get("feature-types"));
                extractSelectionOptions.setFeatureTypes(Arrays.asList(fTypes.split(",")));
                DownloadCommand downloadCommand = new DownloadCommand();
                downloadCommand.setBGTDatabase(bgtDatabase);

                listener.updateStatus(PROCESSING.toString());
                config.setStatus(PROCESSING);
                Stripersist.getEntityManager().merge(config);
                Stripersist.getEntityManager().flush();

                if (null == config.getLastrun()) {
                    listener.updateStatus("Ophalen BGT stand...");
                    listener.addLog("Ophalen BGT stand");
                    exitCode = downloadCommand.initial(databaseOptions, loadOptions, extractSelectionOptions, noGeoFilter, null, new CLIOptions(), false);
                    // exitCode 2 = USAGE / config fout
                    listener.updateStatus("Einde ophalen BGT stand");
                    listener.addLog("Einde ophalen BGT stand");
                } else {
                    listener.updateStatus("Ophalen BGT mutaties...");
                    listener.addLog("Ophalen BGT mutaties");
                    exitCode = downloadCommand.update(databaseOptions, new CLIOptions(), null, false, false);
                    listener.updateStatus("Einde ophalen BGT mutaties");
                    listener.addLog("Einde ophalen BGT mutaties");
                }

            } catch (ClassNotFoundException e) {
                LOG.error(e.getLocalizedMessage());
                listener.exception(e);
            } catch (BrmoException | SQLException e) {
                LOG.error("Fout tijdens benaderen BGT database", e);
                listener.exception(e);
            } catch (Exception e) {
                LOG.error("Fout tijdens laden BGT", e);
                listener.exception(e);
            } finally {
                if (exitCode == 0) {
                    config.setStatus(WAITING);
                    config.setLastrun(new Date());
                    listener.updateStatus(WAITING.toString());
                } else {
                    config.setStatus(ERROR);
                    config.setLastrun(new Date());
                    listener.updateStatus(ERROR.toString());
                }
                listener.addLog("BGT laden afgerond");
                config.setLastrun(new Date());
                Stripersist.getEntityManager().merge(config);
                Stripersist.getEntityManager().flush();
            }
        } else if (config.getStatus().equals(ERROR)) {
            listener.addLog("Vorige run is met ERROR status afgerond, kan proces niet starten");
        }
    }
}
