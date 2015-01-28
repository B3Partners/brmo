package nl.b3p.brmo.loader.util;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import nl.b3p.brmo.loader.entity.Bericht;
import nl.b3p.brmo.loader.entity.LaadProces;
import org.apache.commons.dbutils.BasicRowProcessor;

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
        if (type.getName().contains("LaadProces")) {
            LaadProces lp = new LaadProces();

            lp.setId(rs.getLong("id"));
            lp.setBestandNaam(rs.getString("bestand_naam"));
            lp.setBestandDatum(rs.getDate("bestand_datum"));
            lp.setSoort(rs.getString("soort"));
            lp.setGebied(rs.getString("gebied"));
            lp.setOpmerking(rs.getString("opmerking"));
            lp.setStatus(LaadProces.STATUS.valueOf(rs.getString("status")));
            lp.setStatusDatum(rs.getDate("status_datum"));
            lp.setContactEmail(rs.getString("contact_email"));

            return lp;
        }

        if (type.getName().contains("Bericht")) {
            Bericht b = new Bericht(rs.getString("br_xml"));

            b.setId(rs.getLong("id"));
            b.setLaadProcesId(rs.getInt("laadprocesid"));
            b.setObjectRef(rs.getString("object_ref"));
            b.setDatum(rs.getDate("datum"));
            b.setVolgordeNummer(rs.getInt("volgordenummer"));
            b.setSoort(rs.getString("soort"));
            b.setOpmerking(rs.getString("opmerking"));
            b.setStatus(Bericht.STATUS.valueOf(rs.getString("status")));
            b.setStatusDatum(rs.getDate("status_datum"));
            b.setJobId(rs.getString("job_id"));
            b.setBrOrgineelXml(rs.getString("br_orgineel_xml"));
            b.setDbXml(rs.getString("db_xml"));
            b.setXslVersion(rs.getString("xsl_version"));

            return b;
        }

        return null;
    }
}
