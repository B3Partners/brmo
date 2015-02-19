/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.digikoppeling;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.xml.bind.DatatypeConverter;
import nl.b3p.brmo.digipoort.koppelvlakservices._1_2.AfleverRequest;
import nl.b3p.brmo.digipoort.koppelvlakservices._1_2.AfleverResponse;
import nl.b3p.brmo.digipoort.koppelvlakservices._1_2.BerichtInhoudType;
import nl.b3p.brmo.digipoort.koppelvlakservices._1_2.FoutType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Mark Prins <mark@b3partners.nl>
 */
public class BAGAfleverRequestHandler implements AfleverRequestHandling {

    private static final Log log = LogFactory.getLog(BAGAfleverRequestHandler.class);

    /**
     * Afhandelen van het aflever verzoek. dwz uitpakken van de payload en
     * doorgeven aan BRMO... of opslaan
     *
     * @param verzoek
     * @param antwoord
     */
    @Override
    public void handle(AfleverRequest verzoek, AfleverResponse antwoord) {
        try {
            antwoord.setTijdstempelAfgeleverd(XMLUtil.getNow());
            antwoord.setKenmerk(verzoek.getKenmerk());
            antwoord.setBerichtsoort(verzoek.getBerichtsoort());
            antwoord.setBerichtkenmerk(verzoek.getBerichtkenmerk());

            log.debug("Verwerken verzoek: " + verzoek);
            // inhoud ophalen
            BerichtInhoudType t = verzoek.getBerichtInhoud();
            String bNaam = t.getBestandsnaam();
            String mType = t.getMimeType();
            byte[] inhoud = t.getInhoud();

            // TODO "iets" doen met bericht in het verzoek
            if (mType.equalsIgnoreCase("application/xml")) {

            } else if (mType.equalsIgnoreCase("application/base64")) {
                inhoud = DatatypeConverter.parseBase64Binary(new String(inhoud, "UTF-8"));
            }
            File payload = new File("/tmp/", bNaam);
            FileWriter fw = new FileWriter(payload);
            fw.write(new String(inhoud));
            fw.close();

            // TODO antwoord aanpassen...
            antwoord.setStatusdetails(String.format("Er zijn %d bytes opgeslagen in bestand %s.", payload.length(), payload.getName()));
            antwoord.setStatuscode(STATUSCODE.SUCCESS.toString());
            antwoord.setTijdstempelStatus(XMLUtil.getNow());

        } catch (UnsupportedEncodingException ex) {
            // mag niet voorkomen want in strijd met "Algemene afspraak 'Karaktercodering en karakterset'"
            FoutType t = new FoutType();
            t.setFoutbeschrijving(ex.getLocalizedMessage());
            t.setFoutcode(STATUSCODE.ERROR.toString());
            antwoord.setStatusFoutcode(t);
        } catch (IOException ex) {
            FoutType t = new FoutType();
            t.setFoutbeschrijving(ex.getLocalizedMessage());
            t.setFoutcode(STATUSCODE.ERROR.toString());
            antwoord.setStatusFoutcode(t);

        }

    }
}
