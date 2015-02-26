package nl.b3p.brmo.persistence.staging;

import com.sun.xml.ws.developer.JAXWSProperties;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyStore;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.persistence.Entity;
import javax.persistence.NoResultException;
import javax.persistence.Transient;
import javax.xml.ws.BindingProvider;
import nl.b3p.gds2.Main;
import nl.kadaster.schemas.gds2.afgifte_bestandenlijstgbopvragen.v20130701.BestandenlijstGbOpvragenType;
import nl.kadaster.schemas.gds2.afgifte_bestandenlijstgbresultaat.afgifte.v20130701.AfgifteGBType;
import nl.kadaster.schemas.gds2.afgifte_bestandenlijstopvragen.v20130701.BestandenlijstOpvragenType;
import nl.kadaster.schemas.gds2.afgifte_bestandenlijstresultaat.afgifte.v20130701.AfgifteType;
import nl.kadaster.schemas.gds2.afgifte_bestandenlijstselectie.v20130701.AfgifteSelectieCriteriaType;
import nl.kadaster.schemas.gds2.afgifte_bestandenlijstselectie.v20130701.BestandKenmerkenType;
import nl.kadaster.schemas.gds2.service.afgifte.v20130701.Gds2AfgifteServiceV20130701;
import nl.kadaster.schemas.gds2.service.afgifte.v20130701.Gds2AfgifteServiceV20130701Service;
import nl.kadaster.schemas.gds2.service.afgifte_bestandenlijstgbopvragen.v20130701.BestandenlijstGBOpvragenRequest;
import nl.kadaster.schemas.gds2.service.afgifte_bestandenlijstgbopvragen.v20130701.BestandenlijstGBOpvragenResponse;
import nl.kadaster.schemas.gds2.service.afgifte_bestandenlijstopvragen.v20130701.BestandenlijstOpvragenRequest;
import nl.kadaster.schemas.gds2.service.afgifte_bestandenlijstopvragen.v20130701.BestandenlijstOpvragenResponse;
import nl.logius.digikoppeling.gb._2010._10.DataReference;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stripesstuff.stripersist.Stripersist;

/**
 *
 * @author Matthijs Laan
 */

@Entity
public class GDS2OphaalProces extends AutomatischProces {
    private static final Log log = LogFactory.getLog(GDS2OphaalProces.class);

    @Transient
    private ProgressUpdateListener l;

    @Transient
    private SSLContext context;

    @Override
    public void execute(ProgressUpdateListener listener) {
        this.l = listener;

        try {
            l.updateStatus("Initialiseren...");

            Gds2AfgifteServiceV20130701 gds2 = new Gds2AfgifteServiceV20130701Service().getAGds2AfgifteServiceV20130701();

            BestandenlijstOpvragenRequest request = new BestandenlijstOpvragenRequest();
            BestandenlijstOpvragenType verzoek = new BestandenlijstOpvragenType();
            request.setVerzoek(verzoek);
            AfgifteSelectieCriteriaType criteria = new AfgifteSelectieCriteriaType();
            verzoek.setAfgifteSelectieCriteria(criteria);

            BestandenlijstGBOpvragenRequest requestGb = new BestandenlijstGBOpvragenRequest();
            BestandenlijstGbOpvragenType verzoekGb = new BestandenlijstGbOpvragenType();
            requestGb.setVerzoek(verzoekGb);
            String contractnummer = getConfig().get("gds2_contractnummer").getValue();
            criteria.setBestandKenmerken(new BestandKenmerkenType());
            criteria.getBestandKenmerken().setContractnummer(contractnummer);
            verzoekGb.setAfgifteSelectieCriteria(criteria);

            BindingProvider bp = (BindingProvider)gds2;

            Map<String, Object> ctxt = bp.getRequestContext();

            //ctxt.put(BindingProvider.USERNAME_PROPERTY, username);
            //ctxt.put(BindingProvider.PASSWORD_PROPERTY, password);

            String endpoint = (String)ctxt.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
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
            String keystore = getConfig().get("keystore_path").getValue();
            String pass = getConfig().get("keystore_password").getValue();
            ks.load(new FileInputStream(keystore), pass.toCharArray());

            kmf.init(ks, "changeit".toCharArray());

            l.updateStatus("Opzetten SSL context...");

            l.addLog("Initializing SSLContext");
            context = SSLContext.getInstance("TLS", "SunJSSE");
            context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            SSLContext.setDefault(context);
            ctxt.put(JAXWSProperties.SSL_SOCKET_FACTORY, context.getSocketFactory());

            List<AfgifteGBType> afgiftesGb = null;

            l.updateStatus("Uitvoeren SOAP request naar Kadaster...");
            BestandenlijstGBOpvragenResponse responseGb = gds2.bestandenlijstGBOpvragen(requestGb);

            int filterAlVerwerkt = 0;
            int aantalGeladen = 0;

            afgiftesGb = responseGb.getAntwoord().getBestandenLijstGB().getAfgifteGB();
            l.total(afgiftesGb.size());
            int progress = 0;
            for(AfgifteGBType a: afgiftesGb) {
                String url = null;

                if(a.getDigikoppelingExternalDatareferences() != null
                        && a.getDigikoppelingExternalDatareferences().getDataReference() != null) {
                    for(DataReference dr: a.getDigikoppelingExternalDatareferences().getDataReference()) {
                        url = dr.getTransport().getLocation().getSenderUrl().getValue();
                        break;
                    }
                }
                if(url != null) {
                    if(isAfgifteAlGeladen(a, url)) {
                        filterAlVerwerkt++;
                    } else {
                        laadAfgifte(a, url);
                        aantalGeladen++;
                    }
                }
                l.progress(++progress);
            }

            l.addLog("Meer afgiftes beschikbaar: " + responseGb.getAntwoord().getMeerAfgiftesbeschikbaar());

            BestandenlijstOpvragenResponse response = gds2.bestandenlijstOpvragen(request);

            List<AfgifteType> afgiftes = response.getAntwoord().getBestandenLijst().getAfgifte();

            l.addLog("\n\n**** resultaat ****\n");
            l.addLog("Aantal afgiftes die al waren verwerkt: " + filterAlVerwerkt);
            l.addLog("Aantal afgiftes geladen: " + aantalGeladen);
            l.addLog("Aantal afgiftes: " + (afgiftes == null ? "<fout>" :  afgiftes.size()));
            l.addLog("Aantal afgiftes grote bestanden: " + (afgiftesGb == null ? "<fout>" : afgiftesGb.size()));

            Stripersist.getEntityManager().getTransaction().commit();
        } catch(Exception e) {
            l.exception(e);
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
        } catch(NoResultException nre) {
            return false;
        }
    }

    private void laadAfgifte(AfgifteGBType a, String url) throws MalformedURLException, IOException {
        l.updateStatus("Downloaden " + url);
        l.addLog("Downloaden " + url);
        URLConnection connection = new URL(url).openConnection();
        InputStream input = (InputStream)connection.getContent();

        LaadProces lp = new LaadProces();
        lp.setBestand_naam(getLaadprocesBestansanaam(a));

        if(a.getDigikoppelingExternalDatareferences() != null
                && a.getDigikoppelingExternalDatareferences().getDataReference() != null) {
            for(DataReference dr: a.getDigikoppelingExternalDatareferences().getDataReference()) {
                lp.setBestand_datum(dr.getLifetime().getCreationTime().getValue().toGregorianCalendar().getTime());
                break;
            }
        }
        lp.setSoort("brk");
        lp.setStatus(LaadProces.STATUS.STAGING_OK);
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        lp.setOpmerking("GDS2 download van " + url + " op " + sdf.format(new Date()));

        Bericht b = new Bericht();
        b.setLaadprocesid(lp);
        b.setDatum(lp.getBestand_datum());
        b.setSoort("brk");
        b.setStatus(Bericht.STATUS.STAGING_OK);
        b.setStatus_datum(new Date());

        ZipInputStream zip = new ZipInputStream(input);
        ZipEntry entry = zip.getNextEntry();
        while(entry != null && !entry.getName().toLowerCase().endsWith(".xml")) {
            l.addLog("Overslaan zip entry geen XML: " + entry.getName());
            entry = zip.getNextEntry();
        }
        if(entry == null) {
            l.addLog("Geen geschikt XML bestand gevonden in zip bestand!");
            return;
        }
        b.setBr_xml(IOUtils.toString(zip, "UTF-8"));

        Stripersist.getEntityManager().persist(lp);
        Stripersist.getEntityManager().persist(b);
        Stripersist.getEntityManager().flush();

    }
}
