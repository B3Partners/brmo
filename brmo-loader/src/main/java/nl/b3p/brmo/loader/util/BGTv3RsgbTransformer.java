
package nl.b3p.brmo.loader.util;

import java.io.File;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import javax.sql.DataSource;
import static nl.b3p.brmo.loader.BrmoFramework.BR_BGTV3;
import nl.b3p.brmo.loader.ProgressUpdateListener;
import nl.b3p.brmo.loader.StagingProxy;
import nl.b3p.brmo.loader.entity.LaadProces;
import nl.b3p.brmo.loader.gml.BGTv3Loader;
import nl.b3p.loader.jdbc.GeometryJdbcConverter;
import nl.b3p.loader.jdbc.GeometryJdbcConverterFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author matthijsln
 */
public class BGTv3RsgbTransformer implements Runnable  {

    private static final Log log = LogFactory.getLog(BGTv3RsgbTransformer.class);
    
    private final StagingProxy stagingProxy;
    private final DataSource dataSourceRsgbBgt;
    private final ProgressUpdateListener listener;
    private final long[] lpIDs;
    private final Properties params = new Properties();
    private BGTv3Loader bgtv3Loader;

    public BGTv3RsgbTransformer(DataSource dataSourceRsgbBgt, StagingProxy stagingProxy, long[] lpIDs, ProgressUpdateListener listener) {
        this.stagingProxy = stagingProxy;
        this.dataSourceRsgbBgt = dataSourceRsgbBgt;
        this.lpIDs = lpIDs;
        this.listener = listener;

    }

    private void transform(long lpID) throws SQLException {
        LaadProces.STATUS status;
        LaadProces lp = stagingProxy.getLaadProcesById(lpID);
        if (lp.getSoort().equalsIgnoreCase(BR_BGTV3) && lp.getStatus() == LaadProces.STATUS.STAGING_OK) {
            File zip = new File(lp.getBestandNaam());
            stagingProxy.updateLaadProcesStatus(lp, LaadProces.STATUS.RSGB_BGT_WAITING, "Transformatie loopt...");
            try {
                // het bestand aan de GML transformer geven om te transformeren
                boolean ok = bgtv3Loader.processZipFile(zip);
                status = ok ? LaadProces.STATUS.RSGB_BGT_OK : LaadProces.STATUS.RSGB_BGT_NOK;
                stagingProxy.updateLaadProcesStatus(lp, status, bgtv3Loader.getOpmerkingen().toString());
            } catch(Exception ex) {
                log.error("Laden van bestand " + zip + " is mislukt.", ex);
                String opmerkingen = bgtv3Loader.getOpmerkingen()
                        + "\nLaden van bestand " + zip + " is mislukt: " + ex.getLocalizedMessage();
                stagingProxy.updateLaadProcesStatus(lp, LaadProces.STATUS.RSGB_BGT_NOK, opmerkingen);
            }
        } else {
            log.warn("LaadProces " + lp.getId() + " van soort " + lp.getSoort() + " met status: " + lp.getStatus() + " is overgeslagen.");
        }
    }

    public void init() throws SQLException {
        GeometryJdbcConverter geomjdbc = GeometryJdbcConverterFactory.getGeometryJdbcConverter(dataSourceRsgbBgt.getConnection());

        params.put("jndiReferenceName", "java:comp/env/jdbc/brmo/rsgbbgt");
        params.put("dbtype", geomjdbc.getGeotoolsDBTypeName());
        params.put("schema", geomjdbc.getSchema());

        bgtv3Loader = new BGTv3Loader(params);
        //gmlLoader.setIsOracle(geomjdbc.getGeotoolsDBTypeName().toLowerCase().contains("oracle"));
        //gmlLoader.setIsMSSQL(geomjdbc.getGeotoolsDBTypeName().toLowerCase().contains("sqlserver"));
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
            Arrays.sort(lpIDs);
            for (long id : lpIDs) {
                this.transform(id);
                count++;
                if (listener != null) {
                    listener.progress(count);
                }
            }
        } catch (Exception e) {
            log.error("Fout tijdens verwerken BGTv3 laadprocessen", e);
            if (listener != null) {
                listener.exception(e);
            }
        }
    }
}
