package nl.b3p.brmo.service.stripes;

import java.util.List;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.validation.Validate;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.entity.LaadProces;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.service.util.ConfigUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.stripesstuff.stripersist.Stripersist;

/**
 *
 * @author Boy de Wit
 */
public class LaadProcesActionBean implements ActionBean {

    private static final Log log = LogFactory.getLog(LaadProcesActionBean.class);

    private ActionBeanContext context;
    private List<LaadProces> processen;

    private long[] selectedIds;

    @Validate
    private int page;
    @Validate
    private int start;
    @Validate
    private int limit;
    @Validate
    private String sort;
    @Validate
    private String dir;
    @Validate
    private JSONArray filter;
    @Validate
    private JSONObject changedItem;

    @DefaultHandler
    public Resolution list() {
        return new ForwardResolution("/WEB-INF/jsp/laadproces/list.jsp");
    }

    public Resolution delete() {
        if (selectedIds != null) {

            BrmoFramework brmo = null;
            try {
                DataSource dataSourceStaging = ConfigUtil.getDataSourceStaging();
                DataSource dataSourceRsgb = ConfigUtil.getDataSourceRsgb();

                brmo = new BrmoFramework(dataSourceStaging, dataSourceRsgb);

                for (Long id : selectedIds) {
                    brmo.delete(id);
                }

            } catch (BrmoException e) {

                log.error("Fout", e);
                return new ErrorResolution(500, "Er is een onherstelbare fout opgetreden. "
                            + "Contacteer uw applicatiebeheerder: "
                            + e.getLocalizedMessage());

            } finally {
                if (brmo != null) {
                    brmo.closeBrmoFramework();
                }
           }
        }

        final JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("success", true);
        return new StreamingResolution("application/json") {
            @Override
            public void stream(HttpServletResponse response) throws Exception {
                response.getWriter().print(jsonResponse.toString());
            }
        };
    }

    public Resolution getGridData() {

        // ophalen filters
        String filterSoort = "";
        String filterStatus = "";

        if (this.getFilter() != null) {
            for (int k = 0; k < this.getFilter().length(); k++) {
                JSONObject j = this.getFilter().getJSONObject(k);
                String property = j.getString("property");
                String value = j.getString("value");
                if (property.equals("soort")) {
                    filterSoort = value;
                }
                if (property.equals("status")) {
                    filterStatus = value;
                }
            }
        }

        final JSONObject grid = new JSONObject();
        BrmoFramework brmo = null;
        try {
            DataSource dataSourceStaging = ConfigUtil.getDataSourceStaging();
            long count = 0l;
            JSONArray jsonProcessen = new JSONArray();

            brmo = new BrmoFramework(dataSourceStaging, null);

            count = brmo.getCountLaadProcessen(sort, dir, filterSoort, filterStatus);

            processen = brmo.getLaadprocessen(page, start, limit, sort, dir, filterSoort,
                    filterStatus);

            for (LaadProces proces : processen) {
                JSONObject jsonObject = laadProces2Json(proces);
                jsonProcessen.put(jsonObject);
            }

            grid.put("total", count);
            grid.put("items", jsonProcessen);

        } catch (BrmoException e) {

            log.error("Fout", e);
            return new ErrorResolution(500, "Er is een onherstelbare fout opgetreden. "
                            + "Contacteer uw applicatiebeheerder: "
                            + e.getLocalizedMessage());

        } finally {
            if (brmo != null) {
                brmo.closeBrmoFramework();
            }
        }

        return new StreamingResolution("application/json") {
            @Override
            public void stream(HttpServletResponse response) throws Exception {
                response.getWriter().print(grid.toString());
            }
        };
    }

    private JSONObject laadProces2Json(LaadProces lp) {
        JSONObject json = new JSONObject();

        json.put("id", lp.getId());
        json.put("bestand_naam", lp.getBestandNaam());
        json.put("bestand_datum", lp.getBestandDatum());
        json.put("soort", lp.getSoort());
        json.put("status", lp.getStatus());
        json.put("volgorde", "nvt");
        json.put("opmerking", lp.getOpmerking());

        return json;
    }

    public Resolution saveRecord() {
        JSONObject item = this.getChangedItem();

        final EntityManager em = Stripersist.getEntityManager();
        nl.b3p.brmo.persistence.staging.LaadProces _lp = em.find(nl.b3p.brmo.persistence.staging.LaadProces.class, item.getLong("id"));
        _lp.setStatus(nl.b3p.brmo.persistence.staging.LaadProces.STATUS.valueOf(item.getString("status")));
        em.merge(_lp);
        em.getTransaction().commit();

        final JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("success", true);
        return new StreamingResolution("application/json") {
            @Override
            public void stream(HttpServletResponse response) throws Exception {
                response.getWriter().print(jsonResponse.toString());
            }
        };
    }

    public ActionBeanContext getContext() {
        return context;
    }

    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    public List<LaadProces> getProcessen() {
        return processen;
    }

    public void setProcessen(List<LaadProces> processen) {
        this.processen = processen;
    }

    public long[] getSelectedIds() {
        return selectedIds;
    }

    public void setSelectedIds(long[] selectedIds) {
        this.selectedIds = selectedIds;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getSort() {
        return sort;
    }

    public void setSort(String sort) {
        this.sort = sort;
    }

    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public JSONArray getFilter() {
        return filter;
    }

    public void setFilter(JSONArray filter) {
        this.filter = filter;
    }

    public JSONObject getChangedItem() {
        return changedItem;
    }

    public void setChangedItem(JSONObject changedItem) {
        this.changedItem = changedItem;
    }
}
