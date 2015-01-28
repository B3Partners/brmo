/*
 * Copyright (C) 2012-2015 B3Partners B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.b3p.brmo.persistence.auth;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

/**
 *
 * @author Matthijs Laan
 */
@Entity
@Table(name = "groep_")
public class Groep implements Serializable {

    @Id
    private String naam;

    @Lob
    @org.hibernate.annotations.Type(type = "org.hibernate.type.StringClobType")
    private String beschrijving;

    @ManyToMany(mappedBy = "groepen")
    private Set<Gebruiker> leden = new HashSet<Gebruiker>();

    public String getBeschrijving() {
        return beschrijving;
    }

    public void setBeschrijving(String beschrijving) {
        this.beschrijving = beschrijving;
    }

    public String getNaam() {
        return naam;
    }

    public void setNaam(String naam) {
        this.naam = naam;
    }

    public Set<Gebruiker> getLeden() {
        return leden;
    }

    public void setLeden(Set<Gebruiker> leden) {
        this.leden = leden;
    }
}
