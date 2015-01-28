/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.persistence.staging;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.ElementCollection;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.hibernate.annotations.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generieke beschrijving van een automatisch proces.
 *
 * @author Mark Prins <mark@b3partners.nl>
 *
 * @note Tabel "automatisch_proces" in de database.
 *
 */
@MappedSuperclass
@DiscriminatorColumn(name = "dtype", discriminatorType = DiscriminatorType.STRING, length = 255)
public abstract class AutomatischProces implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(AutomatischProces.class);

    public enum ProcessingStatus {

        PROCESSING("processing"), WAITING("waiting"), ONBEKEND("onbekend"), NULL(""), ERROR("error");

        private String status;

        ProcessingStatus(String status) {
            this.status = status;
        }
    }

    @Id
    @GeneratedValue
    private Long id;

    /**
     * @note Tabel "automatisch_proces_config" in de database.
     */
    @ElementCollection
    @JoinTable(joinColumns = @JoinColumn(name = "proces_id"))
    private Map<String, String> config = new HashMap<String, String>();

    /**
     * laatste run tijdtip vasthouden ten behoeve van logging en rapportage.
     */
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastrun;

    /**
     * status van de laatste run.
     */
    @Enumerated(EnumType.STRING)
    private ProcessingStatus status = ProcessingStatus.NULL;

    /**
     * samenvatting van de laatste run.
     */
    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    private String samenvatting;

    /**
     * pad naar de laatste logfile.
     */
    private String logfile;

    // <editor-fold defaultstate="collapsed" desc="getters and setters">
    public Map<String, String> getConfig() {
        return config;
    }

    public void setConfig(Map<String, String> config) {
        this.config = config;
    }

    public Date getLastrun() {
        return lastrun;
    }

    public void setLastrun(Date lastrun) {
        this.lastrun = lastrun;
    }

    public ProcessingStatus getStatus() {
        return status;
    }

    public void setStatus(ProcessingStatus status) {
        this.status = status;
    }

    public String getSamenvatting() {
        return samenvatting;
    }

    public void setSamenvatting(String samenvatting) {
        this.samenvatting = samenvatting;
    }

    public String getLogfile() {
        return logfile;
    }

    public void setLogfile(String logfile) {
        this.logfile = logfile;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    //</editor-fold>
}
