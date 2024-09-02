/*
 * Copyright (C) 2021 B3Partners B.V.
 *
 * SPDX-License-Identifier: MIT
 *
 */

package nl.b3p.brmo.service.scanner;

import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.ERROR;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.PROCESSING;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.WAITING;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import javax.persistence.Transient;
import nl.b3p.brmo.bag2.loader.BAG2Database;
import nl.b3p.brmo.bag2.loader.cli.BAG2DatabaseOptions;
import nl.b3p.brmo.bag2.loader.cli.BAG2LoadOptions;
import nl.b3p.brmo.bag2.loader.cli.BAG2LoaderMain;
import nl.b3p.brmo.bag2.loader.cli.BAG2MutatiesCommand;
import nl.b3p.brmo.bag2.loader.cli.BAG2ProgressOptions;
import nl.b3p.brmo.bgt.loader.ProgressReporter;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.persistence.staging.BAG2MutatieProces;
import nl.b3p.brmo.persistence.staging.ClobElement;
import nl.b3p.brmo.service.util.ConfigUtil;
import nl.b3p.jdbc.util.converter.PGConnectionUnwrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stripesstuff.stripersist.Stripersist;

public class BAG2MutatieProcesRunner extends AbstractExecutableProces {
  private static final Log LOG = LogFactory.getLog(BAG2MutatieProcesRunner.class);
  private final BAG2MutatieProces config;

  @Transient private ProgressUpdateListener listener;

  public BAG2MutatieProcesRunner(BAG2MutatieProces config) {
    this.config = config;
  }

  /**
   * Voert deze taak eenmalig uit.
   *
   * @throws BrmoException als er een fout optreed in het uitvoeren van het proces.
   */
  @Override
  public void execute() throws BrmoException {
    this.execute(
        new ProgressUpdateListener() {
          @Override
          public void total(long total) {}

          @Override
          public void progress(long progress) {}

          @Override
          public void exception(Throwable t) {
            LOG.error(t);
          }

          @Override
          public void updateStatus(String status) {}

          @Override
          public void addLog(String log) {
            LOG.info(log);
            config.updateSamenvattingEnLogfile(log);
            Stripersist.getEntityManager().merge(config);
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
    BAG2LoaderMain.configureLogging(false);
    listener.addLog(String.format("Het BAG2 mutatie proces is gestart op %tc.", new Date()));
    if (config.getStatus().equals(WAITING) || config.getStatus().equals(ERROR)) {
      if (config.getStatus().equals(ERROR)) {
        listener.addLog("Vorige run is met ERROR status afgerond, opnieuw proberen");
      }

      int exitCode = -1;
      BAG2Database bag2Database;
      try (Connection rsgbbagConnection = ConfigUtil.getDataSourceRsgbBag().getConnection()) {

        BAG2DatabaseOptions databaseOptions = new BAG2DatabaseOptions();
        // Niet nodig (rsgbbagConnection wordt gebruikt), maar voor de duidelijkheid
        databaseOptions.setConnectionString(rsgbbagConnection.getMetaData().getURL());
        Connection connection = rsgbbagConnection;
        if (databaseOptions.getConnectionString().startsWith("jdbc:postgresql:")) {
          // Voor gebruik van pgCopy is unwrappen van de connectie nodig.
          // Ook al doet PostGISCopyInsertBatch zelf ook een unwrap, de
          // PGConnectionUnwrapper kan ook Tomcat JNDI
          // connection pool unwrapping aan welke niet met een normale Connection.unwrap()
          // werkt.
          connection = (Connection) PGConnectionUnwrapper.unwrap(rsgbbagConnection);
          databaseOptions.setUsePgCopy(true);
        }
        bag2Database =
            new BAG2Database(databaseOptions, connection) {
              /** connectie niet sluiten; dat doen we later als we helemaal klaar zijn */
              @Override
              public void close() {
                LOG.debug("Had de BAG2 databaseconnectie kunnen sluiten... maar niet gedaan.");
              }
            };

        BAG2LoaderMain main = new BAG2LoaderMain();
        main.setBag2Database(bag2Database);
        BAG2MutatiesCommand mutatiesCommand = new BAG2MutatiesCommand();
        mutatiesCommand.setParent(main);

        // Use defaults
        BAG2LoadOptions loadOptions = new BAG2LoadOptions();
        loadOptions.setGeoFilter(ClobElement.nullSafeGet(config.getConfig().get("geo-filter")));
        BAG2DatabaseOptions dbOptions = new BAG2DatabaseOptions();

        listener.updateStatus(PROCESSING.toString());
        config.setStatus(PROCESSING);
        Stripersist.getEntityManager().merge(config);
        Stripersist.getEntityManager().flush();

        String mode = ClobElement.nullSafeGet(config.getConfig().get("mode"));
        String directory = ClobElement.nullSafeGet(config.getConfig().get("directory"));
        String url = ClobElement.nullSafeGet(config.getConfig().get("url"));
        String kadasterUser = ClobElement.nullSafeGet(config.getConfig().get("kadaster-username"));
        String kadasterPassword =
            ClobElement.nullSafeGet(config.getConfig().get("kadaster-password"));
        String queryParams = ClobElement.nullSafeGet(config.getConfig().get("query"));

        if (!mode.equals("applyFromMirror")) {
          // Let op, zet URL naar die van BAG Bestanden, niet de default publieke mirror
          // voor applyFromMirror modus
          // (alhoewel downloaden/verwerken van de mirror en op andere computer 'load'
          // modus ook zou kunnen, maar dan
          // moet de default value voor de 'url' config niet ingevuld worden als nu)
          url = BAG2MutatiesCommand.LVBAG_BESTANDEN_API_URL;
        }
        if (queryParams == null || queryParams.trim().isEmpty()) {
          // Defaultwaarde voor dagmutaties, anders krijg je ook standen er bij...
          queryParams = "artikelnummers=2529";
        }
        final BAG2ProgressOptions progress = new BAG2ProgressOptions();
        progress.setCustomProgressReporter(
            new ProgressReporter() {
              @Override
              protected void log(String msg) {
                listener.addLog(msg);
              }

              @Override
              protected void status(String msg) {
                listener.updateStatus(msg);
              }
            });

        switch (mode) {
          case "applyFromMirror":
            listener.updateStatus("Verwerken van BAG2 mutatiebestanden...");
            listener.addLog(
                String.format(
                    "Verwerken van BAG2 mutatiebestanden van publieke mirror \"%s\"", url));
            exitCode =
                mutatiesCommand.apply(dbOptions, progress, null, null, url, queryParams, false);
            listener.addLog("Einde verwerken BAG2 bestanden");
            break;
          case "apply":
            listener.updateStatus("Verwerken van BAG2 mutatiebestanden...");
            listener.addLog(
                String.format(
                    "Verwerken van BAG2 mutatiebestanden van BAG Bestanden, gebruikersnaam %s",
                    kadasterUser));
            exitCode =
                mutatiesCommand.apply(
                    dbOptions, progress, kadasterUser, kadasterPassword, url, queryParams, false);
            listener.addLog("Einde verwerken BAG2 bestanden");
            break;
          case "download":
            listener.updateStatus("Downloaden van BAG2 mutatiebestanden...");
            listener.addLog(
                String.format(
                    "Downloaden van BAG2 mutatiebestanden naar directory %s, gebruikersnaam %s",
                    directory, kadasterUser));
            exitCode =
                mutatiesCommand.download(
                    false, kadasterUser, kadasterPassword, url, queryParams, directory, "", false);
            listener.addLog("Einde downloaden BAG2 bestanden");
            break;
          case "load":
            listener.updateStatus("Laden van BAG2 bestanden...");
            listener.addLog("Laden van BAG2 bestanden uit directory " + directory);
            exitCode = main.load(dbOptions, loadOptions, progress, new String[] {directory}, false);
            listener.addLog("Einde laden BAG2 bestanden");
            break;
        }
      } catch (ClassNotFoundException e) {
        LOG.error(e.getLocalizedMessage());
        listener.exception(e);
      } catch (BrmoException | SQLException e) {
        LOG.error("Fout tijdens benaderen BAG2 database", e);
        listener.exception(e);
      } catch (Exception e) {
        LOG.error("Fout tijdens BAG2 proces", e);
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
        listener.addLog("BAG2 mutatieproces afgerond op " + new Date());
        config.setLastrun(new Date());
        Stripersist.getEntityManager().merge(config);
        Stripersist.getEntityManager().flush();
      }
    }
  }
}
