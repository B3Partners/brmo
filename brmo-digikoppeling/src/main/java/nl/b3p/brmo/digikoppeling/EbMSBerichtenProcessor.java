/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.digikoppeling;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import nl.b3p.brmo.digipoort.koppelvlakservices._1_2.AfleverFault;
import nl.b3p.brmo.digipoort.koppelvlakservices._1_2.AfleverRequest;
import nl.b3p.brmo.digipoort.koppelvlakservices._1_2.AfleverResponse;
import nl.b3p.brmo.digipoort.koppelvlakservices._1_2.FoutType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Mark Prins <mark@b3partners.nl>
 */
public class EbMSBerichtenProcessor {

    private static final Log log = LogFactory.getLog(EbMSBerichtenProcessor.class);

    private JAXBContext jc;

    private String storeDirectory;

    /**
     * constructor, initialiseert de JAXBContext en de opslag directory.
     *
     * @param storeDirectory directory waar de (uitgepakte) berichten worden
     * opgeslagen
     */
    public EbMSBerichtenProcessor(String storeDirectory) {
        this.storeDirectory = storeDirectory;
        new File(this.storeDirectory).mkdirs();
        try {
            this.jc = JAXBContext.newInstance("nl.b3p.brmo.digipoort.koppelvlakservices._1_2");
        } catch (JAXBException ex) {
            log.fatal("Er kon geen JAXB context worden geinitialiseerd.", ex);
        }
    }

    /**
     * Verwerkt het aflever request en zet een aflever response (of een aflever
     * fault) op de output stream.
     *
     * @param input
     * @param output
     * @throws IOException
     */
    public void processAfleverRequest(InputStream input, OutputStream output) throws IOException {
        AfleverResponse antwoord = new AfleverResponse();
        AfleverRequest verzoek;
        AfleverFault fout = null;
        try {
            Unmarshaller u = this.jc.createUnmarshaller();
            verzoek = (AfleverRequest) u.unmarshal(input);
            log.info(String.format("AfleverRequest (type %s) ontvangen met kenmerk %s.",
                    verzoek.getBerichtsoort(), verzoek.getBerichtkenmerk()));
        } catch (JAXBException ex) {
            log.error("Parsen van de aflever request is mislukt.", ex);
            throw new IOException(ex);
        }

        if (verzoek.getBerichtsoort().equalsIgnoreCase("BAG")) {
            BAGAfleverRequestHandler h = new BAGAfleverRequestHandler(storeDirectory);
            h.handle(verzoek, antwoord);
            // }else if( TODO andere berichten...){
        } else {
            // foutmelding genereren
            String msg = String.format("Er is een niet ondersteunde AfleverRequest (type %s) gestuurd.", verzoek.getBerichtsoort());
            log.warn(msg);
            FoutType t = new FoutType();
            t.setFoutbeschrijving(msg);
            t.setFoutcode(AfleverRequestHandling.STATUSCODE.ERROR.toString());
            antwoord.setStatusFoutcode(t);
        }

        try {
            Marshaller m = this.jc.createMarshaller();
            m.setProperty("jaxb.formatted.output", log.isDebugEnabled());
            if (antwoord.getStatusFoutcode() != null) {
                fout = new AfleverFault(antwoord.getStatusFoutcode());
                m.marshal(fout, output);
            } else {
                m.marshal(antwoord, output);
            }
        } catch (JAXBException ex) {
            log.error("Parsen van de aflever response is mislukt.", ex);
            throw new IOException(ex);
        }

    }

}
