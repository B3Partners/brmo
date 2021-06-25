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

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Matthijs Laan
 */
@Entity
@Table(name = "gebruiker_")
public class Gebruiker implements Serializable {

    @Id
    private String gebruikersnaam;

    private String wachtwoord;

    @ManyToMany
    @JoinTable(name = "gebruiker_groepen", joinColumns = @JoinColumn(name = "gebruikersnaam"), inverseJoinColumns = @JoinColumn(name = "groep_"))
    private Set<Groep> groepen = new HashSet<>();

    public String getWachtwoord() {
        return wachtwoord;
    }

    /**
     * Stel nieuw wachtwoord in. Het versleutelen van het wachtwoord naar een hash dient al uitgevoerd te zijn en
     * moet conform de geconfigureerde {@code CredentialHandler} van de security realm gedaan zijn om te kunnen werken.
     *
     * @param wachtwoord een versleuteld/gehashed wachtwoord
     */
    public void setWachtwoord(String wachtwoord) {
        this.wachtwoord = wachtwoord;
    }

    public Set<Groep> getGroepen() {
        return groepen;
    }

    public void setGroepen(Set<Groep> groepen) {
        this.groepen = groepen;
    }

    public String getGebruikersnaam() {
        return gebruikersnaam;
    }

    public void setGebruikersnaam(String gebruikersnaam) {
        this.gebruikersnaam = gebruikersnaam;
    }
}
