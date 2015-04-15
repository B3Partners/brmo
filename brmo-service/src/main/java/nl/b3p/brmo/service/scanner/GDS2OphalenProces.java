/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.service.scanner;

import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.persistence.staging.Bericht;
import nl.b3p.brmo.persistence.staging.GDS2OphaalProces;
import nl.b3p.brmo.persistence.staging.LaadProces;
import com.sun.xml.ws.developer.JAXWSProperties;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Transient;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.BindingProvider;
import net.sourceforge.stripes.util.Base64;
import nl.b3p.brmo.loader.entity.BrkBericht;
import nl.b3p.brmo.loader.xml.BrkSnapshotXMLReader;
import nl.b3p.brmo.persistence.staging.AutomatischProces;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.LOG_NEWLINE;
import nl.b3p.brmo.persistence.staging.ClobElement;
import nl.b3p.gds2.Main;
import nl.kadaster.schemas.gds2.afgifte_bestandenlijstgbopvragen.v20130701.BestandenlijstGbOpvragenType;
import nl.kadaster.schemas.gds2.afgifte_bestandenlijstgbresultaat.afgifte.v20130701.AfgifteGBType;
import nl.kadaster.schemas.gds2.afgifte_bestandenlijstopvragen.v20130701.BestandenlijstOpvragenType;
import nl.kadaster.schemas.gds2.afgifte_bestandenlijstselectie.v20130701.AfgifteSelectieCriteriaType;
import nl.kadaster.schemas.gds2.afgifte_bestandenlijstselectie.v20130701.BestandKenmerkenType;
import nl.kadaster.schemas.gds2.afgifte_proces.v20130701.FilterDatumTijdType;
import nl.kadaster.schemas.gds2.service.afgifte.v20130701.Gds2AfgifteServiceV20130701;
import nl.kadaster.schemas.gds2.service.afgifte.v20130701.Gds2AfgifteServiceV20130701Service;
import nl.kadaster.schemas.gds2.service.afgifte_bestandenlijstgbopvragen.v20130701.BestandenlijstGBOpvragenRequest;
import nl.kadaster.schemas.gds2.service.afgifte_bestandenlijstgbopvragen.v20130701.BestandenlijstGBOpvragenResponse;
import nl.kadaster.schemas.gds2.service.afgifte_bestandenlijstopvragen.v20130701.BestandenlijstOpvragenRequest;
import nl.logius.digikoppeling.gb._2010._10.DataReference;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stripesstuff.stripersist.Stripersist;

/**
 *
 * @author Matthijs Laan
 */
public class GDS2OphalenProces extends AbstractExecutableProces {

    private static final Log log = LogFactory.getLog(GDS2OphalenProces.class);

    private final GDS2OphaalProces config;

    @Transient
    private ProgressUpdateListener l;

    @Transient
    private SSLContext context;

    private static final String PEM_KEY_START = "-----BEGIN PRIVATE KEY-----";
    private static final String PEM_KEY_END = "-----END PRIVATE KEY-----";
    private static final String PEM_CERT_START = "-----BEGIN CERTIFICATE-----";
    private static final String PEM_CERT_END = "-----END CERTIFICATE-----";

    GDS2OphalenProces(GDS2OphaalProces config) {
        this.config = config;
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
                log.error(t);
            }

            @Override
            public void updateStatus(String status) {
            }

            @Override
            public void addLog(String log) {
            }
        });
    }

    private static PrivateKey getPrivateKeyFromPEM(String pem) throws NoSuchAlgorithmException, InvalidKeySpecException {
        pem = pem.replaceAll("\n", "").trim();
        if (!pem.startsWith(PEM_KEY_START)) {
            throw new IllegalArgumentException("Private key moet beginnen met " + PEM_KEY_START);
        }
        if (!pem.endsWith(PEM_KEY_END)) {
            throw new IllegalArgumentException("Private key moet eindigen met " + PEM_KEY_END);
        }
        pem = pem.replace(PEM_KEY_START, "").replace(PEM_KEY_END, "");

        byte[] decoded = Base64.decode(pem);

        KeyFactory kf = KeyFactory.getInstance("RSA");
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        return kf.generatePrivate(spec);
    }

    private static Certificate getCertificateFromPEM(String pem) throws CertificateException, UnsupportedEncodingException {
        pem = pem.replaceAll("\n", "").trim();
        if (!pem.startsWith(PEM_CERT_START)) {
            throw new IllegalArgumentException("Certificaat moet beginnen met " + PEM_CERT_START);
        }
        if (!pem.endsWith(PEM_CERT_END)) {
            throw new IllegalArgumentException("Certificaat moet eindigen met " + PEM_CERT_END);
        }
        return CertificateFactory.getInstance("X509").generateCertificate(new ByteArrayInputStream(pem.getBytes("US-ASCII")));
    }

    @Override
    public void execute(ProgressUpdateListener listener) {
        this.l = listener;

        try {
            l.updateStatus("Initialiseren...");
            l.addLog(String.format("Initialiseren... %tc", new Date()));
            this.config.setStatus(AutomatischProces.ProcessingStatus.PROCESSING);
            Stripersist.getEntityManager().flush();

            BestandenlijstOpvragenRequest request = new BestandenlijstOpvragenRequest();
            BestandenlijstOpvragenType verzoek = new BestandenlijstOpvragenType();
            request.setVerzoek(verzoek);
            AfgifteSelectieCriteriaType criteria = new AfgifteSelectieCriteriaType();
            verzoek.setAfgifteSelectieCriteria(criteria);

            BestandenlijstGBOpvragenRequest requestGb = new BestandenlijstGBOpvragenRequest();
            BestandenlijstGbOpvragenType verzoekGb = new BestandenlijstGbOpvragenType();
            requestGb.setVerzoek(verzoekGb);
            String contractnummer = this.config.getConfig().get("gds2_contractnummer").getValue();
            criteria.setBestandKenmerken(new BestandKenmerkenType());
            criteria.getBestandKenmerken().setContractnummer(contractnummer);

            String alGerapporteerd = ClobElement.nullSafeGet(this.config.getConfig().get("gds2_al_gerapporteerde_afgiftes"));
            if (!"true".equals(alGerapporteerd)) {
                criteria.setNogNietGerapporteerd(true);
            }

            // datum tijd parsen/instellen
            GregorianCalendar vanaf = getDatumTijd(ClobElement.nullSafeGet(this.config.getConfig().get("vanafdatum")));
            GregorianCalendar tot = getDatumTijd(ClobElement.nullSafeGet(this.config.getConfig().get("totdatum")));
            GregorianCalendar currentDay = null;
            boolean parseblePeriod = false;
            if (vanaf != null && tot != null && vanaf.before(tot)) {
                parseblePeriod = true;
                currentDay = vanaf;
                //Volgens Kadaster werkt dit ook icm periode
                //criteria.setNogNietGerapporteerd(false);
            }

            // Mark denkt: misschien een Set<AfgifteGBType> van maken, dan zijn er geen duplicaten om te verwerken
            List<AfgifteGBType> afgiftesGb = new ArrayList<AfgifteGBType>();

            boolean morePeriods2Process = false;
            do {
                l.addLog("\n*** start periode ***");
                if (parseblePeriod) {
                    FilterDatumTijdType d = new FilterDatumTijdType();
                    d.setDatumTijdVanaf(getXMLDatumTijd(currentDay));
                    l.addLog(String.format("Datum vanaf: %tc", currentDay.getTime()));
                    this.config.addLogLine(String.format("Datum vanaf: %tc", currentDay.getTime()));
                    //maak periode van precies 1 dag door currentDay vooruit te zetten
                    //indien meer dan 2000 gewoon ophalen via hasMore test in antwoord
                    currentDay.add(GregorianCalendar.DAY_OF_MONTH, 1);
                    d.setDatumTijdTot(getXMLDatumTijd(currentDay));
                    l.addLog(String.format("Datum tot: %tc", currentDay.getTime()));
                    this.config.addLogLine(String.format("Datum tot: %tc", currentDay.getTime()));
                     criteria.setPeriode(d);

                    //bereken of er nog meer dagen in de gevraagde periode zitten
                    if (currentDay.before(tot)) {
                        morePeriods2Process = true;
                    } else {
                        morePeriods2Process = false;
                    }
                }

                verzoekGb.setAfgifteSelectieCriteria(criteria);

                Gds2AfgifteServiceV20130701 gds2 = initGDS2();
                l.updateStatus("Uitvoeren SOAP request naar Kadaster...");
                BestandenlijstGBOpvragenResponse responseGb = gds2.bestandenlijstGBOpvragen(requestGb);

                int aantalInAntwoord = responseGb.getAntwoord().getBestandenLijstGB().getAfgifteGB().size();
                l.addLog("Aantal in antwoord: " + aantalInAntwoord);
                this.config.addLogLine("Aantal in antwoord: " + aantalInAntwoord);
                // opletten; in de xsd staat een default value van 'J' voor meerAfgiftesbeschikbaar
                boolean hasMore = responseGb.getAntwoord().getMeerAfgiftesbeschikbaar().equalsIgnoreCase("true");
                l.addLog("Meer afgiftes beschikbaar: " + hasMore);
                this.config.addLogLine("Meer afgiftes beschikbaar: " + hasMore);
 
                afgiftesGb.addAll(responseGb.getAntwoord().getBestandenLijstGB().getAfgifteGB());

                /*
                 Indicatie nog niet gerapporteerd:
                 Met deze indicatie wordt aangegeven of uitsluitend de nog niet
                 gerapporteerde bestanden moeten worden opgenomen in de lijst, of
                 dat alle beschikbare bestanden worden genoemd.
                 Niet gerapporteerd betekent in dit geval ‘niet eerder opgevraagd in
                 deze bestandenlijst’.
                 Als deze indicator wordt gebruikt, dan worden na terugmelding van de
                 bestandenlijst de bijbehorende bestanden gemarkeerd als zijnde
                 ‘gerapporteerd’ in het systeem van GDS.
                 */
                int moreCount = 0;
                String dontGetMoreConfig = ClobElement.nullSafeGet(this.config.getConfig().get("gds2_niet_gerapporteerde_afgiftes_niet_ophalen"));
                while (hasMore && !"true".equals(dontGetMoreConfig)) {
                    l.updateStatus("Uitvoeren SOAP request naar Kadaster voor meer afgiftes..." + moreCount++);
                    criteria.setNogNietGerapporteerd(true);
                    responseGb = gds2.bestandenlijstGBOpvragen(requestGb);

                    List<AfgifteGBType> afgiftes = responseGb.getAntwoord().getBestandenLijstGB().getAfgifteGB();
                    for (AfgifteGBType t : afgiftes) {
                        // lijst urls naar aparte logfile loggen
                        if (t.getDigikoppelingExternalDatareferences() != null
                                && t.getDigikoppelingExternalDatareferences().getDataReference() != null) {
                            for (DataReference dr : t.getDigikoppelingExternalDatareferences().getDataReference()) {
                                log.info("GSD2url: " + dr.getTransport().getLocation().getSenderUrl().getValue());
                                break;
                            }
                        }
                    }
                    afgiftesGb.addAll(afgiftes);
                    aantalInAntwoord = afgiftes.size();
                    l.addLog("Aantal in antwoord: " + aantalInAntwoord);
                    this.config.addLogLine("Aantal in antwoord: " + aantalInAntwoord);
                    hasMore = responseGb.getAntwoord().getMeerAfgiftesbeschikbaar().equalsIgnoreCase("true");
                    l.addLog("Nog meer afgiftes beschikbaar: " + hasMore);
                    this.config.addLogLine("Nog meer afgiftes beschikbaar: " + hasMore);
                }

            } while (morePeriods2Process);

            l.total(afgiftesGb.size());
            config.addLogLine("Totaal aantal opgehaalde berichten: " + afgiftesGb.size());

            verwerkAfgiftes(afgiftesGb);

        } catch (Exception e) {
            log.error("Fout bij ophalen van GDS2 berichten", e);
            this.config.setLastrun(new Date());
            this.config.setSamenvatting("Er is een fout opgetreden, details staan in de logs");
            this.config.setStatus(AutomatischProces.ProcessingStatus.ERROR);
            String m = "Fout bij ophalen van GDS2 berichten: " + ExceptionUtils.getMessage(e);
            if (e.getCause() != null) {
                m += ", oorzaak: " + ExceptionUtils.getRootCauseMessage(e);
            }
            this.config.addLogLine(m);
            l.exception(e);
        } finally {
            if (Stripersist.getEntityManager().getTransaction().getRollbackOnly()) {
                // XXX bij rollback only wordt status niet naar ERROR gezet vanwege
                // rollback, zou in aparte transactie moeten
                Stripersist.getEntityManager().getTransaction().rollback();
            }
        }
    }

    private String getLaadprocesBestansanaam(AfgifteGBType a) {
        return a.getBestand().getBestandsnaam() + " (" + a.getAfgifteID() + ")";
    }

    private boolean isAfgifteAlGeladen(AfgifteGBType a, String url) {
        try {
            Stripersist.getEntityManager().createQuery("select 1 from LaadProces lp where lp.bestand_naam = :n")
                    .setParameter("n", getLaadprocesBestansanaam(a))
                    .getSingleResult();
            return true;
        } catch (NoResultException nre) {
            return false;
        }
    }

    private Bericht laadAfgifte(AfgifteGBType a, String url) throws Exception {
        String msg = "Downloaden " + url;
        l.updateStatus(msg);
        l.addLog(msg);
        this.config.addLogLine(msg);

        HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        int attempt = 0;
        while (true) {
            try {
                URLConnection connection = new URL(url).openConnection();
                InputStream input = (InputStream) connection.getContent();
                IOUtils.copy(input, bos);
                break;
            } catch (Exception e) {
                attempt++;
                if (attempt == 5) {
                    l.addLog("Fout bij laatste poging downloaden afgifte: " + e.getClass().getName() + ": " + e.getMessage());
                    throw e;
                } else {
                    l.addLog("Fout bij poging " + attempt + " om afgifte te downloaden: " + e.getClass().getName() + ": " + e.getMessage());
                    Thread.sleep(1000);
                }
            }
        }

        LaadProces lp = new LaadProces();
        lp.setBestand_naam(getLaadprocesBestansanaam(a));

        if (a.getDigikoppelingExternalDatareferences() != null
                && a.getDigikoppelingExternalDatareferences().getDataReference() != null) {
            for (DataReference dr : a.getDigikoppelingExternalDatareferences().getDataReference()) {
                lp.setBestand_datum(dr.getLifetime().getCreationTime().getValue().toGregorianCalendar().getTime());
                break;
            }
        }
        lp.setSoort("brk");
        lp.setStatus(LaadProces.STATUS.STAGING_OK);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        lp.setOpmerking("GDS2 download van " + url + " op " + sdf.format(new Date()));
        lp.setAutomatischProces(Stripersist.getEntityManager().find(AutomatischProces.class, config.getId()));

        Bericht b = new Bericht();
        b.setLaadprocesid(lp);
        b.setDatum(lp.getBestand_datum());
        b.setSoort("brk");
        b.setStatus(Bericht.STATUS.STAGING_OK);
        b.setStatus_datum(new Date());

        ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(bos.toByteArray()));
        ZipEntry entry = zip.getNextEntry();
        while (entry != null && !entry.getName().toLowerCase().endsWith(".xml")) {
            msg = "Overslaan zip entry geen XML: " + entry.getName();
            l.addLog(msg);
            this.config.addLogLine(msg);
            entry = zip.getNextEntry();
        }
        if (entry == null) {
            msg = "Geen geschikt XML bestand gevonden in zip bestand!";
            l.addLog(msg);
            this.config.addLogLine(msg);
            return null;
        }
        b.setBr_orgineel_xml(IOUtils.toString(zip, "UTF-8"));

        BrkSnapshotXMLReader reader = new BrkSnapshotXMLReader(new ByteArrayInputStream(b.getBr_orgineel_xml().getBytes("UTF-8")));
        BrkBericht bericht = reader.next();
        b.setObject_ref(bericht.getObjectRef());
        b.setBr_xml(bericht.getBrXml());
        b.setVolgordenummer(bericht.getVolgordeNummer());

        Stripersist.getEntityManager().persist(lp);
        Stripersist.getEntityManager().persist(b);
        Stripersist.getEntityManager().getTransaction().commit();
        return b;
    }

    /**
     * Post de xml naar de geconfigureerde url.
     *
     * @param send te versturen
     */
    public static boolean doorsturenBericht(AutomatischProces proces, ProgressUpdateListener l, Bericht b, String endpoint) {
        String msg;
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        try {
            if (endpoint == null || endpoint.length() < 1) {
                return false;
            }
            URL url = new URL(endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Length", "" + b.getBr_orgineel_xml().length());
            conn.setRequestProperty("Content-Type", "application/octet-stream");
            IOUtils.copy(IOUtils.toInputStream(b.getBr_orgineel_xml(), "UTF-8"), conn.getOutputStream());
            conn.disconnect();
            if (conn.getResponseCode() != 200) {
                msg = String.format("HTTP foutcode bij doorsturen bericht %s op %s naar endpoint %s: %s",
                        b.getObject_ref(),
                        sdf.format(new Date()), endpoint, conn.getResponseCode() + ": " + conn.getResponseMessage());
                b.setStatus(Bericht.STATUS.STAGING_NOK);
            } else {
                msg = "Bericht " + b.getObject_ref() + " op " + sdf.format(new Date()) + " doorgestuurd naar " + endpoint;
                b.setStatus(Bericht.STATUS.STAGING_FORWARDED);
            }
            b.setOpmerking(msg);
            proces.addLogLine(msg);
            l.addLog(msg);
            log.info(msg);

            return b.getStatus() == Bericht.STATUS.STAGING_FORWARDED;
        } catch (Exception e) {
            msg = String.format("Fout bij doorsturen bericht op %s naar endpoint %s: %s",
                    sdf.format(new Date()), endpoint, e.getClass() + ": " + e.getMessage());
            b.setOpmerking(msg);
            b.setStatus(Bericht.STATUS.STAGING_NOK);
            proces.addLogLine(msg);
            l.addLog(msg);
            log.error(msg, e);
            return false;
        }
    }

    /**
     * parse datum uit string.
     *
     * @param dateStr datum in dd-MM-yyyy formaat
     * @return datum of null in geval van een parse fout
     */
    private GregorianCalendar getDatumTijd(String dateStr) {
        if (dateStr == null) {
            return null;
        }
        Date date;
        final DateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        if (dateStr.equalsIgnoreCase("nu")) {
            date = new Date();
        } else {
            try {
                date = format.parse(dateStr);
            } catch (ParseException ex) {
                log.error(ex);
                return null;
            }
        }
        
        GregorianCalendar gregory = new GregorianCalendar();
        gregory.setTime(date);

        return gregory;
    }
    
    private XMLGregorianCalendar getXMLDatumTijd(GregorianCalendar gregory) {
        try {

            return DatatypeFactory.newInstance().newXMLGregorianCalendar(gregory);
        } catch (DatatypeConfigurationException ex) {
            log.error(ex);
            return null;
        }
    }

    /**
     * verwerk de afgiftes in de response en stuur eventueel door.
     *
     * @param responseGb
     * @param afgiftesGb
     * @throws Exception
     */
    private void verwerkAfgiftes(List<AfgifteGBType> afgiftesGb) throws Exception {
        int filterAlVerwerkt = 0;
        int aantalGeladen = 0;
        int progress = 0;
        List<Bericht> geladenBerichten = new ArrayList();

        for (AfgifteGBType a : afgiftesGb) {
            String url = null;
            if (a.getDigikoppelingExternalDatareferences() != null
                    && a.getDigikoppelingExternalDatareferences().getDataReference() != null) {
                for (DataReference dr : a.getDigikoppelingExternalDatareferences().getDataReference()) {
                    url = dr.getTransport().getLocation().getSenderUrl().getValue();
                    break;
                }
            }
            if (url != null) {
                if (isAfgifteAlGeladen(a, url)) {
                    filterAlVerwerkt++;
                } else {
                    Bericht b = laadAfgifte(a, url);
                    if (b != null) {
                        geladenBerichten.add(b);
                    }
                    aantalGeladen++;
                }
            }
            l.progress(++progress);
        }

        String doorsturenUrl = ClobElement.nullSafeGet(this.config.getConfig().get("delivery_endpoint"));
        if (doorsturenUrl != null && !geladenBerichten.isEmpty()) {
            for (Bericht b : geladenBerichten) {
                doorsturenBericht(this.config, l, b, doorsturenUrl);
            }
        }

        l.addLog("\n\n**** resultaat ****\n");
        l.addLog("Aantal afgiftes die al waren verwerkt: " + filterAlVerwerkt);
        l.addLog("\nAantal afgiftes geladen: " + aantalGeladen + "\n");

        this.config.updateSamenvattingEnLogfile("Aantal afgiftes die al waren verwerkt: " + filterAlVerwerkt + LOG_NEWLINE
                + "Aantal afgiftes geladen: " + aantalGeladen);

        this.config.setStatus(AutomatischProces.ProcessingStatus.WAITING);
        this.config.setLastrun(new Date());
        Stripersist.getEntityManager().merge(this.config);
        Stripersist.getEntityManager().getTransaction().commit();
    }

    private Gds2AfgifteServiceV20130701 initGDS2() throws Exception {
        Gds2AfgifteServiceV20130701 gds2 = new Gds2AfgifteServiceV20130701Service().getAGds2AfgifteServiceV20130701();
        BindingProvider bp = (BindingProvider) gds2;
        Map<String, Object> ctxt = bp.getRequestContext();
        //ctxt.put(BindingProvider.USERNAME_PROPERTY, username);
        //ctxt.put(BindingProvider.PASSWORD_PROPERTY, password);
        String endpoint = (String) ctxt.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
        l.addLog("Kadaster endpoint: " + endpoint);

        //ctxt.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,  "http://localhost:8088/AfgifteService");
        //l.addLog("Endpoint protocol gewijzigd naar mock");
        l.updateStatus("Laden keys...");
        l.addLog("Loading keystore");
        KeyStore ks = KeyStore.getInstance("jks");
        ks.load(Main.class.getResourceAsStream("/pkioverheid.jks"), "changeit".toCharArray());

        l.addLog("Initializing TrustManagerFactory");
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
        tmf.init(ks);

        l.addLog("Initializing KeyManagerFactory");
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        ks = KeyStore.getInstance("jks");
        char[] thePassword = "changeit".toCharArray();
        PrivateKey privateKey = getPrivateKeyFromPEM(this.config.getConfig().get("gds2_privkey").getValue());
        Certificate certificate = getCertificateFromPEM(this.config.getConfig().get("gds2_pubkey").getValue());
        ks.load(null);
        ks.setKeyEntry("thekey", privateKey, thePassword, new Certificate[]{certificate});
        kmf.init(ks, thePassword);

        l.updateStatus("Opzetten SSL context...");
        l.addLog("Initializing SSLContext");
        this.config.addLogLine("Initializing SSLContext");
        context = SSLContext.getInstance("TLS", "SunJSSE");
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        SSLContext.setDefault(context);
        ctxt.put(JAXWSProperties.SSL_SOCKET_FACTORY, context.getSocketFactory());

        return gds2;
    }
}
