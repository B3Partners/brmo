/*
 * Copyright (C) 2022 B3Partners B.V.
 */
package nl.b3p.brmo.persistence.staging;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "nhr_laadproces")
public class NHRLaadProces implements Serializable {

    @Temporal(TemporalType.TIMESTAMP)
    private Date datum;

    @Temporal(TemporalType.TIMESTAMP)
    private Date laatstGeprobeerd;

    @Temporal(TemporalType.TIMESTAMP)
    private Date volgendProberen;

    private Integer probeerAantal;

    @Id
    @Column(nullable = false)
    private String kvkNummer;

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String exception;

    // <editor-fold defaultstate="collapsed" desc="getters and setters">
    public Date getDatum() {
        return datum;
    }

    public void setDatum(Date datum) {
        this.datum = datum;
    }

    public Date getLaatstGeprobeerd() {
        return laatstGeprobeerd;
    }

    public void setLaatstGeprobeerd(Date laatstGeprobeerd) {
        this.laatstGeprobeerd = laatstGeprobeerd;
    }

    public Date getVolgendProberen() {
        return volgendProberen;
    }

    public void setVolgendProberen(Date volgendProberen) {
        this.volgendProberen = volgendProberen;
    }

    public Integer getProbeerAantal() {
        return probeerAantal;
    }

    public void setProbeerAantal(Integer probeerAantal) {
        this.probeerAantal = probeerAantal;
    }

    public String getKvkNummer() {
        return kvkNummer;
    }

    public void setKvkNummer(String kvkNummer) {
        this.kvkNummer = kvkNummer;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

//</editor-fold>
}
