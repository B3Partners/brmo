package nl.b3p.brmo.service.stripes;

import java.util.List;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.validation.Validate;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.entity.Bericht;
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
public class BerichtenActionBean implements ActionBean {

    private static final Log log = LogFactory.getLog(BerichtenActionBean.class);

    private ActionBeanContext context;
    private List<Bericht> berichten;

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

//    private String result;

    @DefaultHandler
    public Resolution list() {
        return new ForwardResolution("/WEB-INF/jsp/bericht/list.jsp");
    }

    public Resolution getGridData() throws BrmoException {

        // ophalen filters
        String filterSoort = "";
        String filterStatus = "";

        if (this.getFilter() != null) {
            for (int k = 0; k < this.getFilter().length(); k++) {
                JSONObject j = this.getFilter().getJSONObject(k);
                String property = j.getString("property");
                Object value = j.opt("value"); //JSONARRAY ["STAGING_OK","RSGB_OK","RSGB_NOK"] of string
                String stringValue = "";
                if (value instanceof String) {
                    stringValue = (String)value;
                } else if (value instanceof JSONArray) {
                    JSONArray jaValue = (JSONArray)value;
                    stringValue = jaValue.join(","); // lelijk
                    stringValue = stringValue.replaceAll("\"", "");
                }
                if (property.equals("soort")) {
                     filterSoort = stringValue;
                 }
                 if (property.equals("status")) {
                     filterStatus = stringValue;
                 }
             }
        }

        DataSource dataSourceStaging = ConfigUtil.getDataSourceStaging();
        long count = 0l;
        JSONArray jsoNBerichten = new JSONArray();
        BrmoFramework brmo = null;
        try {
            brmo = new BrmoFramework(dataSourceStaging, null);

            count = brmo.getCountBerichten(sort, dir, filterSoort, filterStatus);

            if (start < 0) {
                start = 0;
            }
            berichten = brmo.getBerichten(page, start, limit, 
                    (sort==null || sort.trim().isEmpty())?"id":sort, 
                    (dir==null || dir.trim().isEmpty())?"asc":dir, 
                    filterSoort,
                    filterStatus);

        } finally {
            if (brmo!=null) {
                brmo.closeBrmoFramework();
            }
        }
        for (Bericht b : berichten) {
            JSONObject jsonObject = bericht2Json(b);
            jsoNBerichten.put(jsonObject);
        }

        final JSONObject grid = new JSONObject();
        if (count<0)  {
            grid.put("virtualtotal", true);
            count = 0l;
        }
        grid.put("total", count);
        grid.put("items", jsoNBerichten);

        return new StreamingResolution("application/json") {
            @Override
            public void stream(HttpServletResponse response) throws Exception {
                response.getWriter().print(grid.toString());
            }
        };
    }

    private JSONObject bericht2Json(Bericht ber) {
        JSONObject json = new JSONObject();

        json.put("id", ber.getId());
        json.put("object_ref", ber.getObjectRef());
        json.put("datum", ber.getDatum());
        json.put("soort", ber.getSoort());
        json.put("status", ber.getStatus());
        json.put("volgordenummer", ber.getVolgordeNummer());

        return json;
    }

    public Resolution saveRecord() {
        JSONObject item = this.getChangedItem();

        final EntityManager em = Stripersist.getEntityManager();
        nl.b3p.brmo.persistence.staging.Bericht bericht = em.find(nl.b3p.brmo.persistence.staging.Bericht.class, item.getLong("id"));
        bericht.setStatus(nl.b3p.brmo.persistence.staging.Bericht.STATUS.valueOf(item.getString("status")));
        // TODO / VRAAG moeten we ook iets aan de log/opmerking van het bericht doen?
        // bericht.setOpmerking("");
        em.merge(bericht);
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

    public Resolution log() throws BrmoException{
        DataSource dataSourceStaging = ConfigUtil.getDataSourceStaging();
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("success", Boolean.FALSE);
        BrmoFramework brmo = new BrmoFramework(dataSourceStaging, null);
        try{
            Bericht bericht = brmo.getBerichtById(selectedIds[0]);
            jsonObj = bericht2Json(bericht);
            jsonObj.put("opmerking", bericht.getOpmerking());
            jsonObj.put("success", Boolean.TRUE);
        }finally{
            brmo.closeBrmoFramework();
        }
        final String returnValue =  jsonObj.toString();
        return new StreamingResolution("application/json") {
            @Override
            public void stream(HttpServletResponse response) throws Exception {
                response.getWriter().print(returnValue);
            }
        };
    }

    public ActionBeanContext getContext() {
        return context;
    }

    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    public List<Bericht> getBerichten() {
        return berichten;
    }

    public void setBerichten(List<Bericht> berichten) {
        this.berichten = berichten;
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



//    public String getResult() {
//        return result;
//    }
//
//    public void setResult(String result) {
//        this.result = result;
//    }

    public JSONObject getChangedItem() {
        return changedItem;
    }

    public void setChangedItem(JSONObject changedItem) {
        this.changedItem = changedItem;
    }
}
