/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.loader.util;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import nl.b3p.brmo.loader.StagingProxy;
import nl.b3p.brmo.loader.entity.Bericht;
import nl.b3p.brmo.loader.entity.LaadProces;
import nl.b3p.brmo.loader.gml.BGTGMLLightLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *
 * @author mprins
 */
public class BGTLightRsgbTransformer implements RsgbTransformer {

    private static final Log LOG = LogFactory.getLog(BGTLightRsgbTransformer.class);
    private StagingProxy stagingProxy;

    public BGTLightRsgbTransformer(StagingProxy stagingProxy) {
        this.stagingProxy = stagingProxy;
    }

    @Override
    public String transformToDbXml(Bericht bericht) throws SAXException, IOException, TransformerConfigurationException, TransformerException {
        int total = 0;
        try {
            // het bericht ophalen
            // want bericht.getLaadProcesId() is null ??
            Long lpId = (long) stagingProxy.getBerichtById(bericht.getId()).getLaadProcesId();
            // van het laadproces de bestandsnaam ophalen
            LaadProces lp = stagingProxy.getLaadProcesById(lpId);
            String naam = lp.getBestandNaam();
            File zip = new File(naam);
            // het bestand aan de GML transformer geven om te transformeren
            BGTGMLLightLoader l = new BGTGMLLightLoader();

            // XXX hardcoded props
            Properties params = new Properties();
            params.put("dbtype", "postgis");
            params.put("jndiReferenceName", "java:comp/env/jdbc/brmo/rsgb");
            params.put("schema", "bgttest");

            l.setDbConnProps(params);
            total = l.processZipFile(zip);
            LOG.debug("verwerkte BGT features: " + total);
        } catch (SQLException ex) {
            LOG.error(ex);
            throw new IOException(ex);
        }
        return "totaal verwerkt: " + total;
    }

    @Override
    public Node transformToDbXmlNode(Bericht bericht) throws SAXException, IOException, TransformerConfigurationException, TransformerException {
        throw new UnsupportedOperationException("Conversie naar XML node is niet geimplementeerd");
    }

}
