package nl.b3p.brmo.loader.xml;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.Iterator;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.entity.Bericht;
import nl.b3p.brmo.loader.entity.NhrBericht;
import nl.b3p.brmo.loader.entity.NhrBerichten;
import nl.b3p.brmo.loader.util.BrmoLeegBestandException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Matthijs Laan
 */
public class NhrXMLReader extends BrmoXMLReader {

    private static final Log log = LogFactory.getLog(NhrXMLReader.class);

    private static Templates splitTemplates;

    private static Transformer t;

    Iterator<NhrBericht> iterator;
    int volgorde = 0;

    public NhrXMLReader(InputStream in) throws Exception {
        initTemplates();

        // Split input naar multiple berichten
        DOMResult r = new DOMResult();
        splitTemplates.newTransformer().transform(new StreamSource(in), r);

        JAXBContext jc = JAXBContext.newInstance(NhrBerichten.class, NhrBericht.class, Bericht.class);
        Unmarshaller unmarshaller = jc.createUnmarshaller();

        NhrBerichten b = (NhrBerichten)unmarshaller.unmarshal(new DOMSource(r.getNode()));

        if(b == null || b.berichten == null) {
            throw new BrmoLeegBestandException("Geen BRMO berichten gevonden in NHR XML");
        }

        if(!b.berichten.isEmpty()) {
            setBestandsDatum(b.berichten.get(0).getDatum());
        }

        iterator = b.berichten.iterator();

        init();
    }

    private synchronized void initTemplates() throws Exception {
        if(splitTemplates == null) {
            log.info("Initializing NHR split XSL templates...");
            Source xsl = new StreamSource(this.getClass().getResourceAsStream("/xsl/nhr-split-2.5.xsl"));
            TransformerFactory tf = TransformerFactory.newInstance();
            tf.setURIResolver(new URIResolver() {
                @Override
                public Source resolve(String href, String base) throws TransformerException {
                    return new StreamSource(NhrXMLReader.class.getResourceAsStream("/xsl/" + href));
                }
            });
            NhrXMLReader.splitTemplates = tf.newTemplates(xsl);

            t = TransformerFactory.newInstance().newTransformer();
        }
    }

    @Override
    public void init() throws Exception {
        soort = BrmoFramework.BR_NHR;
    }

    @Override
    public boolean hasNext() throws Exception {
        return iterator.hasNext();
    }

    @Override
    public NhrBericht next() throws Exception {
        NhrBericht b = iterator.next();

        StringWriter sw = new StringWriter();
        t.transform(new DOMSource(b.getNode().getFirstChild()), new StreamResult(sw));
        b.setBrXml(sw.toString());

        b.setVolgordeNummer(volgorde++);
        return b;
    }
}
