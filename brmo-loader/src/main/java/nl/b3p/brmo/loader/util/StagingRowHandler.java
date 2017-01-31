package nl.b3p.brmo.loader.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import nl.b3p.brmo.loader.entity.Bericht;
import nl.b3p.brmo.loader.entity.LaadProces;
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * creates laadproces or bericht bean from database row.
 *
 * @author Chris van Lith
 */
public class StagingRowHandler extends BasicRowProcessor {

    @Override
    public List toBeanList(ResultSet rs, Class clazz) {
        try {
            List newlist = new ArrayList();
            while (rs.next()) {
                newlist.add(toBean(rs, clazz));
            }
            return newlist;
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Object toBean(ResultSet rs, Class type) throws SQLException {
        
        //vind beschikbare kolomnamen
        ResultSetMetaData rsm = rs.getMetaData();
        IgnoreCaseStringList namesList = new IgnoreCaseStringList();
        for (int i = 1; i <= rsm.getColumnCount(); i++) {
            namesList.add(rsm.getColumnLabel(i));
        }

        if (type.getName().contains("LaadProces")) {
            LaadProces lp = new LaadProces();

            if (namesList.contains("id")) {
                lp.setId(rs.getLong("id"));
            }
            if (namesList.contains("bestand_naam")) {
                lp.setBestandNaam(rs.getString("bestand_naam"));
            }
            if (namesList.contains("bestand_datum")) {
                lp.setBestandDatum(rs.getDate("bestand_datum"));
            }
            if (namesList.contains("soort")) {
                lp.setSoort(rs.getString("soort"));
            }
            if (namesList.contains("gebied")) {
                lp.setGebied(rs.getString("gebied"));
            }
            if (namesList.contains("opmerking")) {
                lp.setOpmerking(rs.getString("opmerking"));
            }
            if (namesList.contains("status")) {
                lp.setStatus(LaadProces.STATUS.valueOf(rs.getString("status")));
            }
            if (namesList.contains("status_datum")) {
                lp.setStatusDatum(rs.getDate("status_datum"));
            }
            if (namesList.contains("contact_email")) {
                lp.setContactEmail(rs.getString("contact_email"));
            }

            return lp;
        }

        if (type.getName().contains("Bericht")) {
            Bericht b = null;
            if (namesList.contains("br_xml")) {
                b = new Bericht(rs.getString("br_xml"));
            } else {
                b = new Bericht("");
            }

            if (namesList.contains("id")) {
                b.setId(rs.getLong("id"));
            }
            if (namesList.contains("laadprocesid")) {
                b.setLaadProcesId(rs.getInt("laadprocesid"));
            }
            if (namesList.contains("object_ref")) {
                b.setObjectRef(rs.getString("object_ref"));
            }
            if (namesList.contains("datum")) {
                // gebruik getTimestamp() (en niet getDate()) om tijdinfo ook op te halen.
                // zie ook: https://stackoverflow.com/questions/3266530/jdbc-resultset-getdate-losing-precision
                b.setDatum(rs.getTimestamp("datum"));
            }
            if (namesList.contains("volgordenummer")) {
                b.setVolgordeNummer(rs.getInt("volgordenummer"));
            }
            if (namesList.contains("soort")) {
                b.setSoort(rs.getString("soort"));
            }
            if (namesList.contains("opmerking")) {
                b.setOpmerking(rs.getString("opmerking"));
            }
            if (namesList.contains("status")) {
                b.setStatus(Bericht.STATUS.valueOf(rs.getString("status")));
            }
            if (namesList.contains("status_datum")) {
                b.setStatusDatum(rs.getTimestamp("status_datum"));
            }
            if (namesList.contains("job_id")) {
                b.setJobId(rs.getString("job_id"));
            }
            if (namesList.contains("br_orgineel_xml")) {
                b.setBrOrgineelXml(rs.getString("br_orgineel_xml"));
            }
            if (namesList.contains("db_xml")) {
                b.setDbXml(rs.getString("db_xml"));
            }
            if (namesList.contains("xsl_version")) {
                b.setXslVersion(rs.getString("xsl_version"));
            }

            return b;
        }

        return null;
    }
    
    class IgnoreCaseStringList extends ArrayList<String> {

        @Override
        public boolean contains(Object o) {
            String paramStr = (String) o;
            for (String s : this) {
                if (paramStr.equalsIgnoreCase(s)) {
                    return true;
                }
            }
            return false;
        }
    }

}
