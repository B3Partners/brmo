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

    /**
     * newline string ({@value LOG_NEWLINE}) voor de logberichten en
     * samenvatting.
     */
    public static final String LOG_NEWLINE = "\n";

    public enum ProcessingStatus {

        PROCESSING("PROCESSING"), WAITING("WAITING"), ONBEKEND("ONBEKEND"), NULL(""), ERROR("ERROR");

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
    @JoinTable(joinColumns=@JoinColumn(name="proces_id"))
    // Element wrapper required because of http://opensource.atlassian.com/projects/hibernate/browse/JPA-11
    private Map<String,ClobElement> config = new HashMap<String,ClobElement>();

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
     * logfile
     */
    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    private String logfile;

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

    public void execute() {
        execute(new ProgressUpdateListener() {

            @Override
            public void total(long total) {
            }

            @Override
            public void progress(long progress) {
            }

            @Override
            public void exception(Throwable t) {
            }

            @Override
            public void updateStatus(String status) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public void addLog(String log) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
    }

    public abstract void execute(ProgressUpdateListener listener);

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

    //</editor-fold>
}
