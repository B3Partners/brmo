/*
 * Copyright (C) 2017 B3Partners B.V.
 */
package nl.b3p.brmo.loader.util;

import java.io.File;
import java.net.MalformedURLException;
import java.sql.SQLException;
import javax.sql.DataSource;
import javax.xml.bind.JAXBException;
import nl.b3p.brmo.loader.ProgressUpdateListener;
import nl.b3p.brmo.loader.StagingProxy;
import nl.b3p.brmo.loader.entity.LaadProces;
import static nl.b3p.brmo.loader.entity.LaadProces.STATUS;
import nl.b3p.topnl.Processor;
import nl.b3p.topnl.TopNLType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom2.JDOMException;

/**
 *
 * @author Meine Toonen
 */
public class TopNLRsgbTransformer implements Runnable {

    private static final Log LOG = LogFactory.getLog(TopNLRsgbTransformer.class);
    private final StagingProxy stagingProxy;
    private final DataSource dataSourceTopNL;
    private final ProgressUpdateListener listener;
    private final long[] lpIDs;
    private Processor processor = null;
    

    public TopNLRsgbTransformer(DataSource dataSourceTopNL, StagingProxy stagingProxy, long[] lpIDs, ProgressUpdateListener listener) throws JAXBException, SQLException {
        this.stagingProxy = stagingProxy;
        this.dataSourceTopNL = dataSourceTopNL;
        this.lpIDs = lpIDs;
        this.listener = listener;
        this.processor = new Processor(this.dataSourceTopNL);
    }

    private void transform(long lpID) throws SQLException  {
        STATUS status;
        String opmerkingen;
        LaadProces lp = stagingProxy.getLaadProcesById(lpID);
        
        if (TopNLType.isTopNLType(lp.getSoort()) && lp.getStatus() == STATUS.STAGING_OK) {
            stagingProxy.updateLaadProcesStatus(lp, STATUS.RSGB_TOPNL_WAITING, "Transformatie loopt...");
            File gml = new File(lp.getBestandNaam());
            try {
                processor.importIntoDb(gml.toURI().toURL(), TopNLType.valueOf(lp.getSoort().toUpperCase()));
                switch (processor.getStatus()) {
                    case OK:
                        status = STATUS.RSGB_TOPNL_OK;
                        opmerkingen = "Geen fouten bij inladen.";
                        break;
                    case NOK:
                    default:
                        status = STATUS.RSGB_TOPNL_NOK;
                        opmerkingen = "Er zijn herstelbare fouten bij inladen opgetreden. Waarschijnlijk is niet alle data geladen van bestand " + gml;
                }
                stagingProxy.updateLaadProcesStatus(lp, status, opmerkingen);
            } catch (JDOMException | MalformedURLException ex) {
                LOG.debug("Error loading gml file", ex);
                opmerkingen = "Laden van bestand " + gml + " is mislukt: " + ex.getLocalizedMessage();
                status = STATUS.RSGB_TOPNL_NOK;
                stagingProxy.updateLaadProcesStatus(lp, status, opmerkingen);
            }
        } else {
            LOG.warn("LaadProces " + lp.getId() + " van soort " + lp.getSoort() + " met status: " + lp.getStatus() + " is overgeslagen.");
        }
    }

    public void init() throws SQLException {
       /* geomjdbc = GeometryJdbcConverterFactory.getGeometryJdbcConverter(dataSourceTopNL.getConnection());

        params.put("jndiReferenceName", "java:comp/env/jdbc/brmo/rsgbbgt");
        params.put("dbtype", geomjdbc.getGeotoolsDBTypeName());
        params.put("schema", geomjdbc.getSchema());

        gmlLoader.setDbConnProps(params);
        gmlLoader.setBijwerkDatum(new Date());
        gmlLoader.setIsOracle(geomjdbc.getGeotoolsDBTypeName().toLowerCase().contains("oracle"));
        gmlLoader.setIsMSSQL(geomjdbc.getGeotoolsDBTypeName().toLowerCase().contains("sqlserver"));*/
    }


    @Override
    public void run() {
        try {
            init();
            int count = 0;
            if (listener != null) {
                listener.total(lpIDs.length);
                listener.progress(count);
            }
            for (long id : lpIDs) {
                this.transform(id);
                count++;
                if (listener != null) {
                    listener.progress(count);
                }
            }
        } catch (Exception e) {
            LOG.error("Fout tijdens verwerken TOPNL laadprocessen", e);
            if (listener != null) {
                listener.exception(e);
            }
        }
    }
}
