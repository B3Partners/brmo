/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.service.scanner;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.persistence.*;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.entity.BrkBericht;
import nl.b3p.brmo.loader.util.BrmoDuplicaatLaadprocesException;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.loader.util.BrmoLeegBestandException;
import nl.b3p.brmo.loader.xml.BrkSnapshotXMLReader;
import nl.b3p.brmo.persistence.staging.AutomatischProces;
import static nl.b3p.brmo.persistence.staging.AutomatischProces.LOG_NEWLINE;
import nl.b3p.brmo.persistence.staging.Bericht;
import nl.b3p.brmo.persistence.staging.ClobElement;
import nl.b3p.brmo.persistence.staging.GDS2OphaalProces;
import nl.b3p.brmo.persistence.staging.LaadProces;
import nl.b3p.brmo.service.util.ConfigUtil;
import nl.b3p.brmo.service.util.TrustManagerDelegate;
import static nl.b3p.gds2.GDS2Util.*;
import nl.b3p.brmo.soap.util.LogMessageHandler;
import nl.kadaster.schemas.gds2.afgifte_bestandenlijstopvragen.v20170401.BestandenlijstOpvragenType;
import nl.kadaster.schemas.gds2.afgifte_bestandenlijstresultaat.afgifte.v20170401.AfgifteType;
import nl.kadaster.schemas.gds2.afgifte_bestandenlijstselectie.v20170401.AfgifteSelectieCriteriaType;
import nl.kadaster.schemas.gds2.afgifte_bestandenlijstselectie.v20170401.BestandKenmerkenType;
import nl.kadaster.schemas.gds2.afgifte_proces.v20170401.FilterDatumTijdType;
import nl.kadaster.schemas.gds2.afgifte_proces.v20170401.KlantAfgiftenummerReeksType;
import nl.kadaster.schemas.gds2.imgds.baseurl.v20170401.BaseURLType;
import nl.kadaster.schemas.gds2.service.afgifte.v20170401.Gds2AfgifteServiceV20170401;
import nl.kadaster.schemas.gds2.service.afgifte.v20170401.Gds2AfgifteServiceV20170401Service;
import nl.kadaster.schemas.gds2.service.afgifte_bestandenlijstopvragen.v20170401.BestandenlijstOpvragenRequest;
import nl.kadaster.schemas.gds2.service.afgifte_bestandenlijstopvragen.v20170401.BestandenlijstOpvragenResponse;
import nl.logius.digikoppeling.gb._2010._10.DataReference;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.commons.io.input.TeeInputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stripesstuff.stripersist.Stripersist;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;

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

    private static final int BESTANDENLIJST_ATTEMPTS = 5;
    private static final int BESTANDENLIJST_RETRY_WAIT = 10000;

    private static final int AFGIFTE_DOWNLOAD_ATTEMPTS = 5;
    private static final int AFGIFTE_DOWNLOAD_RETRY_WAIT = 3000;

    public GDS2OphalenProces(GDS2OphaalProces config) {
        this.config = config;
    }

    public static Log getLog() {
        return log;
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
                GDS2OphalenProces.log.info(log);
            }
        });
    }

    @Override
    public void execute(ProgressUpdateListener listener) {
        this.l = listener;

        try {
            l.updateStatus("Initialiseren...");
            final Date startDate = new Date();
            l.addLog(String.format("Initialiseren GDS2 ophalen proces %d... %tc", config.getId(), startDate));
            this.config.updateSamenvattingEnLogfile(String.format("Het GDS2 ophalen proces is gestart op %tc.", startDate));
            this.config.setStatus(AutomatischProces.ProcessingStatus.PROCESSING);
            this.config.setLastrun(startDate);
            Stripersist.getEntityManager().merge(this.config);
            Stripersist.getEntityManager().flush();

            BestandenlijstOpvragenRequest request = new BestandenlijstOpvragenRequest();
            BestandenlijstOpvragenType verzoek = new BestandenlijstOpvragenType();
            AfgifteSelectieCriteriaType criteria = new AfgifteSelectieCriteriaType();
            request.setVerzoek(verzoek);
            verzoek.setAfgifteSelectieCriteria(criteria);
            criteria.setBestandKenmerken(new BestandKenmerkenType());

            String contractnummer = ClobElement.nullSafeGet(this.config.getConfig().get("gds2_contractnummer"));
            if (contractnummer != null) {
                log.debug("GDS2 contractnummer is: " + contractnummer);
                criteria.getBestandKenmerken().setContractnummer(contractnummer);
            }
            String artikelnummer = ClobElement.nullSafeGet(this.config.getConfig().get("gds2_artikelnummer"));
            if (artikelnummer != null) {
                log.debug("GDS2 artikelnummer is: " + artikelnummer);
                l.addLog("GDS2 artikelnummer is: " + artikelnummer);
                criteria.getBestandKenmerken().setArtikelnummer(artikelnummer);
            }

            String alGerapporteerd = ClobElement.nullSafeGet(this.config.getConfig().get("gds2_al_gerapporteerde_afgiftes"));
            if (!"true".equals(alGerapporteerd)) {
                criteria.setNogNietGerapporteerd(true);
            }

            // datum tijd parsen/instellen
            GregorianCalendar vanaf = null;
            GregorianCalendar tot = getDatumTijd(ClobElement.nullSafeGet(this.config.getConfig().get("totdatum")));
            String sVanaf = ClobElement.nullSafeGet(this.config.getConfig().get("vanafdatum"));
            if (tot != null && ("-1".equals(sVanaf) | "-2".equals(sVanaf) | "-3".equals(sVanaf))) {
                vanaf = getDatumTijd(ClobElement.nullSafeGet(this.config.getConfig().get("totdatum")), Integer.parseInt(sVanaf));
            } else {
                vanaf = getDatumTijd(sVanaf);
            }

            List<AfgifteType> afgiftes = new ArrayList<>();
            BestandenlijstOpvragenResponse response = null;
            int hoogsteKlantAfgifteNummer = -1;

            Gds2AfgifteServiceV20170401 gds2 = initGDS2();
            l.updateStatus("Uitvoeren SOAP request naar Kadaster...");

            boolean afgifteNummerGebaseerdOphalen = false;
            if (vanaf == null && tot == null) {
                //  instellen laagste en hoogste afgiftenummer voor dit verzoek, mag alleen als er geen periode is gedefinieerd
                KlantAfgiftenummerReeksType afgiftenummers = new KlantAfgiftenummerReeksType();
                String nr = ClobElement.nullSafeGet(this.config.getConfig().get("klantafgiftenummer_vanaf"));
                if (StringUtils.isNotBlank(nr)) {
                    BigInteger afgifteNrVanaf = new BigInteger(nr);
                    afgiftenummers.setKlantAfgiftenummerVanaf(afgifteNrVanaf);
                    log.info("Ophalen vanaf klant afgifte nummer: " + afgifteNrVanaf);
                    l.addLog("Ophalen vanaf klant afgifte nummer: " + afgifteNrVanaf);

                    nr = ClobElement.nullSafeGet(this.config.getConfig().get("klantafgiftenummer_totenmet"));
                    if (StringUtils.isNotBlank(nr)) {
                        BigInteger afgifteNrTM = new BigInteger(nr);
                        afgiftenummers.setKlantAfgiftenummerTotmet(afgifteNrTM);
                        log.info("Ophalen tot en met klant afgifte nummer: " + afgifteNrTM);
                        l.addLog("Ophalen tot en met klant afgifte nummer: " + afgifteNrTM);
                    }
                    criteria.setKlantAfgiftenummerReeks(afgiftenummers);
                    afgifteNummerGebaseerdOphalen = true;
                }
            }

            if (afgifteNummerGebaseerdOphalen) {
                // <editor-fold> afgiftenummer-based ophalen
                l.addLog("GDS2 verzoek voor afgiftenummers vanaf: " + criteria.getKlantAfgiftenummerReeks().getKlantAfgiftenummerVanaf());
                l.addLog("GDS2 verzoek voor afgiftenummers tot-en-met: " + criteria.getKlantAfgiftenummerReeks().getKlantAfgiftenummerTotmet());
                response = retryBestandenLijstOpvragen(gds2, request, BESTANDENLIJST_ATTEMPTS, BESTANDENLIJST_RETRY_WAIT);
                hoogsteKlantAfgifteNummer = response.getAntwoord().getKlantAfgiftenummerMax();
                boolean hasMore = response.getAntwoord().isMeerAfgiftesbeschikbaar();
                int aantalInAntwoord = response.getAntwoord().getAfgifteAantalInLijst();
                if (aantalInAntwoord > 0) {
                    afgiftes.addAll(response.getAntwoord().getBestandenLijst().getAfgifte());
                }
                l.addLog("Aantal in antwoord: " + aantalInAntwoord);
                l.addLog("Meer afgiftes beschikbaar: " + hasMore);

                while (hasMore) {
                    criteria.getKlantAfgiftenummerReeks().setKlantAfgiftenummerVanaf(
                            // vanaf ophogen met aantal opgehaald
                            criteria.getKlantAfgiftenummerReeks().getKlantAfgiftenummerVanaf().add(BigInteger.valueOf(aantalInAntwoord))
                    );
                    l.addLog("GDS2 verzoek voor afgiftenummers vanaf: " + criteria.getKlantAfgiftenummerReeks().getKlantAfgiftenummerVanaf());
                    l.addLog("GDS2 verzoek voor afgiftenummers tot-en-met: " + criteria.getKlantAfgiftenummerReeks().getKlantAfgiftenummerTotmet());
                    response = retryBestandenLijstOpvragen(gds2, request, BESTANDENLIJST_ATTEMPTS, BESTANDENLIJST_RETRY_WAIT);
                    hoogsteKlantAfgifteNummer = response.getAntwoord().getKlantAfgiftenummerMax();
                    aantalInAntwoord = response.getAntwoord().getAfgifteAantalInLijst();
                    hasMore = response.getAntwoord().isMeerAfgiftesbeschikbaar();
                    l.addLog("Aantal in antwoord: " + aantalInAntwoord);
                    l.addLog("Meer afgiftes beschikbaar: " + hasMore);

                    if (aantalInAntwoord > 0) {
                        afgiftes.addAll(response.getAntwoord().getBestandenLijst().getAfgifte());
                    }
                }
                // </editor-fold> afgiftenummer-based ophalen
            } else {
                // <editor-fold> time-based ophalen
                GregorianCalendar currentMoment = null;
                boolean parseblePeriod = false;
                int loopType = Calendar.DAY_OF_MONTH;
                int loopMax = 180;
                int loopNum = 0;
                boolean reducePeriod = false;
                boolean increasePeriod = false;

                if (vanaf != null && tot != null && vanaf.before(tot)) {
                    parseblePeriod = true;
                    currentMoment = vanaf;
                }

                boolean morePeriods2Process = false;
                do /* while morePeriods2Process is true */ {
                    l.addLog("\n*** start periode ***");
                    //zet periode in criteria indien gewenst
                    if (parseblePeriod) {
                        //check of de periodeduur verkleind moet worden
                        if (reducePeriod) {
                            switch (loopType) {
                                case Calendar.DAY_OF_MONTH:
                                    currentMoment.add(loopType, -1);
                                    loopType = Calendar.HOUR_OF_DAY;
                                    l.addLog("* Verklein loop periode naar uur");
                                    break;
                                case Calendar.HOUR_OF_DAY:
                                    currentMoment.add(loopType, -1);
                                    loopType = Calendar.MINUTE;
                                    l.addLog("* Verklein loop periode naar minuut");
                                    break;
                                case Calendar.MINUTE:
                                default:
                                    /*
                                     * Hier kom je alleen als binnen een minuut meer
                                     * dan 2000 berichten zijn aangamaakt en het
                                     * vinkje ook "al rapporteerde berichten
                                     * ophalen" staat aan.
                                     */
                                    l.addLog("Niet alle gevraagde berichten zijn opgehaald");
                            }
                            reducePeriod = false;
                        }

                        //check of de periodeduur vergroot moet worden
                        if (increasePeriod) {
                            switch (loopType) {
                                case Calendar.HOUR_OF_DAY:
                                    loopType = Calendar.DAY_OF_MONTH;
                                    l.addLog("* Vergroot loop periode naar dag");
                                    break;
                                case Calendar.MINUTE:
                                    loopType = Calendar.HOUR_OF_DAY;
                                    l.addLog("* Vergroot loop periode naar uur");
                                    break;
                                case Calendar.DAY_OF_MONTH:
                                default:
                                //not possible
                            }
                            increasePeriod = false;
                        }

                        FilterDatumTijdType d = new FilterDatumTijdType();
                        d.setDatumTijdVanaf(getXMLDatumTijd(currentMoment));
                        l.addLog(String.format("Datum vanaf: %tc", currentMoment.getTime()));
                        currentMoment.add(loopType, 1);
                        d.setDatumTijdTotmet(getXMLDatumTijd(currentMoment));
                        l.addLog(String.format("Datum tot: %tc", currentMoment.getTime()));
                        criteria.setPeriode(d);

                        switch (loopType) {
                            case Calendar.HOUR_OF_DAY:
                                //0-23
                                if (currentMoment.get(loopType) == 0) {
                                    increasePeriod = true;
                                }
                                break;
                            case Calendar.MINUTE:
                                //0-59
                                if (currentMoment.get(loopType) == 0) {
                                    increasePeriod = true;
                                }
                                break;
                            case Calendar.DAY_OF_MONTH:
                            default:
                                //alleen dagen tellen, uur en minuut altijd helemaal
                                loopNum++;
                        }

                        //bereken of einde van periode bereikt is
                        if (currentMoment.before(tot) && loopNum < loopMax) {
                            morePeriods2Process = true;
                        } else {
                            morePeriods2Process = false;
                        }
                    }

                    verzoek.setAfgifteSelectieCriteria(criteria);
                    response = retryBestandenLijstOpvragen(gds2, request, BESTANDENLIJST_ATTEMPTS, BESTANDENLIJST_RETRY_WAIT);
                    hoogsteKlantAfgifteNummer = response.getAntwoord().getKlantAfgiftenummerMax();

                    int aantalInAntwoord = response.getAntwoord().getAfgifteAantalInLijst();
                    l.addLog("Aantal in antwoord: " + aantalInAntwoord);
                    // opletten; in de xsd staat een default value van 'J' voor meerAfgiftesbeschikbaar
                    boolean hasMore = response.getAntwoord().isMeerAfgiftesbeschikbaar();
                    l.addLog("Meer afgiftes beschikbaar: " + hasMore);

                    /*
                     * Als "al gerapporteerde berichten" moeten worden opgehaald en
                     * er zitten dan 2000 berichten in het antwoord dan heeft het
                     * geen zin om meer keer de berichten op te halen, je krijgt
                     * telkens dezelfde.
                     */
                    if (hasMore && "true".equals(alGerapporteerd)) {
                        reducePeriod = true;
                        morePeriods2Process = true;
                        increasePeriod = false;
                        // als er geen parsable periode is
                        // (geen periode ingevuld en alGerapporteerd is true
                        // dan moet morePeriods2Process false worden om een
                        // eindloze while loop te voorkomen
                        if (!parseblePeriod) {
                            morePeriods2Process = false;
                        } else {
                            continue;
                        }
                    }

                    if (aantalInAntwoord > 0) {
                        afgiftes.addAll(response.getAntwoord().getBestandenLijst().getAfgifte());
                    }

                    /*
                     * Indicatie nog niet gerapporteerd: Met deze indicatie wordt
                     * aangegeven of uitsluitend de nog niet gerapporteerde
                     * bestanden moeten worden opgenomen in de lijst, of dat alle
                     * beschikbare bestanden worden genoemd. Niet gerapporteerd
                     * betekent in dit geval ‘niet eerder opgevraagd in deze
                     * bestandenlijst’. Als deze indicator wordt gebruikt, dan
                     * worden na terugmelding van de bestandenlijst de bijbehorende
                     * bestanden gemarkeerd als zijnde ‘gerapporteerd’ in het
                     * systeem van GDS.
                     */
                    int moreCount = 0;
                    String dontGetMoreConfig = ClobElement.nullSafeGet(this.config.getConfig().get("gds2_niet_gerapporteerde_afgiftes_niet_ophalen"));

                    while (hasMore && !"true".equals(dontGetMoreConfig)) {
                        l.updateStatus("Uitvoeren SOAP request naar Kadaster voor meer afgiftes..." + moreCount++);
                        criteria.setNogNietGerapporteerd(true);
                        response = retryBestandenLijstOpvragen(gds2, request, BESTANDENLIJST_ATTEMPTS, BESTANDENLIJST_RETRY_WAIT);
                        hoogsteKlantAfgifteNummer = response.getAntwoord().getKlantAfgiftenummerMax();

                        List<AfgifteType> afgifteLijst = response.getAntwoord().getBestandenLijst().getAfgifte();
                        for (AfgifteType t : afgiftes) {
                            // lijst urls naar aparte logfile loggen
                            if (t.getDigikoppelingExternalDatareferences() != null
                                    && t.getDigikoppelingExternalDatareferences().getDataReference() != null) {
                                for (DataReference dr : t.getDigikoppelingExternalDatareferences().getDataReference()) {
                                    log.info("GDS2url te downloaden: " + dr.getTransport().getLocation().getSenderUrl().getValue());
                                    break;
                                }
                            }
                        }
                        afgiftes.addAll(afgifteLijst);
                        aantalInAntwoord = response.getAntwoord().getAfgifteAantalInLijst();
                        l.addLog("Aantal in antwoord: " + aantalInAntwoord);
                        hasMore = response.getAntwoord().isMeerAfgiftesbeschikbaar();
                        l.addLog("Nog meer afgiftes beschikbaar: " + hasMore);
                    }

                } while (morePeriods2Process);
                // </editor-fold> time-based ophalen
            }
            l.total(afgiftes.size());
            l.addLog("Totaal aantal op te halen berichten: " + afgiftes.size());
            l.addLog("Hoogste klant afgifte nummer: " + hoogsteKlantAfgifteNummer);

            this.config.getConfig().put("hoogste_afgiftenummer", new ClobElement("" + hoogsteKlantAfgifteNummer));

            final String soort = this.config.getConfig().getOrDefault("gds2_br_soort", new ClobElement("brk")).getValue();
            switch (soort) {
                case "brk":
                    verwerkAfgiftes(afgiftes, getCertificaatBaseURL(response.getAntwoord()));
                    break;
                case "bag":
                    // bag heeft geen contractnummer
                    verwerkAfgiftes(afgiftes, getAnoniemBaseURL(response.getAntwoord()));
                    break;
                default:
                    throw new BrmoException("Onbekende basisregistratie soort: " + soort);
            }

        } catch (Exception e) {
            log.error("Fout bij ophalen van GDS2 berichten", e);
            this.config.setLastrun(new Date());
            this.config.setSamenvatting("Er is een fout opgetreden, details staan in de logs");
            this.config.setStatus(AutomatischProces.ProcessingStatus.ERROR);
            String m = "Fout bij ophalen van GDS2 berichten: " + ExceptionUtils.getMessage(e);
            if (e.getCause() != null) {
                m += ", oorzaak: " + ExceptionUtils.getRootCauseMessage(e);
            }
            l.exception(e);
        } finally {
            if (Stripersist.getEntityManager().getTransaction().getRollbackOnly()) {
                // XXX bij rollback only wordt status niet naar ERROR gezet vanwege
                // rollback, zou in aparte transactie moeten
                Stripersist.getEntityManager().getTransaction().rollback();
            } else {
                Stripersist.getEntityManager().merge(this.config);
                Stripersist.getEntityManager().getTransaction().commit();
            }
            l.updateStatus("Uitvoeren SOAP request naar Kadaster afgerond");
        }
    }

    private String getLaadprocesBestandsnaam(AfgifteType a) {
        return a.getBestand().getBestandsnaam() + " (" + a.getAfgifteID() + ")";
    }

    private boolean isAfgifteAlGeladen(AfgifteType a, String url) {
        try {
            Stripersist.getEntityManager().createQuery("select 1 from LaadProces lp where lp.bestand_naam = :n")
                    .setParameter("n", getLaadprocesBestandsnaam(a))
                    .getSingleResult();
            return true;
        } catch (NoResultException nre) {
            return false;
        }
    }

    /**
     * Ophalen en verwerken van BAG mutaties van GDS2. Deze zijn anders dan BRK
     * mutaties omdat de data in een geneste zip zit en er een paar andere
     * bestanden inzitten waar we (nog) niks mee doen.
     *
     * @param a de afgifte uit het soap verzoek
     * @param url te downloaden bestand
     * @throws Exception if any
     * @see #laadAfgifte(AfgifteType, String)
     */
    private void laadBagAfgifte(AfgifteType a, String url) throws Exception {
        String msg = "Downloaden " + url;
        l.updateStatus(msg);
        l.addLog(msg);

        InputStream input = null;
        int attempt = 0;
        while (true) {
            try {
                URLConnection connection = new URL(url).openConnection();
                if (connection instanceof HttpsURLConnection) {
                    ((HttpsURLConnection) connection).setSSLSocketFactory(context.getSocketFactory());
                }
                input = (InputStream) connection.getContent();
                if (log.isDebugEnabled()) {
                    // dump zip bestand naar /tmp/ voordat de xml wordt geparsed
                    File dump = File.createTempFile(a.getBestand().getBestandsnaam() + "_id-" + a.getAfgifteID(), ".zip");
                    log.debug("Dump BAG mutaties zip naar: " + dump.getAbsolutePath());
                    FileOutputStream archiveFile = new FileOutputStream(dump);
                    input = new TeeInputStream(input, archiveFile, true);
                }
                break;
            } catch (Exception e) {
                attempt++;
                if (attempt == AFGIFTE_DOWNLOAD_ATTEMPTS) {
                    l.addLog("Fout bij laatste poging downloaden afgifte: " + e.getClass().getName() + ": " + e.getMessage());
                    throw e;
                } else {
                    l.addLog("Fout bij poging " + attempt + " om afgifte te downloaden: " + e.getClass().getName() + ": " + e.getMessage());
                    Thread.sleep(AFGIFTE_DOWNLOAD_RETRY_WAIT);
                    l.addLog("Uitvoeren poging " + (attempt + 1) + " om afgifte " + url + " te downloaden...");
                }
            }
        }

        BrmoFramework brmo = new BrmoFramework(ConfigUtil.getDataSourceStaging(), null);

        ZipInputStream zip = new ZipInputStream(input);
        ZipEntry entry = zip.getNextEntry();
        while (entry != null) {
            log.trace("gevonden " + entry.getName());
            if (!entry.getName().toLowerCase().startsWith("gem-wpl-relatie")
                    && !entry.getName().equalsIgnoreCase("Leveringsdocument-BAG-Mutaties.xml")) {
                if (entry.getName().toLowerCase().endsWith(".zip")) {
                    // alleen mutaties oppakken
                    ZipInputStream innerzip = new ZipInputStream(zip);
                    ZipEntry innerentry = innerzip.getNextEntry();
                    String localLpName = "";
                    while (innerentry != null && innerentry.getName().toLowerCase().endsWith(".xml")) {
                        msg = "Verwerken " + entry.getName() + "/" + innerentry.getName() + " uit " + getLaadprocesBestandsnaam(a);
                        l.updateStatus(msg);
                        l.addLog(msg);
                        log.debug(msg);
                        try {
                            localLpName = getLaadprocesBestandsnaam(a) + "/" + entry.getName() + "/" + innerentry.getName();
                            brmo.loadFromStream(
                                    BrmoFramework.BR_BAG,
                                    CloseShieldInputStream.wrap(innerzip),
                                    localLpName,
                                    config.getId()
                            );
                        } catch (BrmoDuplicaatLaadprocesException d) {
                            msg = "Duplicaat laadproces. " + d.getLocalizedMessage();
                            l.updateStatus(msg);
                            l.addLog(msg);
                            log.warn(msg);
                        } catch (BrmoLeegBestandException e) {
                            msg = "Leeg bestand voor laadproces. " + e.getLocalizedMessage();
                            l.updateStatus(msg);
                            l.addLog(msg);
                            log.info(msg);
                        } finally {
                            brmo.updateLaadProcesMeta(
                                    brmo.getLaadProcesIdByFileName(localLpName),
                                    a.getKlantAfgiftenummer(),
                                    a.getContractAfgiftenummer(),
                                    a.getBestandKenmerken().getArtikelnummer(),
                                    a.getBestandKenmerken().getContractnummer(),
                                    a.getAfgifteID(),
                                    a.getAfgiftereferentie(),
                                    a.getBestand().getBestandsreferentie(),
                                    a.getBeschikbaarTot().toGregorianCalendar().getTime()
                            );
                        }
                        innerentry = innerzip.getNextEntry();
                    }
                } else {
                    log.warn("Overslaan van onbekend bestand in bag mutaties: " + entry.getName());
                }
            }
            entry = zip.getNextEntry();
        }
        zip.close();
        brmo.closeBrmoFramework();
    }

    private Bericht laadAfgifte(AfgifteType a, String url) throws Exception {
        EntityManager em = Stripersist.getEntityManager();
        String msg = "Downloaden " + url;
        l.updateStatus(msg);
        l.addLog(msg);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        int attempt = 0;
        while (true) {
            try {
                URLConnection connection = new URL(url).openConnection();
                if (connection instanceof HttpsURLConnection) {
                    ((HttpsURLConnection) connection).setSSLSocketFactory(context.getSocketFactory());
                }
                InputStream input = (InputStream) connection.getContent();
                IOUtils.copy(input, bos);
                break;
            } catch (Exception e) {
                attempt++;
                if (attempt == AFGIFTE_DOWNLOAD_ATTEMPTS) {
                    l.addLog("Fout bij laatste poging downloaden afgifte: " + e.getClass().getName() + ": " + e.getMessage());
                    throw e;
                } else {
                    l.addLog("Fout bij poging " + attempt + " om afgifte te downloaden: " + e.getClass().getName() + ": " + e.getMessage());
                    Thread.sleep(AFGIFTE_DOWNLOAD_RETRY_WAIT);
                    l.addLog("Uitvoeren poging " + (attempt + 1) + " om afgifte " + url + " te downloaden...");
                }
            }
        }

        LaadProces lp = new LaadProces();
        lp.setBestand_naam(getLaadprocesBestandsnaam(a));
        lp.setKlantafgiftenummer(a.getKlantAfgiftenummer());
        lp.setContractafgiftenummer(a.getContractAfgiftenummer());
        lp.setArtikelnummer(a.getBestandKenmerken().getArtikelnummer());
        lp.setContractnummer(a.getBestandKenmerken().getContractnummer());
        lp.setAfgifteid(a.getAfgifteID());
        lp.setAfgiftereferentie(a.getAfgiftereferentie());
        lp.setBestandsreferentie(a.getBestand().getBestandsreferentie());
        lp.setBeschikbaar_tot(a.getBeschikbaarTot().toGregorianCalendar().getTime());
        lp.setBestand_naam_hersteld(a.getBestand().getBestandsnaam());

        if (a.getDigikoppelingExternalDatareferences() != null
                && a.getDigikoppelingExternalDatareferences().getDataReference() != null) {
            for (DataReference dr : a.getDigikoppelingExternalDatareferences().getDataReference()) {
                lp.setBestand_datum(dr.getLifetime().getCreationTime().getValue().toGregorianCalendar().getTime());
                break;
            }
        }
        lp.setSoort("brk");
        lp.setStatus(LaadProces.STATUS.STAGING_OK);
        lp.setStatus_datum(new Date());
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        lp.setOpmerking("GDS2 download van " + url + " op " + sdf.format(new Date()));
        lp.setAutomatischProces(em.find(AutomatischProces.class, config.getId()));

        Bericht b = new Bericht();
        b.setLaadprocesid(lp);
        b.setDatum(lp.getBestand_datum());
        b.setSoort("brk");
        b.setStatus_datum(new Date());

        ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(bos.toByteArray()));
        ZipEntry entry = zip.getNextEntry();
        while (entry != null && !entry.getName().toLowerCase().endsWith(".xml")) {
            msg = "Overslaan zip entry geen XML: " + entry.getName();
            l.addLog(msg);
            entry = zip.getNextEntry();
        }
        if (entry == null) {
            msg = "Geen geschikt XML bestand gevonden in zip bestand!";
            l.addLog(msg);
            return null;
        }
        b.setBr_orgineel_xml(IOUtils.toString(zip, "UTF-8"));

        if (log.isDebugEnabled()) {
            // dump xml bestand naar /tmp/ voordat de xml wordt geparsed
            String fName = File.createTempFile(lp.getBestand_naam(), ".xml").getAbsolutePath();
            log.debug("Dump xml bericht naar: " + fName);
            IOUtils.write(b.getBr_orgineel_xml(), new FileWriter(fName));
        }

        try {
            BrkSnapshotXMLReader reader = new BrkSnapshotXMLReader(new ByteArrayInputStream(b.getBr_orgineel_xml().getBytes("UTF-8")));
            BrkBericht bericht = reader.next();

            if (bericht.getDatum() != null) {
                b.setDatum(bericht.getDatum());
            }
            b.setBr_xml(bericht.getBrXml());
            b.setVolgordenummer(bericht.getVolgordeNummer());

            //Als objectRef niet opgehaald kan worden,dan kan het
            //bericht niet verwerkt worden.
            String objectRef = bericht.getObjectRef();
            if (objectRef != null && !objectRef.isEmpty()) {
                b.setObject_ref(bericht.getObjectRef());
                b.setStatus(Bericht.STATUS.STAGING_OK);
                b.setOpmerking("Klaar voor verwerking.");
            } else {
                b.setStatus(Bericht.STATUS.STAGING_NOK);
                b.setOpmerking("Object Ref niet gevonden in bericht-xml, neem contact op met leverancier.");
            }
        } catch (Exception e) {
            b.setStatus(Bericht.STATUS.STAGING_NOK);
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            b.setOpmerking("Fout bij parsen BRK bericht: " + sw.toString());
        }
        try {
            em.persist(lp);
            em.persist(b);
            em.merge(this.config);
            em.flush();
            em.getTransaction().commit();
            em.clear();
        } catch (PersistenceException pe) {
            if (!em.getTransaction().isActive()) {
                em.getTransaction().begin();
            }
            em.getTransaction().rollback();
            em.getTransaction().begin();
            lp.setId(null);
            log.warn("Opslaan van bericht uit laadproces " + lp.getBestand_naam() + " is mislukt.", pe);
            log.warn("Duplicaat bericht: " + b.getObject_ref() + ":" + b.getBr_orgineel_xml() + "(" + b.getBr_xml() + ")");
            lp.setOpmerking(lp.getOpmerking() + ": Fout, duplicaat bericht. Berichtinhoud: \n\n" + b.getBr_xml());
            b = null;
            lp.setStatus(LaadProces.STATUS.STAGING_DUPLICAAT);
            lp.setAutomatischProces(em.find(AutomatischProces.class, this.config.getId()));
            em.merge(this.config);
            em.persist(lp);
            em.flush();
            em.getTransaction().commit();
            em.clear();
        }
        return b;
    }

    /**
     * Post de xml naar de geconfigureerde url.
     *
     * @param proces proces waarvoor doorgetuurd wordt
     * @param l update listener
     * @param b door te sturen bericht
     * @param endpoint url waarnaartoe wordt gepost
     * @return {@code true} als succesvol doorgestuurd
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
            if ((conn.getResponseCode() == HttpURLConnection.HTTP_OK)
                    || (conn.getResponseCode() == HttpURLConnection.HTTP_CREATED)
                    || (conn.getResponseCode() == HttpURLConnection.HTTP_ACCEPTED)) {
                msg = "Bericht " + b.getObject_ref() + " op " + sdf.format(new Date()) + " doorgestuurd naar " + endpoint;
                b.setStatus(Bericht.STATUS.STAGING_FORWARDED);
            } else {
                msg = String.format("HTTP foutcode bij doorsturen bericht %s op %s naar endpoint %s: %s",
                        b.getObject_ref(),
                        sdf.format(new Date()), endpoint, conn.getResponseCode() + ": " + conn.getResponseMessage());
                b.setStatus(Bericht.STATUS.STAGING_NOK);
            }
            b.setOpmerking(msg);
            l.addLog(msg);

            return b.getStatus() == Bericht.STATUS.STAGING_FORWARDED;
        } catch (Exception e) {
            msg = String.format("Fout bij doorsturen bericht op %s naar endpoint %s: %s",
                    sdf.format(new Date()), endpoint, e.getClass() + ": " + e.getMessage());
            b.setOpmerking(msg);
            b.setStatus(Bericht.STATUS.STAGING_NOK);
            l.addLog(msg);
            log.error(msg, e);
            return false;
        }
    }

    /**
     * verwerk de afgiftes in de response en stuur eventueel door.
     *
     * @param afgiftes lijst afgiftes
     * @throws Exception if any
     */
    private void verwerkAfgiftes(List<AfgifteType> afgiftes, BaseURLType baseUrl) throws Exception {
        int filterAlVerwerkt = 0;
        int aantalGeladen = 0;
        int aantalDoorgestuurd = 0;
        int progress = 0;
        List<Long> geladenBerichtIds = new ArrayList();
        String doorsturenUrl = ClobElement.nullSafeGet(this.config.getConfig().get("delivery_endpoint"));
        final String soort = this.config.getConfig().getOrDefault("gds2_br_soort", new ClobElement("brk")).getValue();

        for (AfgifteType a : afgiftes) {
            String url = getAfgifteURL(a, baseUrl);

            if (url != null) {
                if (isAfgifteAlGeladen(a, url)) {
                    filterAlVerwerkt++;
                } else if (soort.equalsIgnoreCase("bag")) {
                    laadBagAfgifte(a, url);
                    aantalGeladen++;
                } else {
                    Bericht b = laadAfgifte(a, url);
                    if (b != null && doorsturenUrl != null) {
                        geladenBerichtIds.add(b.getId());
                    }
                    aantalGeladen++;
                }
            }
            l.progress(++progress);
        }

        Bericht b;
        if (doorsturenUrl != null && !geladenBerichtIds.isEmpty()) {
            l.addLog("Doorsturen van de opgehaalde berichten, in totaal " + geladenBerichtIds.size());
            l.total(geladenBerichtIds.size());
            progress = 0;

            for (Long bId : geladenBerichtIds) {
                b = Stripersist.getEntityManager().find(Bericht.class, bId);
                l.updateStatus("Doorsturen bericht: " + b.getObject_ref());
                doorsturenBericht(this.config, l, b, doorsturenUrl);
                Stripersist.getEntityManager().merge(b);
                Stripersist.getEntityManager().merge(this.config);
                Stripersist.getEntityManager().flush();
                Stripersist.getEntityManager().getTransaction().commit();
                Stripersist.getEntityManager().clear();
                aantalDoorgestuurd++;
                l.progress(++progress);
            }
        }

        l.addLog("\n\n**** resultaat ****\n");
        l.addLog("Aantal afgiftes die al waren verwerkt: " + filterAlVerwerkt);
        l.addLog("\nAantal afgiftes geladen: " + aantalGeladen);
        l.addLog("\nAantal afgiftes doorgestuurd: " + aantalDoorgestuurd + "\n");

        l.addLog(String.format("Het GDS2 ophalen proces met ID %d is afgerond op %tc", config.getId(), Calendar.getInstance()));
        this.config.updateSamenvattingEnLogfile(
                String.format("Het GDS2 ophalen proces, gestart op %tc, is afgerond op %tc. " + LOG_NEWLINE
                        + "Aantal afgiftes die al waren verwerkt: %d" + LOG_NEWLINE
                        + "Aantal afgiftes geladen: %d" + LOG_NEWLINE
                        + "Aantal afgiftes doorgestuurd: %d" + LOG_NEWLINE
                        + "Hoogste klantafgiftenummer: %s",
                        this.config.getLastrun(), Calendar.getInstance(), filterAlVerwerkt, aantalGeladen, aantalDoorgestuurd,
                        ClobElement.nullSafeGet(this.config.getConfig().get("hoogste_afgiftenummer")))
        );

        this.config.setStatus(AutomatischProces.ProcessingStatus.WAITING);
        this.config.setLastrun(new Date());
        Stripersist.getEntityManager().merge(this.config);
        // om geheugen problemen te vookomen bij runs met veel downloads en berichten
        // een flush/commit/clear forceren
        Stripersist.getEntityManager().flush();
        Stripersist.getEntityManager().getTransaction().commit();
        Stripersist.getEntityManager().clear();
    }

    private Gds2AfgifteServiceV20170401 initGDS2() throws Exception {
        Gds2AfgifteServiceV20170401 gds2 = new Gds2AfgifteServiceV20170401Service().getAGds2AfgifteServiceV20170401();
        BindingProvider bp = (BindingProvider) gds2;
        Map<String, Object> ctxt = bp.getRequestContext();

        // soap berichten logger inhaken (actief met TRACE level)
        List<Handler> handlerChain = bp.getBinding().getHandlerChain();
        handlerChain.add(new LogMessageHandler());
        bp.getBinding().setHandlerChain(handlerChain);

        String endpoint = (String) ctxt.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
        l.addLog("Kadaster endpoint: " + endpoint);
        l.updateStatus("Laden keys...");

        l.addLog("Initializing KeyManagerFactory");
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        KeyStore ks = KeyStore.getInstance("jks");
        final char[] thePassword = "changeit".toCharArray();
        PrivateKey privateKey = getPrivateKeyFromPEM(this.config.getConfig().get("gds2_privkey").getValue());
        Certificate certificate = getCertificateFromPEM(this.config.getConfig().get("gds2_pubkey").getValue());
        ks.load(null);
        ks.setKeyEntry("thekey", privateKey, thePassword, new Certificate[]{certificate});
        kmf.init(ks, thePassword);

        l.updateStatus("Opzetten SSL context...");
        context = createSslContext(kmf);

        Client client = ClientProxy.getClient(gds2);
        HTTPConduit http = (HTTPConduit) client.getConduit();
        TLSClientParameters tlsClientParameters = new TLSClientParameters();
        tlsClientParameters.setKeyManagers(kmf.getKeyManagers());
        tlsClientParameters.setSslContext(context);
        http.setTlsClientParameters(tlsClientParameters);

        return gds2;
    }

    private SSLContext createSslContext(KeyManagerFactory kmf) throws KeyStoreException, IOException, CertificateException, NoSuchProviderException, NoSuchAlgorithmException, KeyManagementException {
        final SSLContext sslContext = SSLContext.getInstance("TLS", "SunJSSE");

        l.addLog("Loading PKIX Overheid keystore");
        KeyStore ks = KeyStore.getInstance("jks");
        ks.load(nl.b3p.gds2.GDS2Util.class.getResourceAsStream("/pkioverheid.jks"), "changeit".toCharArray());

        l.addLog("Initializing default TrustManagerFactory");
        final TrustManagerFactory javaDefaultTrustManager = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        javaDefaultTrustManager.init((KeyStore) null);
        X509TrustManager defaultX509TrustManager = null;
        for (TrustManager t : javaDefaultTrustManager.getTrustManagers()) {
            if (t instanceof X509TrustManager) {
                defaultX509TrustManager = (X509TrustManager) t;
                break;
            }
        }
        l.addLog("Initializing PKIX TrustManagerFactory");
        final TrustManagerFactory customCaTrustManager = TrustManagerFactory.getInstance("PKIX");
        customCaTrustManager.init(ks);

        l.addLog("Initializing SSLContext");
        sslContext.init(
                kmf.getKeyManagers(),
                new TrustManager[]{
                    new TrustManagerDelegate(
                            // customCaTrustManager is PKIX dus altijd X.509 (RFC3280)
                            (X509TrustManager) customCaTrustManager.getTrustManagers()[0],
                            defaultX509TrustManager
                    )
                },
                null
        );
        return sslContext;
    }
}
