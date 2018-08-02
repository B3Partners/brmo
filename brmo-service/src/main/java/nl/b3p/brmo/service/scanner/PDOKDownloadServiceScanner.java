/*
 * Copyright (C) 2018 B3Partners B.V.
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.persistence.Transient;
import nl.b3p.brmo.loader.util.BrmoException;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.ERROR;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.PROCESSING;
import nl.b3p.brmo.persistence.staging.LaadProces;
import nl.b3p.brmo.persistence.staging.PDOKDownloadServiceProces;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.engine.jdbc.StreamUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.stripesstuff.stripersist.Stripersist;

/**
 * <p>Scans the PDOK Download Service API for new available downloads and creates
 * LaadProcessen with DOWNLOAD_AVAILABLE status. The download service provides
 * 31 days back of full and delta downloads (multiple times per day).
 * </p>
 * <p>
 * This can be run automatically when configured for a specific dataset. It looks
 * at existing LaadProcessen to find a full dataset. If a full dataset is not found,
 * it will create a LaadProces for the latest full dataset. Otherwise it will create
 * LaadProcessen for the deltaId's since the last full dataset to update it. If
 * an "update-gap" is detected, a new full dataset download LaadProces is created.
 * With an update gap the currently data tables must be truncated and fully
 * repopulated.
 * </p>
 * <p>
 * Note that this scanner by default does not grab all updates as the service
 * currently only is testing for a dataset which provides complete object history
 * in a full download. Not all possible datasets do this and if the service is
 * reused for those the scanner could be configured to instead grab the first full
 * download and the most possible delta's if as much history is required.
 * </p>
 * <p>
 * For testing of applying deltas, the scanner can be configured to start with a
 * full download at a specific deltaId.
 * </p>
 *
 * @author matthijsln
 */
public class PDOKDownloadServiceScanner extends AbstractExecutableProces {

    private final Log log;

    private final PDOKDownloadServiceProces config;

    @Transient
    private ProgressUpdateListener listener;

    @Transient
    private SSLContext context;

    public PDOKDownloadServiceScanner(PDOKDownloadServiceProces config) {
        this.config = config;

        // Create custom logger path with dataset name, to be able to split
        // to separate log files per dataset

        log = LogFactory.getLog("nl.b3p.brmo.service.scanner.pdok." + config.getDataset());
        log.info("init " + config.toString());
    }

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
            }

            @Override
            public void updateStatus(String status) {
            }

            @Override
            public void addLog(String l) {
            }
        });
    }

    private void info(String l) {
        this.listener.addLog(l);
        log.info(l);
    }

    @Override
    public void execute(ProgressUpdateListener listener) {
        this.listener = listener;
        config.setStatus(PROCESSING);
        config.setLastrun(new Date());
        Stripersist.getEntityManager().merge(config);
        Stripersist.getEntityManager().flush();

        try {
            if(PDOKDownloadServiceProces.MODE_CHECK_DOWNLOADS.equals(config.getMode())) {
                info("Controleer op nieuwe beschikbare " + config.getDataset() + " downloads bij de PDOK Download Service... ");
                checkDownloads();
            } else {
                info("Controleer of " + config.getDataset() + " datasets gedownload moeten worden van de PDOK Download Service... ");
                download();
            }

        } catch(Exception e) {
            config.setSamenvatting("Er is een fout opgetreden (" + e.getClass() + ": " + e.getMessage() + "), details staan in de logs.");
            config.setStatus(ERROR);
            listener.exception(e);
        }
    }

    private String getFileNamePrefix() {
        return config.getDataset() + "_" + config.getFormat() + "_";
    }

    private void checkDownloads() throws Exception {
        info("Start ophalen met deltaIds van " + config.getPDOKServiceURL());

        // Get all deltaIds always - do not send since deltaId as this is useless
        // This API call is fast and always gives a small response
        JSONObject response = getDeltaIds(config);

        if(!response.has("deltaIds") || response.getJSONArray("deltaIds").length() == 0) {
            throw new Exception("Geen deltaIds gevonden!");
        }
        JSONArray deltaIds = response.getJSONArray("deltaIds");
        info("Aantal deltaIds: " + deltaIds.length() + ", laatste: " + deltaIds.getString(deltaIds.length()-1));

        // Look at current laadprocessen
        info("Controleer bestaande laadprocessen...");
        List<LaadProces> lps = Stripersist.getEntityManager().createQuery("from LaadProces where bestand_naam like '%/" + getFileNamePrefix() + "%.zip' order by bestand_datum").getResultList();

        LaadProces latestFull = null;
        LaadProces latestDelta = null;
        for(LaadProces lp: lps) {
            log.debug(String.format("Gevonden laadproces van %tF %tT: %s", lp.getBestand_datum(), lp.getBestand_datum(), lp.getBestand_naam()));
            if(lp.getBestand_naam().contains(getFileNamePrefix() + "full_")) {
                latestFull = lp;
                latestDelta = null;
            }
            if(lp.getBestand_naam().contains(getFileNamePrefix() + "delta_")) {
                if(latestFull == null) {
                    info("LaadProces " + lp.getBestand_naam() + " is delta zonder voorgaande volledige stand, negeren!");
                } else {
                    latestDelta = lp;
                }
            }
        }
        if(latestFull == null) {
            info("Nog geen laadproces gevonden met volledige stand");

            String startDeltaId = config.getStartDeltaId();
            int startDeltaIdIndex = -1;
            if(startDeltaId != null) {
                info("Zoek naar deltaId " + startDeltaId + " om mee te beginnen...");
                for(int i = 0; i < deltaIds.length(); i++) {
                    String d = deltaIds.getString(i);
                    if(startDeltaId.equals(d)) {
                        startDeltaIdIndex = i;
                        break;
                    }
                }
                if(startDeltaIdIndex != -1) {
                    info("Gevraagde start deltaId gevonden op positie " + (startDeltaIdIndex+1) + ", aantal delta's beschikbaar: " + (deltaIds.length() - startDeltaIdIndex - 1));

                    maakLaadProces(true, startDeltaId);

                    for(int i = startDeltaIdIndex + 1; i < deltaIds.length(); i++) {
                        maakLaadProces(false, deltaIds.getString(i));
                    }
                } else {
                    info("Gevraagde start deltaId niet gevonden!");
                }
            } else {
                if(config.isGetFirstDelta()) {
                    info("Maak laadproces voor eerst beschikbare stand");
                    if("bgtv3".equals(config.getDataset())) {
                        info("WAARSCHUWING: Voor bgtv3 is eerste stand downloaden onzinnig, in laatste stand is volledige historie aanwezig!");
                    }
                    maakLaadProces(true, deltaIds.getString(0));
                    for(int i = 1; i < deltaIds.length(); i++) {
                        maakLaadProces(false, deltaIds.getString(i));
                    }
                } else {
                    info("Maak laadproces voor laatst beschikbare stand");
                    maakLaadProces(true, deltaIds.getString(deltaIds.length()-1));
                }
            }
        } else {
            info(String.format("Laatste laadproces van volledige stand: %s van %tF %tT", latestFull.getBestand_naam(), latestFull.getBestand_datum(), latestFull.getBestand_datum()));

            String searchFor;
            if(latestDelta == null) {
                searchFor = getDeltaIdFromLaadProces(latestFull);
                info("Geen delta laadproces gevonden sinds laatste stand " + searchFor + ", zoek naar delta downloads na deze stand");

            } else {
                searchFor = getDeltaIdFromLaadProces(latestDelta);
                info("Zoek naar nieuwe delta downloads sinds laatste deltaId " + searchFor);
            }

            int index = -1;
            for(int i = 0; i < deltaIds.length(); i++) {
                if(searchFor.equals(deltaIds.getString(i))) {
                    index = i;
                    break;
                }
            }

            info("Controleer op nieuwe downloads:");

            if(index == -1) {
                info("ID van laatste stand of delta niet gevonden! Mogelijk langer dan 31 dagen geleden, vanwege een update-gat dient laatste volledige stand te worden ingeladen!");
                maakLaadProces(true, deltaIds.getString(deltaIds.length()-1));
            } else {
                info("ID van laatste stand of delta gevonden! Aantal delta downloads beschikbaar: " + (deltaIds.length() - index - 1));
                for(int i = index + 1; i < deltaIds.length(); i++) {
                    maakLaadProces(false, deltaIds.getString(i));
                }
            }
        }
        Stripersist.getEntityManager().getTransaction().commit();
    }

    private String getDeltaIdFromLaadProces(LaadProces lp) {
        String s = lp.getBestand_naam();
        int i = s.indexOf(getFileNamePrefix());
        s = s.substring(i + getFileNamePrefix().length());
        // remove full_ or delta_
        i = s.indexOf("_");
        s = s.substring(i + 1);
        // remove .zip
        return s.substring(0, s.length() - 4);
    }

    private void maakLaadProces(boolean full, String deltaId) {
        LaadProces lp = new LaadProces();
        lp.setBestand_datum(new Date());
        lp.setBestand_naam(config.getDownloadDirectory() + File.separator + getFileNamePrefix() + (full ? "full_" : "delta_") + deltaId + ".zip");
        lp.setOpmerking("Download beschikbaar");
        lp.setSoort(config.getDataset());
        lp.setStatus(LaadProces.STATUS.DOWNLOAD_AVAILABLE);
        lp.setStatus_datum(new Date());
        Stripersist.getEntityManager().persist(lp);
        info("Laadproces aangemaakt: " + lp.getBestand_naam());
    }

    private void download() {
    }

    private JSONObject getDeltaIds(PDOKDownloadServiceProces config) throws Exception {

        String url = PDOKDownloadServiceProces.PDOK_SERVICE_URL_DEFAULT;
        if(!url.endsWith("/")) {
            url = url + "/";
        }
        url += "api/v2/deltas";

        URLConnection c = getURLConnection(url, config.isSSLValidationEnabled());
        c.connect();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        StreamUtils.copy(c.getInputStream(), bos);
        JSONObject o = new JSONObject(new String(bos.toByteArray(), "UTF-8"));

        return o;
    }

    private SSLContext sslTruster = null;

    private URLConnection getURLConnection(String url, boolean validateSSL) throws MalformedURLException, IOException {
        URLConnection c = new URL(url).openConnection();
        c.setConnectTimeout(30 * 1000);
        c.setReadTimeout(60 * 1000);
        if(!validateSSL && c instanceof HttpsURLConnection) {

            if(sslTruster == null) {
                TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[0];
                        }
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                        }
                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
                };

                try {
                    sslTruster = SSLContext.getInstance("SSL");
                    sslTruster.init(null, trustAllCerts, new java.security.SecureRandom());
                } catch (GeneralSecurityException e) {
                }
            }
            ((HttpsURLConnection)c).setSSLSocketFactory(sslTruster.getSocketFactory());
        }
        return c;
    }
}
