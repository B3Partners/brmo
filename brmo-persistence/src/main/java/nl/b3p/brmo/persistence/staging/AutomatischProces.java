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
import javax.persistence.MapKeyJoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.hibernate.annotations.Type;

/**
 * Generieke beschrijving van een automatisch proces.
 *
 * @author mprins
 *
 * @note Tabel "automatisch_proces" in de database.
 *
 */
@MappedSuperclass
@DiscriminatorColumn(name = "dtype", discriminatorType = DiscriminatorType.STRING, length = 255)
public abstract class AutomatischProces implements Serializable {

    /**
     * newline string ({@value LOG_NEWLINE}) voor de logberichten en
     * samenvatting.
     */
    public static final String LOG_NEWLINE = "\n";

    public enum ProcessingStatus {

        PROCESSING("PROCESSING"), WAITING("WAITING"), ONBEKEND("ONBEKEND"), ERROR("ERROR");

        private final String status;

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
    //volgende annotatie wordt nu niet uitgevoerd, mogelijk volgende hibernate versie
    //dit is alleen nodig voor sql server, script faalt bij maken primary key
    @MapKeyJoinColumn(nullable=false)
    // Element wrapper required because of http://opensource.atlassian.com/projects/hibernate/browse/JPA-11
    private Map<String, ClobElement> config = new HashMap<String, ClobElement>();

    /**
     * laatste run tijdtip vasthouden ten behoeve van logging en rapportage.
     */
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastrun;

    /**
     * status van de laatste run.
     */
    @Enumerated(EnumType.STRING)
    private ProcessingStatus status = ProcessingStatus.WAITING;

    /**
     * samenvatting van de laatste run.
     */
    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    private String samenvatting;

    /**
     * logfile
     */
    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    private String logfile;

    /**
     * cron expressie voor planning.
     */
    private String cronExpressie;

    /**
     * update samenvatting en logfile in één keer, waarbij de logfile aangevuld
     * wordt met de samenvatting.
     *
     * @param samenvatting de nieuwe samenvatting
     * @todo auto-truncate van de clob? oracle CLOB is maximaal 4 GB - 1) *
     * DB_BLOCK_SIZE
     * (http://docs.oracle.com/cd/B19306_01/server.102/b14237/limits001.htm#i287903)
     * ,posgres CLOB (text type) is maximaal limitless
     * (http://www.postgresql.org/docs/9.3/static/datatype-character.html)
     */
    public void updateSamenvattingEnLogfile(String samenvatting) {
        if (samenvatting == null) {
            samenvatting = "";
        }
        this.setSamenvatting(samenvatting);
        if (this.logfile == null) {
            this.setLogfile(samenvatting);
        } else {
            this.setLogfile(this.logfile + LOG_NEWLINE
                    // +"----------------"+ LOG_NEWLINE
                    + samenvatting);
        }

    }

    /**
     * Voeg een regel toe aan de log en de samenvatting. <strong>DOET
     * NIETS</strong> op dit moment.
     *
     * @todo make this work
     * @deprecated doet niets!
     *
     * @param line toe te voegen regel
     */
    @Deprecated
    public void addLogLine(String line) {
        //this.setLogfile(this.getLogfile() + LOG_NEWLINE + line);
    }

    // <editor-fold defaultstate="collapsed" desc="getters and setters">
    public Map<String, ClobElement> getConfig() {
        return config;
    }

    public void setConfig(Map<String, ClobElement> config) {
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

    public String getCronExpressie() {
        return cronExpressie;
    }

    public void setCronExpressie(String cronExpressie) {
        this.cronExpressie = cronExpressie;
    }

    //</editor-fold>
}
