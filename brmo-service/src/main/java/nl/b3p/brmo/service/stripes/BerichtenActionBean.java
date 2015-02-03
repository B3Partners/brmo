package nl.b3p.brmo.service.stripes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletResponse;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.validation.Validate;
import nl.b3p.brmo.persistence.staging.Bericht;
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

    /**
     * @todo naar de JPA opvolger van BrmoFramework of StagingProxy overbrengen.
     * @param sort
     * @param dir
     * @param filterSoort
     * @param filterStatus
     * @return
     */
    private long getCountBerichten(String sort, String dir, String filterSoort, String filterStatus) {
        // sort en dir worden genegeerd, net als in StagingProxy..
        final EntityManager em = Stripersist.getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Bericht> from = cq.from(Bericht.class);

        List<Predicate> predicates = new ArrayList<Predicate>();
        if(!filterSoort.isEmpty()) {
            List<String> berichtSoorten = new ArrayList<String>();
            // soort in ...
            String[] soorten = filterSoort.split(",");
            berichtSoorten.addAll(Arrays.asList(soorten));
            Predicate predicate = from.get("soort").in(berichtSoorten);
            predicates.add(predicate);
        }

        if(!filterStatus.isEmpty()) {
            List<Bericht.STATUS> berichtStatussen = new ArrayList<Bericht.STATUS>();
            // status in...
            String[] statussen = filterStatus.split(",");
            for (String s:statussen){
                berichtStatussen.add(Bericht.STATUS.valueOf(s));
            }
            Predicate predicate = from.get("status").in(berichtStatussen);
            predicates.add(predicate);
        }

        CriteriaQuery<Long> select = cq.select(cb.count(from));
        select.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));
        return em.createQuery(select).getSingleResult();
    }

    /**
     * @todo naar de JPA opvolger van BrmoFramework of StagingProxy overbrengen.
     * @todo ge-dupliceerde code uit this#getCountBerichten uitfilteren
     * @param page
     * @param start
     * @param limit
     * @param sort
     * @param dir
     * @param filterSoort
     * @param filterStatus
     * @return
     */
    private List<Bericht> getBerichten(int page, int start, int limit, String sort, String dir, String filterSoort, String filterStatus){
        // page word genegeerd, net als in StagingProxy#buildFilterSql
        final EntityManager em = Stripersist.getEntityManager();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Bericht> cq = cb.createQuery(Bericht.class);
        Root<Bericht> from = cq.from(Bericht.class);

        // handle filterSoort
        List<Predicate> predicates = new ArrayList<Predicate>();
        if(!filterSoort.isEmpty()) {
            List<String> berichtSoorten = new ArrayList<String>();
            // soort in ...
            String[] soorten = filterSoort.split(",");
            berichtSoorten.addAll(Arrays.asList(soorten));
            Predicate predicate = from.get("soort").in(berichtSoorten);
            predicates.add(predicate);
        }
        // handle filterStatus
        if(!filterStatus.isEmpty()) {
            List<Bericht.STATUS> berichtStatussen = new ArrayList<Bericht.STATUS>();
            // status in...
            String[] statussen = filterStatus.split(",");
            for (String s:statussen){
                berichtStatussen.add(Bericht.STATUS.valueOf(s));
            }
            Predicate predicate = from.get("status").in(berichtStatussen);
            predicates.add(predicate);
        }
        cq.where(cb.and(predicates.toArray(new Predicate[predicates.size()])));

        // handle sort & dir
        if(sort != null && dir != null){
            if(dir.equalsIgnoreCase("ASC")){
                cq.orderBy(cb.asc(from.get(sort)));
            }else{
                cq.orderBy(cb.desc(from.get(sort)));
            }

        }


        TypedQuery<Bericht> q = em.createQuery(cq);

        if(start > 0 ){
            // handle start  & limit
            q.setFirstResult(start);
        }
        if(limit > 0){
            q.setMaxResults(limit);
        }
        return q.getResultList();
    }

    public Resolution getGridData() {

        // ophalen filters
        String filterSoort = "";
        String filterStatus = "";

        if (this.getFilter() != null) {
            for (int k = 0; k < this.getFilter().length(); k++) {
                JSONObject j = this.getFilter().getJSONObject(k);
                String property = j.getString("property");
                Object value = j.opt("value"); //JSONARRAY ["STAGING_OK","RSGB_OK","RSGB_NOK"] of string
                String stringValue = null;
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

        long count = 0l;
        JSONArray jsoNBerichten = new JSONArray();

        count = this.getCountBerichten(sort, dir, filterSoort, filterStatus);

        berichten = this.getBerichten(page, start, limit, sort, dir, filterSoort,
                    filterStatus);

        for (Bericht b : berichten) {
            JSONObject jsonObject = bericht2Json(b);
            jsoNBerichten.put(jsonObject);
        }

        final JSONObject grid = new JSONObject();
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
        json.put("object_ref", ber.getObject_ref());
        json.put("datum", ber.getDatum());
        json.put("soort", ber.getSoort());
        json.put("status", ber.getStatus());
        json.put("volgordenummer", ber.getVolgordenummer());

        return json;
    }

    public Resolution saveRecord() {
        JSONObject item = this.getChangedItem();
        /**
         * @TODO: save the changed item
         */
        final JSONObject jsonResponse = new JSONObject();
        jsonResponse.put("success", true);
        return new StreamingResolution("application/json") {
            @Override
            public void stream(HttpServletResponse response) throws Exception {
                response.getWriter().print(jsonResponse.toString());
            }
        };
    }

    public Resolution log() {
        JSONObject jsonObj = new JSONObject();
        jsonObj.put("success", Boolean.FALSE);
        Bericht bericht = Stripersist.getEntityManager().find(Bericht.class,selectedIds[0]);
        jsonObj = bericht2Json(bericht);
        jsonObj.put("opmerking", bericht.getOpmerking());
        jsonObj.put("success", Boolean.TRUE);
        final String returnValue =  jsonObj.toString();
        return new StreamingResolution("application/json") {
            @Override
            public void stream(HttpServletResponse response) throws Exception {
                response.getWriter().print(returnValue);
            }
        };
    }

    // <editor-fold defaultstate="collapsed" desc="getters and setters">

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

    //</editor-fold>
}
