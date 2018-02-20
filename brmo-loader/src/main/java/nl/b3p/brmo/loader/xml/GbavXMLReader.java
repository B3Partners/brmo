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
package nl.b3p.brmo.loader.xml;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stax.StAXResult;
import javax.xml.transform.stax.StAXSource;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.entity.GbavBericht;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Mark Prins
 */
public class GbavXMLReader extends BrmoXMLReader {

    private static final Log log = LogFactory.getLog(GbavXMLReader.class);
    private static final String PERSOON = "persoon";
    public static final String PREFIX = "NL.GBA.Persoon.";
    private final XMLInputFactory factory = XMLInputFactory.newInstance();
    private final XMLStreamReader streamReader;
    private final Transformer transformer;
    private final XMLOutputFactory xmlof;
    private GbavBericht nextBericht = null;
    private int volgordeNummer = 0;

    public GbavXMLReader(InputStream in) throws XMLStreamException, TransformerConfigurationException {
        streamReader = factory.createXMLStreamReader(in);
        TransformerFactory tf = TransformerFactory.newInstance();
        transformer = tf.newTransformer();
        xmlof = XMLOutputFactory.newInstance();
        // xmlof.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);

        init();
    }

    @Override
    public void init() throws XMLStreamException {
        soort = BrmoFramework.BR_GBAV;
        try {
            while (streamReader.hasNext()) {
                if (streamReader.isStartElement()) {
                    String localName = streamReader.getLocalName();
                    if (localName.equals(PERSOON)) {
                        break;
                    }
                }
                streamReader.next();
            }
        } catch (XMLStreamException ex) {
            log.error("Error while streaming XML", ex);
        }
        if (getBestandsDatum() == null) {
            setBestandsDatum(new Date());
        }
    }

    @Override
    public boolean hasNext() throws Exception {
        if (nextBericht != null) {
            return true;
        }
        try {
            while (streamReader.hasNext()) {
                if (streamReader.isStartElement() && streamReader.getLocalName().equals(PERSOON)) {
                    StringWriter sw = new StringWriter();
                    transformer.transform(new StAXSource(streamReader), new StAXResult(xmlof.createXMLStreamWriter(sw)));
                    nextBericht = new GbavBericht(sw.toString());
                    nextBericht.setDatum(nextBericht.parseDatum());
                    if (nextBericht.getDatum() == null) {
                        nextBericht.setDatum(new Date());
                    }
                    nextBericht.setVolgordeNummer(volgordeNummer);

                    String bsn = nextBericht.getBsn();
                    String bsnHash = this.getHash(bsn);
                    nextBericht.setObjectRef(PREFIX + bsnHash);

                    Map<String, String> bsns = new HashMap<>();
                    nextBericht.getBsnList().forEach((_bsn) -> {
                        log.debug("toevoegen bsn en hash: " + _bsn + ":" + this.getHash(_bsn));
                        bsns.put(_bsn, this.getHash(_bsn));
                    });
                    log.debug("aantal BSN in bericht:" + bsns.size());
                    nextBericht.setBsnMap(bsns);
                    return true;
                } else {
                    streamReader.next();
                }
            }
        } catch (XMLStreamException ex) {
            log.error("Streamfout tijdens parsen GBA-V XML", ex);
        }
        return false;
    }

    /**
     * geeft het volgende bericht uit de stream, mits
     * {@link #hasNext() } {@code true} geeft. Gebruik dus eerst
     * {@link #hasNext()}.
     *
     * @return volgend bericht
     * @throws Exception soms
     */
    @Override
    public GbavBericht next() throws Exception {
        GbavBericht b = nextBericht;
        nextBericht = null;
        volgordeNummer++;
        return b;
    }

}
