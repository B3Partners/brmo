package nl.b3p.brmo.service.stripes;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.FileBean;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import net.sourceforge.stripes.action.StreamingResolution;
import net.sourceforge.stripes.validation.Validate;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.persistence.staging.NHRLaadProces;
import org.json.JSONArray;
import org.json.JSONObject;
import org.stripesstuff.stripersist.Stripersist;

public class NHRActionBean implements ActionBean {
    private ActionBeanContext context;

    private String[] selectedIds;
    @Validate
    private int start;
    @Validate
    private int limit;
    @Validate
    private String sort;
    @Validate
    private String dir;
    @Validate(required = true, on="upload")
    private FileBean file;

    @DefaultHandler
    public Resolution list() {
        return new ForwardResolution("/WEB-INF/jsp/nhr/list.jsp");
    }

    private long getCount() {
        EntityManager entityManager = Stripersist.getEntityManager();
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<NHRLaadProces> from = cq.from(NHRLaadProces.class);
        cq.select(cb.count(from));
        return entityManager.createQuery(cq).getSingleResult();
    }

    public Resolution getGridData() throws BrmoException {
        EntityManager entityManager = Stripersist.getEntityManager();
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<NHRLaadProces> cq = cb.createQuery(NHRLaadProces.class);
        Root<NHRLaadProces> from = cq.from(NHRLaadProces.class);

        if (sort != null) {
            if (dir == null || !dir.equalsIgnoreCase("desc")) {
                cq.orderBy(cb.asc(from.get(sort)));
            } else {
                cq.orderBy(cb.desc(from.get(sort)));
            }
        }

        if (start < 0) {
            start = 0;
        }

        if (limit <= 0) {
            limit = 40;
        }

        List<NHRLaadProces> procList = entityManager.createQuery(cq).setMaxResults(limit).setFirstResult(start).getResultList();
        JSONArray jsonBerichten = new JSONArray();

        for (NHRLaadProces b : procList) {
            jsonBerichten.put(laadproces2Json(b));
        }

        final JSONObject grid = new JSONObject();
        grid.put("total", getCount());
        grid.put("items", jsonBerichten);

        return new StreamingResolution("application/json") {
            @Override
            public void stream(HttpServletResponse response) throws Exception {
                response.getWriter().print(grid.toString());
            }
        };
    }

    public Resolution getLog() {
        EntityManager entityManager = Stripersist.getEntityManager();

        String responseString;
        if (selectedIds == null || selectedIds.length != 1) {
            responseString = "kan geen logs ophalen voor 0 of meer dan 1 item";
        } else {
            NHRLaadProces item = entityManager.find(NHRLaadProces.class, selectedIds[0]);
            if (item == null) {
                responseString = String.format("Log voor KVK nummer %s niet gevonden", selectedIds[0]);
            } else {
                responseString = item.getException();
            }
        }

        return new StreamingResolution("text/plain") {
            @Override
            public void stream(HttpServletResponse response) throws Exception {
                response.getWriter().print(responseString);
            }
        };
    }

    public Resolution runNow() throws BrmoException {
        EntityManager entityManager = Stripersist.getEntityManager();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<NHRLaadProces> cq = cb.createQuery(NHRLaadProces.class);
        Root<NHRLaadProces> from = cq.from(NHRLaadProces.class);

        CriteriaBuilder.In<String> in = cb.in(from.get("kvkNummer"));
        for (String item : selectedIds) {
            in.value(item);
        }
        cq.where(in);

        List<NHRLaadProces> procList = entityManager.createQuery(cq).getResultList();
        for (NHRLaadProces b : procList) {
            b.setVolgendProberen(new java.util.Date());
            b.setProbeerAantal(0);
        }

        entityManager.getTransaction().commit();
        return new StreamingResolution("text/plain") {
            @Override
            public void stream(HttpServletResponse response) throws Exception {
                response.getWriter().print("ok");
            }
        };
    }

    public Resolution upload() throws IOException {
        EntityManager entityManager = Stripersist.getEntityManager();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()))) {
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }

                NHRLaadProces proces;
                proces = entityManager.find(NHRLaadProces.class, line);
                if (proces == null) {
                    proces = new NHRLaadProces();
                }

                proces.setDatum(new Date());
                proces.setVolgendProberen(new Date());
                proces.setProbeerAantal(0);
                proces.setKvkNummer(line);
                entityManager.merge(proces);
            }
        }

        file.delete();
        entityManager.getTransaction().commit();
        return list();
    }

    private JSONObject laadproces2Json(NHRLaadProces laadproces) {
        JSONObject json = new JSONObject();

        json.put("datum", laadproces.getDatum());
        json.put("laatstGeprobeerd", laadproces.getLaatstGeprobeerd());
        json.put("volgendProberen", laadproces.getVolgendProberen());
        json.put("probeerAantal", laadproces.getProbeerAantal());
        json.put("kvkNummer", laadproces.getKvkNummer());

        return json;
    }

    public ActionBeanContext getContext() {
        return context;
    }

    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    public String[] getSelectedIds() {
        return selectedIds;
    }

    public void setSelectedIds(String[] selectedIds) {
        this.selectedIds = selectedIds;
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

    public FileBean getFile() {
        return file;
    }

    public void setFile(FileBean file) {
        this.file = file;
    }
}
