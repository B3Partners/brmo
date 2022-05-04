package nl.b3p.brmo.service.stripes;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.validation.Validate;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.persistence.staging.NHRInschrijving;
import nl.b3p.brmo.service.jobs.NHRJob;
import org.json.JSONArray;
import org.json.JSONObject;
import org.stripesstuff.stripersist.Stripersist;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class NHRActionBean implements ActionBean {
    private ActionBeanContext context;

    private String[] selectedIds;
    @Validate
    private int start;
    @Validate
    private int limit;
    @Validate
    private String sort;
    @Validate
    private String dir;
    @Validate(required = true, on="upload")
    private FileBean file;

    private boolean statusActive;
    private Date statusCertificateExpiry;
    private long statusDaysUntilExpiry;
    private String statusNotification;
    private String statusEndpoint;
    private boolean statusEndpointPreprod;
    private float statusRefetchDays;

    public boolean getStatusActive() {
        return statusActive;
    }
    public Date getStatusCertificateExpiry() {
        return statusCertificateExpiry;
    }
    public long getStatusDaysUntilExpiry() {
        return statusDaysUntilExpiry;
    }
    public String getStatusNotification() {
        return statusNotification;
    }
    public String getStatusEndpoint() {
        return statusEndpoint;
    }
    public boolean getStatusEndpointPreprod() {
        return statusEndpointPreprod;
    }
    public float getStatusRefetchDays() {
        return statusRefetchDays;
    }
    public long getFetchCount() {
        return NHRJob.getFetchCount();
    }
    public long getFetchErrorCount() {
        return NHRJob.getFetchErrorCount();
    }
    public float getSecondsPerFetch() {
        return NHRJob.getAverageFetchTime() / 1000.0f;
    }

    public String getEstimatedTime() {
        float totalSeconds = getSecondsPerFetch() * getPendingCount();

        int hours = (int) (totalSeconds / 3600);
        int minutes = (int) (totalSeconds / 60) % 60;
        int seconds = (int) totalSeconds % 60;

        return String.format("%dh %02dm %02ds", hours, minutes, seconds);
    }

    private void getCertificateStatus() {
            statusNotification = "";
            try {
                statusActive = InitialContext.doLookup("java:comp/env/brmo/nhr/active");
            } catch (NamingException e) {
            } catch (Exception e) {
                statusNotification += String.format("nhr/active incorrect: %s\n", e.getMessage());
            }

            KeyStore keyStore;
            try {
                keyStore = KeyStore.getInstance("PKCS12");
                try (FileInputStream keyStoreFile = new FileInputStream((String) InitialContext.doLookup("java:comp/env/brmo/nhr/keystorePath"))) {
                    keyStore.load(keyStoreFile, ((String)InitialContext.doLookup("java:comp/env/brmo/nhr/keystorePassword")).toCharArray());
                }

                Certificate cert = keyStore.getCertificate("key");
                if (cert == null) {
                    statusNotification += "geen certificaat met alias \"key\" gevonden\n";
                } else {
                    Date now = new Date();
                    statusCertificateExpiry = ((X509Certificate) cert).getNotAfter();
                    statusDaysUntilExpiry = (statusCertificateExpiry.getTime() - now.getTime()) / (60 * 60 * 24);
                    if (statusCertificateExpiry.before(now)) {
                        statusNotification += "certificaat is verlopen!\n";
                    }
                }
            } catch (FileNotFoundException e) {
                statusNotification += "keystore niet gevonden\n";
            } catch (NamingException e) {
                if (statusActive) {
                    statusNotification += String.format("geen keystore ingesteld: %s\n", e.getMessage());
                }
            } catch (Exception e) {
                statusNotification += String.format("keystore incorrect: %s\n", e.getMessage());
            }

            try {
                statusEndpoint = InitialContext.doLookup("java:comp/env/brmo/nhr/endpoint");
                statusEndpointPreprod = InitialContext.doLookup("java:comp/env/brmo/nhr/endpointIsPreprod");
            } catch (Exception e) {
                statusNotification += String.format("endpoint incorrect: %s\n", e.getMessage());
            }

            try {
                statusRefetchDays = ((float)(Integer)InitialContext.doLookup("java:comp/env/brmo/nhr/secondsBetweenFetches")) / (60.0f * 60.0f * 24.0f);
            } catch (Exception e) {
                statusNotification += String.format("secondsBetweenFetches incorrect: %s\n", e.getMessage());
            }
    }

    @DefaultHandler
    public Resolution list() {
        getCertificateStatus();
        return new ForwardResolution("/WEB-INF/jsp/nhr/list.jsp");
    }

    private long getCount() {
        EntityManager entityManager = Stripersist.getEntityManager();
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<NHRInschrijving> from = cq.from(NHRInschrijving.class);
        cq.select(cb.count(from));
        return entityManager.createQuery(cq).getSingleResult();
    }


    public long getPendingCount() {
        EntityManager entityManager = Stripersist.getEntityManager();
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<NHRInschrijving> from = cq.from(NHRInschrijving.class);
        cq.select(cb.count(from));
        cq.where(cb.lessThan(from.get("volgendProberen"), cb.currentTimestamp()));
        return entityManager.createQuery(cq).getSingleResult();
    }

    public Resolution getGridData() throws BrmoException {
        EntityManager entityManager = Stripersist.getEntityManager();
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<NHRInschrijving> cq = cb.createQuery(NHRInschrijving.class);
        Root<NHRInschrijving> from = cq.from(NHRInschrijving.class);

        if (sort != null) {
            if (dir == null || !dir.equalsIgnoreCase("desc")) {
                cq.orderBy(cb.asc(from.get(sort)));
            } else {
                cq.orderBy(cb.desc(from.get(sort)));
            }
        }

        if (start < 0) {
            start = 0;
        }

        if (limit <= 0) {
            limit = 40;
        }

        List<NHRInschrijving> procList = entityManager.createQuery(cq).setMaxResults(limit).setFirstResult(start).getResultList();
        JSONArray jsonBerichten = new JSONArray();

        for (NHRInschrijving b : procList) {
            jsonBerichten.put(laadproces2Json(b));
        }

        final JSONObject grid = new JSONObject();
        grid.put("total", getCount());
        grid.put("items", jsonBerichten);

        return new StreamingResolution("application/json") {
            @Override
            public void stream(HttpServletResponse response) throws Exception {
                response.getWriter().print(grid.toString());
            }
        };
    }

    public Resolution getLog() {
        EntityManager entityManager = Stripersist.getEntityManager();

        String responseString;
        if (selectedIds == null || selectedIds.length != 1) {
            responseString = "kan geen logs ophalen voor 0 of meer dan 1 item";
        } else {
            NHRInschrijving item = entityManager.find(NHRInschrijving.class, selectedIds[0]);
            if (item == null) {
                responseString = String.format("Log voor KVK nummer %s niet gevonden", selectedIds[0]);
            } else {
                responseString = item.getException();
            }
        }

        return new StreamingResolution("text/plain") {
            @Override
            public void stream(HttpServletResponse response) throws Exception {
                response.getWriter().print(responseString);
            }
        };
    }

    public Resolution runNow() throws BrmoException {
        EntityManager entityManager = Stripersist.getEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<NHRInschrijving> cq = cb.createQuery(NHRInschrijving.class);
        Root<NHRInschrijving> from = cq.from(NHRInschrijving.class);

        CriteriaBuilder.In<String> in = cb.in(from.get("kvkNummer"));
        for (String item : selectedIds) {
            in.value(item);
        }
        cq.where(in);

        List<NHRInschrijving> procList = entityManager.createQuery(cq).getResultList();
        for (NHRInschrijving b : procList) {
            b.setVolgendProberen(new java.util.Date());
            b.setProbeerAantal(0);
        }

        entityManager.getTransaction().commit();
        return new StreamingResolution("text/plain") {
            @Override
            public void stream(HttpServletResponse response) throws Exception {
                response.getWriter().print("ok");
            }
        };
    }

    public Resolution upload() throws IOException {
        EntityManager entityManager = Stripersist.getEntityManager();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }

                NHRInschrijving proces;
                proces = entityManager.find(NHRInschrijving.class, line);
                if (proces == null) {
                    proces = new NHRInschrijving();
                }

                proces.setDatum(new Date());
                proces.setVolgendProberen(new Date());
                proces.setProbeerAantal(0);
                proces.setKvkNummer(line);
                entityManager.merge(proces);
            }
        }

        file.delete();
        entityManager.getTransaction().commit();
        return list();
    }

    private JSONObject laadproces2Json(NHRInschrijving laadproces) {
        JSONObject json = new JSONObject();

        json.put("datum", laadproces.getDatum());
        json.put("laatstGeprobeerd", laadproces.getLaatstGeprobeerd());
        json.put("volgendProberen", laadproces.getVolgendProberen());
        json.put("probeerAantal", laadproces.getProbeerAantal());
        json.put("kvkNummer", laadproces.getKvkNummer());

        return json;
    }

    public ActionBeanContext getContext() {
        return context;
    }

    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    public String[] getSelectedIds() {
        return selectedIds;
    }

    public void setSelectedIds(String[] selectedIds) {
        this.selectedIds = selectedIds;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public FileBean getFile() {
        return file;
    }

    public void setFile(FileBean file) {
        this.file = file;
    }
}
