package nl.b3p.brmo.persistence.staging;

import com.sun.xml.ws.developer.JAXWSProperties;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.List;
import java.util.Map;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.persistence.Entity;
import javax.xml.ws.BindingProvider;
import nl.b3p.gds2.Main;
import nl.kadaster.schemas.gds2.afgifte_bestandenlijstgbopvragen.v20130701.BestandenlijstGbOpvragenType;
import nl.kadaster.schemas.gds2.afgifte_bestandenlijstgbresultaat.afgifte.v20130701.AfgifteGBType;
import nl.kadaster.schemas.gds2.afgifte_bestandenlijstopvragen.v20130701.BestandenlijstOpvragenType;
import nl.kadaster.schemas.gds2.afgifte_bestandenlijstresultaat.afgifte.v20130701.AfgifteType;
import nl.kadaster.schemas.gds2.afgifte_bestandenlijstselectie.v20130701.AfgifteSelectieCriteriaType;
import nl.kadaster.schemas.gds2.service.afgifte.v20130701.Gds2AfgifteServiceV20130701;
import nl.kadaster.schemas.gds2.service.afgifte.v20130701.Gds2AfgifteServiceV20130701Service;
import nl.kadaster.schemas.gds2.service.afgifte_bestandenlijstgbopvragen.v20130701.BestandenlijstGBOpvragenRequest;
import nl.kadaster.schemas.gds2.service.afgifte_bestandenlijstgbopvragen.v20130701.BestandenlijstGBOpvragenResponse;
import nl.kadaster.schemas.gds2.service.afgifte_bestandenlijstopvragen.v20130701.BestandenlijstOpvragenRequest;
import nl.kadaster.schemas.gds2.service.afgifte_bestandenlijstopvragen.v20130701.BestandenlijstOpvragenResponse;
import nl.logius.digikoppeling.gb._2010._10.DataReference;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Matthijs Laan
 */

@Entity
public class GDS2OphaalProces extends AutomatischProces {
    private static final Log log = LogFactory.getLog(GDS2OphaalProces.class);

    @Override
    public void execute(ProgressUpdateListener l) {

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
            verzoekGb.setAfgifteSelectieCriteria(criteria);

            BindingProvider bp = (BindingProvider)gds2;

            Map<String, Object> ctxt = bp.getRequestContext();

            //ctxt.put(BindingProvider.USERNAME_PROPERTY, username);
            //ctxt.put(BindingProvider.PASSWORD_PROPERTY, password);

            String endpoint = (String)ctxt.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
            l.addLog("Origineel endpoint: " + endpoint);

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
            SSLContext context = SSLContext.getInstance("TLS", "SunJSSE");
            context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            SSLContext.setDefault(context);
            ctxt.put(JAXWSProperties.SSL_SOCKET_FACTORY, context.getSocketFactory());

            List<AfgifteType> afgiftes = null;
            List<AfgifteGBType> afgiftesGb = null;

            l.updateStatus("Uitvoeren SOAP request naar Kadaster...");
            BestandenlijstGBOpvragenResponse responseGb = gds2.bestandenlijstGBOpvragen(requestGb);

            afgiftesGb = responseGb.getAntwoord().getBestandenLijstGB().getAfgifteGB();
            for(AfgifteGBType a: afgiftesGb) {
                String kenmerken = "(geen)";
                if(a.getBestandKenmerken() != null) {
                    kenmerken = String.format("contractnr: %s, artikelnr: %s, artikelomschrijving: %s",
                            a.getBestandKenmerken().getContractnummer(),
                            a.getBestandKenmerken().getArtikelnummer(),
                            a.getBestandKenmerken().getArtikelomschrijving());
                }
                /*System.out.printf("ID: %s, referentie: %s, bestandsnaam: %s, bestandref: %s, beschikbaarTot: %s, kenmerken: %s\n",
                        a.getAfgifteID(),
                        a.getAfgiftereferentie(),
                        a.getBestand().getBestandsnaam(),
                        a.getBestand().getBestandsreferentie(),
                        a.getBeschikbaarTot(),
                        kenmerken);*/
                if(a.getDigikoppelingExternalDatareferences() != null
                        && a.getDigikoppelingExternalDatareferences().getDataReference() != null) {
                    for(DataReference dr: a.getDigikoppelingExternalDatareferences().getDataReference()) {
                        l.addLog(dr.getTransport().getLocation().getSenderUrl().getValue());
                        /*System.out.printf("   Digikoppeling datareference: contextId: %s, creationTime: %s, expirationTime: %s, filename: %s, checksum: %s, size: %d, type: %s, senderUrl: %s, receiverUrl: %s\n",
                                dr.getContextId(),
                                dr.getLifetime().getCreationTime().getValue(),
                                dr.getLifetime().getExpirationTime().getValue(),
                                dr.getContent().getFilename(),
                                dr.getContent().getChecksum().getValue(),
                                dr.getContent().getSize(),
                                dr.getContent().getContentType(),
                                dr.getTransport().getLocation().getSenderUrl() == null ? "-" : dr.getTransport().getLocation().getSenderUrl().getValue(),
                                dr.getTransport().getLocation().getReceiverUrl() == null ? "-" : dr.getTransport().getLocation().getReceiverUrl().getValue());*/
                    }
                }
            }

            l.addLog("Meer afgiftes beschikbaar: " + responseGb.getAntwoord().getMeerAfgiftesbeschikbaar());

            BestandenlijstOpvragenResponse response = gds2.bestandenlijstOpvragen(request);

            afgiftes = response.getAntwoord().getBestandenLijst().getAfgifte();

            l.addLog("\n\n**** resultaat ****\n");
            l.addLog("Aantal afgiftes: " + (afgiftes == null ? "<fout>" :  afgiftes.size()));
            l.addLog("Aantal afgiftes grote bestanden: " + (afgiftesGb == null ? "<fout>" : afgiftesGb.size()));

        } catch(Exception e) {
            l.exception(e);
        }
    }
}
