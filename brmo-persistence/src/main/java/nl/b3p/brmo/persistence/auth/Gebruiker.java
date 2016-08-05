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
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

/**
 *
 * @author Matthijs Laan
 */
@Entity
@Table(name = "gebruiker_")
public class Gebruiker implements Serializable {

    public static final int MIN_PASSWORD_LENGTH = 8;
    private static final String DIGEST_ALGORITM = "SHA-1";
    private static final String DIGEST_CHARSET = "UTF-8";

    @Id
    private String gebruikersnaam;

    private String wachtwoord;

    @ManyToMany
    @JoinTable(name = "gebruiker_groepen", joinColumns = @JoinColumn(name = "gebruikersnaam"), inverseJoinColumns = @JoinColumn(name = "groep_"))
    private Set<Groep> groepen = new HashSet<Groep>();

    //    @ElementCollection
    //    @JoinTable(joinColumns=@JoinColumn(name="gebruikersnaam"))
    //    private Map<String,String> details = new HashMap<String,String>();
    /**
     *
     * @param password wachtwoord
     * @throws NoSuchAlgorithmException als er geen DIGEST_ALGORITM bestaat
     * @throws UnsupportedEncodingException als er een onbekendeencodingsfout
     * optreed
     */
    public void changePassword(String password) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = (MessageDigest) MessageDigest.getInstance(DIGEST_ALGORITM);
        md.update(password.getBytes(DIGEST_CHARSET));
        byte[] digest = md.digest();

        /* Converteer byte array naar hex-weergave */
        StringBuilder sb = new StringBuilder(digest.length * 2);
        for (int i = 0; i < digest.length; i++) {
            sb.append(Integer.toHexString(digest[i] >> 4 & 0xf)); /* and mask met 0xf nodig door sign-extenden van bytes... */
            sb.append(Integer.toHexString(digest[i] & 0xf));
        }
        setWachtwoord(sb.toString());
    }

    public String getWachtwoord() {
        return wachtwoord;
    }

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
