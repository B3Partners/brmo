package nl.b3p.brmo.service.stripes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sourceforge.stripes.action.*;
import net.sourceforge.stripes.controller.LifecycleStage;
import net.sourceforge.stripes.validation.Validate;
import nl.b3p.brmo.persistence.staging.GDS2OphaalProces;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stripesstuff.stripersist.EntityTypeConverter;
import org.stripesstuff.stripersist.Stripersist;
import nl.b3p.brmo.persistence.staging.ClobElement;

/**
 *
 * @author Matthijs Laan
 */
@StrictBinding
public class GDS2OphaalConfigActionBean implements ActionBean {
    private static final Log log = LogFactory.getLog(AutoProcessenActionBean.class);

    private static final String JSP = "/WEB-INF/jsp/beheer/gds2ophalen.jsp";

    private ActionBeanContext context;

    private List<GDS2OphaalProces> processen = new ArrayList();

    @Validate(converter=EntityTypeConverter.class)
    private GDS2OphaalProces proces;

    @Validate
    private Map<String,ClobElement> config = new HashMap<String,ClobElement>();

    @Before(stages = LifecycleStage.BindingAndValidation)
    public void load() {
        processen = Stripersist.getEntityManager().createQuery("from GDS2OphaalProces p order by p.id").getResultList();
    }

    @DefaultHandler
    public Resolution view() {

        if(proces != null) {
            config = proces.getConfig();
        }

        return new ForwardResolution(JSP);
    }

    @DontValidate
    public Resolution cancel() {
        proces = null;
        return new ForwardResolution(JSP);
    }

    @DontValidate
    public Resolution delete() {
        if(proces != null) {
            Stripersist.getEntityManager().remove(proces);
            load();
            Stripersist.getEntityManager().getTransaction().commit();
            proces = null;
            getContext().getMessages().add(new SimpleMessage("Proces is verwijderd"));
        }
        return new ForwardResolution(JSP);
    }

    @DontValidate
    public Resolution add(){
        return new ForwardResolution(JSP);
    }

    public Resolution save() {
        if(proces == null){
            proces = new GDS2OphaalProces();

        }

        proces.getConfig().clear();
        proces.getConfig().putAll(config);

        Stripersist.getEntityManager().persist(proces);
        load();
        Stripersist.getEntityManager().getTransaction().commit();


        getContext().getMessages().add(new SimpleMessage("Proces is opgeslagen"));

        return new ForwardResolution(JSP).addParameter("proces", proces.getId());
    }

    // <editor-fold defaultstate="collapsed" desc="getters en setters">
    @Override
    public ActionBeanContext getContext() {
        return context;
    }

    @Override
    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    public List<GDS2OphaalProces> getProcessen() {
        return processen;
    }

    public void setProcessen(List<GDS2OphaalProces> processen) {
        this.processen = processen;
    }

    public GDS2OphaalProces getProces() {
        return proces;
    }

    public void setProces(GDS2OphaalProces proces) {
        this.proces = proces;
    }

    public Map<String, ClobElement> getConfig() {
        return config;
    }

    public void setConfig(Map<String, ClobElement> config) {
        this.config = config;
    }
    // </editor-fold>
}
