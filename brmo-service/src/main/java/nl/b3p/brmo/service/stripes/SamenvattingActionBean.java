/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.b3p.brmo.service.stripes;

import javax.persistence.EntityManager;
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ActionBeanContext;
import net.sourceforge.stripes.action.DefaultHandler;
import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.Resolution;
import nl.b3p.brmo.persistence.staging.AutomatischProces;
import org.stripesstuff.stripersist.Stripersist;

/**
 * Toont de samenvatting/log van een automatisch proces.
 *
 * @author Mark Prins <mark@b3partners.nl>
 */
public class SamenvattingActionBean implements ActionBean {

    private AutomatischProces proces;

    private static final String JSP = "/WEB-INF/jsp/beheer/samenvatting.jsp";

    private ActionBeanContext context;

    private Long procesId;

    private String newLine = AutomatischProces.LOG_NEWLINE;

    @DefaultHandler
    public Resolution view() {
        EntityManager em = Stripersist.getEntityManager();
        proces = em.find(AutomatischProces.class, procesId);

        return new ForwardResolution(JSP);
    }

    // <editor-fold defaultstate="collapsed" desc="getters and setters">
    /**
     * {@inheritDoc }
     */
    @Override
    public void setContext(ActionBeanContext context) {
        this.context = context;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public ActionBeanContext getContext() {
        return this.context;
    }

    public AutomatischProces getProces() {
        return proces;
    }

    public void setProces(AutomatischProces proces) {
        this.proces = proces;
    }

    public Long getProcesId() {
        return procesId;
    }

    public void setProcesId(Long procesId) {
        this.procesId = procesId;
    }

    public String getNewLine(){
        return this.newLine;
    }
    //</editor-fold>

}
