/*
 * Copyright (C) 2022 B3Partners B.V.
 */
package nl.b3p.brmo.service.jobs;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.sql.DataSource;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.util.BrmoDuplicaatLaadprocesException;
import nl.b3p.brmo.loader.util.BrmoLeegBestandException;
import nl.b3p.brmo.nhr.loader.NHRCertificateOptions;
import nl.b3p.brmo.nhr.loader.NHRException;
import nl.b3p.brmo.nhr.loader.NHRLoader;
import nl.b3p.brmo.nhr.loader.cli.NHRLoadUtils;
import nl.b3p.brmo.persistence.staging.NHRInschrijving;
import nl.b3p.brmo.service.util.ConfigUtil;
import nl.kvk.schemas.schemas.hrip.dataservice._2015._02.Dataservice;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.stripesstuff.stripersist.Stripersist;

@DisallowConcurrentExecution
public class NHRJob implements Job {
  private static final Log log = LogFactory.getLog(NHRJob.class);

  private static float averageFetchTime;
  private static long totalFetchCount;
  private static long totalFetchErrorCount;

  public static float getAverageFetchTime() {
    return averageFetchTime;
  }

  public static long getFetchCount() {
    return totalFetchCount;
  }

  public static long getFetchErrorCount() {
    return totalFetchErrorCount;
  }

  private void fetchOne(Dataservice dataservice, String kvkNummer) throws Exception {
    BrmoFramework brmo = null;
    try {
      DataSource dataSourceStaging = ConfigUtil.getDataSourceStaging();
      brmo = new BrmoFramework(dataSourceStaging, null, null, null);

      brmo.setOrderBerichten(false);

      NHRLoader.sendSingleRequest(dataservice, brmo, kvkNummer, null);
    } finally {
      if (brmo != null) {
        brmo.closeBrmoFramework();
      }
    }
  }

  private Dataservice getDataservice() {
    try {
      InitialContext ctx = new InitialContext();
      Boolean isActive = (Boolean) ctx.lookup("java:comp/env/brmo/nhr/active");
      if (!isActive) {
        return null;
      }

      NHRCertificateOptions certOptions = new NHRCertificateOptions();
      certOptions.setKeystore((String) ctx.lookup("java:comp/env/brmo/nhr/keystorePath"));
      certOptions.setKeystorePassword(
          (String) ctx.lookup("java:comp/env/brmo/nhr/keystorePassword"));
      certOptions.setKeystoreAlias(null);
      certOptions.setTruststore((String) ctx.lookup("java:comp/env/brmo/nhr/truststorePath"));
      certOptions.setTruststorePassword(
          (String) ctx.lookup("java:comp/env/brmo/nhr/truststorePassword"));

      String endpoint = (String) ctx.lookup("java:comp/env/brmo/nhr/endpoint");
      boolean isPreprod = (Boolean) ctx.lookup("java:comp/env/brmo/nhr/endpointIsPreprod");

      return NHRLoadUtils.getDataservice(endpoint, isPreprod, certOptions);
    } catch (Exception e) {
      throw new Error("NHR DataService initializeren mislukt", e);
    }
  }

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    Dataservice ds = getDataservice();
    if (ds == null) {
      return;
    }

    Integer secondsBetweenFetches = null;

    try {
      InitialContext ctx = new InitialContext();

      secondsBetweenFetches = (Integer) ctx.lookup("java:comp/env/brmo/nhr/secondsBetweenFetches");
    } catch (Exception e) {

    }

    while (true) {
      // Batch requests in groups of 20, to make sure the database doesn't end up too far
      // behind,
      // and the user interface stays roughly up to date.
      Stripersist.requestInit();
      EntityManager entityManager = Stripersist.getEntityManager();
      CriteriaBuilder cb = entityManager.getCriteriaBuilder();
      CriteriaQuery<NHRInschrijving> cq = cb.createQuery(NHRInschrijving.class);
      Root<NHRInschrijving> from = cq.from(NHRInschrijving.class);
      cq.where(cb.lessThan(from.get("volgendProberen"), cb.currentTimestamp()));
      List<NHRInschrijving> procList =
          entityManager.createQuery(cq).setMaxResults(20).getResultList();
      if (procList.isEmpty()) break;

      for (NHRInschrijving process : procList) {
        long fetchStart = Calendar.getInstance().getTimeInMillis();
        boolean failed = false;
        Exception exception = null;
        try {
          fetchOne(ds, process.getKvkNummer());
          process.setException("");
        } catch (BrmoDuplicaatLaadprocesException | BrmoLeegBestandException e) {
          // Non-recoverable error, so consider it "successful"
          log.info(String.format("KVK nummer %s ophalen mislukt", process.getKvkNummer()), e);
          exception = e;
        } catch (IllegalStateException e) {
          // We're likely in a weird spot (web app instance just shut down?), let's just
          // fail out of this loop.
          // This will attempt a database flush + retry persistence.
          break;
        } catch (NHRException e) {
          Map<String, String> errors = e.getErrors();
          if (errors.containsKey("IPD0004") || errors.containsKey("IPD0005")) {
            failed = false; // KVK nummer cannot be found. We cannot expect this to work
            // after a retry.
          } else {
            failed = true;
          }
          exception = e;
        } catch (Exception e) {
          failed = true;
          exception = e;
        }

        if (exception != null) {
          process.setException(exception.toString());
        }

        if (failed) {
          log.error(
              String.format(
                  "KVK nummer %s ophalen mislukt (%d keer geprobeerd)",
                  process.getKvkNummer(), process.getProbeerAantal()),
              exception);
          totalFetchErrorCount += 1;
          process.setProbeerAantal(process.getProbeerAantal() + 1);
          Calendar time = Calendar.getInstance();
          process.setLaatstGeprobeerd(new Date());
          // Wait for 30 seconds, then 1 minute, 2 minutes, 4 minutes, ...
          int secondsUntilNextTry =
              30 * (int) Math.pow(2, Math.min(process.getProbeerAantal() - 1, 10));

          // Make sure that fetches retry at least every two hours.
          // (This will happen after 9 retries, or two hours in.)
          if (secondsUntilNextTry > 7200) {
            secondsUntilNextTry = 7200;
          } else if (secondsUntilNextTry < 30) {
            secondsUntilNextTry = 30;
          }

          time.add(Calendar.SECOND, secondsUntilNextTry);
          process.setVolgendProberen(time.getTime());
          entityManager.merge(process);
        } else {
          if (secondsBetweenFetches == null || secondsBetweenFetches == 0) {
            entityManager.remove(process);
          } else {
            Calendar time = Calendar.getInstance();
            time.add(Calendar.SECOND, secondsBetweenFetches);
            process.setProbeerAantal(0);
            process.setVolgendProberen(time.getTime());
            entityManager.merge(process);
          }
        }

        totalFetchCount += 1;
        averageFetchTime =
            (averageFetchTime * 9.0f + (Calendar.getInstance().getTimeInMillis() - fetchStart))
                / 10.0f;
      }

      entityManager.flush();
      if (entityManager.getTransaction().isActive()) {
        entityManager.getTransaction().commit();
        entityManager.getTransaction().begin();
      }
    }

    Stripersist.requestComplete();
  }
}
