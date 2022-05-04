/*
 * Copyright (C) 2022 B3Partners B.V.
 */
package nl.b3p.brmo.service.jobs;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.EntityManager;
import javax.sql.DataSource;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.nhr.loader.cli.NHRLoadUtils;
import nl.b3p.brmo.nhr.loader.NHRCertificateOptions;
import nl.b3p.brmo.nhr.loader.NHRLoader;
import nl.b3p.brmo.persistence.staging.NHRLaadProces;
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
            certOptions.setKeystorePassword((String) ctx.lookup("java:comp/env/brmo/nhr/keystorePassword"));
            certOptions.setKeystoreAlias("key");
            certOptions.setTruststore((String) ctx.lookup("java:comp/env/brmo/nhr/truststorePath"));
            certOptions.setTruststorePassword((String) ctx.lookup("java:comp/env/brmo/nhr/truststorePassword"));

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
            // Batch requests in groups of 100, to make sure the database doesn't end up too far behind,
            // and the user interface stays roughly up to date.
            Stripersist.requestInit();
            EntityManager entityManager = Stripersist.getEntityManager();
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<NHRLaadProces> cq = cb.createQuery(NHRLaadProces.class);
            Root<NHRLaadProces> from = cq.from(NHRLaadProces.class);
            cq.where(cb.lessThan(from.get("volgendProberen"), cb.currentTimestamp()));
            List<NHRLaadProces> procList = entityManager.createQuery(cq).setMaxResults(100).getResultList();
            if (procList.isEmpty()) break;

            log.info(String.format("processing %d items", procList.size()));

            for (NHRLaadProces process : procList) {
                long fetchStart = Calendar.getInstance().getTimeInMillis();
                try {
                    fetchOne(ds, process.getKvkNummer());

                    if (secondsBetweenFetches == null) {
                        entityManager.remove(process);
                    } else {
                        Calendar time = Calendar.getInstance();
                        time.add(Calendar.SECOND, secondsBetweenFetches);
                        process.setProbeerAantal(0);
                        process.setVolgendProberen(time.getTime());
                        process.setException("");
                        entityManager.merge(process);
                    }
                } catch (Exception e) {
                    totalFetchErrorCount += 1;

                    log.error(String.format("KVK nummer %s ophalen mislukt (%d keer geprobeerd)", process.getKvkNummer(), process.getProbeerAantal()), e);
                    process.setException(e.toString());
                    process.setProbeerAantal(process.getProbeerAantal() + 1);
                    Calendar time = Calendar.getInstance();
                    process.setLaatstGeprobeerd(new Date());
                    // Wait for 30 seconds, then 1 minute, 2 minutes, 4 minutes, ...
                    int secondsUntilNextTry = 30 * (int) Math.pow(2, process.getProbeerAantal() - 1);

                    // Make sure that fetches retry at least every two hours.
                    // (This will happen after 9 retries, or two hours in.)
                    if (secondsUntilNextTry > 7200) {
                        secondsUntilNextTry = 7200;
                    }

                    time.add(Calendar.SECOND, secondsUntilNextTry);
                    process.setVolgendProberen(time.getTime());
                    entityManager.merge(process);
                }

                totalFetchCount += 1;
                averageFetchTime = (averageFetchTime * 9.0f + (Calendar.getInstance().getTimeInMillis() - fetchStart)) / 10.0f;
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
