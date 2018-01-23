/*
 * Copyright (C) 2016 - 2017 B3Partners B.V.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package nl.b3p.topnl.converters;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import nl.b3p.topnl.TopNLType;
import nl.b3p.topnl.entities.FunctioneelGebied;
import nl.b3p.topnl.entities.Gebouw;
import nl.b3p.topnl.entities.GeografischGebied;
import nl.b3p.topnl.entities.Hoogte;
import nl.b3p.topnl.entities.Inrichtingselement;
import nl.b3p.topnl.entities.Plaats;
import nl.b3p.topnl.entities.PlanTopografie;
import nl.b3p.topnl.entities.RegistratiefGebied;
import nl.b3p.topnl.entities.Relief;
import nl.b3p.topnl.entities.Spoorbaandeel;
import nl.b3p.topnl.entities.Terrein;
import nl.b3p.topnl.entities.TopNLEntity;
import nl.b3p.topnl.entities.Waterdeel;
import nl.b3p.topnl.entities.Wegdeel;
import nl.b3p.topnl.top250nl.BRTJaNeeWaardeType;
import nl.b3p.topnl.top250nl.CodeType;
import nl.b3p.topnl.top250nl.FeatureMemberType;
import nl.b3p.topnl.top250nl.FunctioneelGebiedType;
import nl.b3p.topnl.top250nl.GebouwType;
import nl.b3p.topnl.top250nl.GeografischGebiedType;
import nl.b3p.topnl.top250nl.HoogteType;
import nl.b3p.topnl.top250nl.Identificatie;
import nl.b3p.topnl.top250nl.InrichtingselementType;
import nl.b3p.topnl.top250nl.PlaatsType;
import nl.b3p.topnl.top250nl.RegistratiefGebiedType;
import nl.b3p.topnl.top250nl.ReliefType;
import nl.b3p.topnl.top250nl.SpoorbaandeelType;
import nl.b3p.topnl.top250nl.TerreinType;
import nl.b3p.topnl.top250nl.Top250NlObjectType;
import nl.b3p.topnl.top250nl.WaterdeelType;
import nl.b3p.topnl.top250nl.WegdeelType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author Meine Toonen
 */
public class Top250NLConverter extends Converter {

    protected final static Log log = LogFactory.getLog(Top250NLConverter.class);
    
    protected SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    @Override
    public List<TopNLEntity> convert(List jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        List<TopNLEntity> entities = null;
        if (jaxbObject instanceof ArrayList) {
            entities = convertFeatureCollection(jaxbObject);
        } else {
            entities = new ArrayList<>();
            entities.add(convertObject(jaxbObject));
        }
        return entities;
    }

    @Override
    public List<TopNLEntity> convertFeatureCollection(List featureMembers) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        
        List<TopNLEntity> entities = new ArrayList<>();

        for (Object fm : featureMembers) {
            FeatureMemberType featureMember = (FeatureMemberType)fm;
            JAXBElement jaxbObject = featureMember.getTop250NlObject();
            Object obj = jaxbObject.getValue();
            TopNLEntity entity = convertObject(obj);
            entities.add(entity);
        }

        return entities;
    }

    @Override
    public TopNLEntity convertObject(Object featureMember) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        TopNLEntity entity = null;
        if(featureMember instanceof FeatureMemberType){
            FeatureMemberType fm = (FeatureMemberType)featureMember;
            JAXBElement jaxbObject = fm.getTop250NlObject();
            return convertObject(jaxbObject.getValue());
        }else if (featureMember instanceof HoogteType) {
            entity = convertHoogte(featureMember);
        } else if (featureMember instanceof FunctioneelGebiedType) {
            entity = convertFunctioneelGebied(featureMember);
        } else if (featureMember instanceof GebouwType) {
            entity = convertGebouw(featureMember);
        } else if (featureMember instanceof GeografischGebiedType) {
            entity = convertGeografischGebied(featureMember);
        } else if (featureMember instanceof InrichtingselementType) {
            entity = convertInrichtingselement(featureMember);
        } else if (featureMember instanceof PlaatsType) {
            entity = convertPlaats(featureMember);
        } else if (featureMember instanceof RegistratiefGebiedType) {
            entity = convertRegistratiefGebied(featureMember);
        } else if (featureMember instanceof ReliefType) {
            entity = convertRelief(featureMember);
        } else if (featureMember instanceof SpoorbaandeelType) {
            entity = convertSpoorbaandeel(featureMember);
        } else if (featureMember instanceof TerreinType) {
            entity = convertTerrein(featureMember);
        } else if (featureMember instanceof WaterdeelType) {
            entity = convertWaterdeel(featureMember);
        } else if (featureMember instanceof WegdeelType) {
            entity = convertWegdeel(featureMember);
        } else {
            throw new IllegalArgumentException("Type not recognized: " + featureMember.getClass());
        }
        log.debug("Converted type " + entity.getClass() + ", identificatie: " + entity.getIdentificatie());
        entity.setTopnltype(TopNLType.TOP250NL.getType());
        return entity;
    }

    @Override
    public Hoogte convertHoogte(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        HoogteType h = (HoogteType) jaxbObject;

        Hoogte hoogte = new Hoogte();
        convertTop250NlObjectType(h, hoogte);

        hoogte.setTypeHoogte(h.getTypeHoogte().getValue());

        hoogte.setHoogte(h.getHoogte());
        hoogte.setObjectEindTijd(h.getObjectEindTijd() != null ? h.getObjectEindTijd().getTime() : null);
        hoogte.setReferentieVlak(h.getReferentievlak() != null ? h.getReferentievlak().getValue() : null);

        Element el = h.getGeometrie();
        Geometry geom = gc.convertGeometry(el);
        hoogte.setGeometrie(geom);

        return hoogte;
    }

    public String convertIdentificatie(Identificatie identificatie) {
        String idString = identificatie.getNEN3610ID().getNamespace() + "." + identificatie.getNEN3610ID().getLokaalID();
        return idString;
    }

    private void convertTop250NlObjectType(Top250NlObjectType type, TopNLEntity entity) {
        entity.setIdentificatie(convertIdentificatie(type.getIdentificatie()));
        entity.setBrontype(type.getBrontype().getValue());
        entity.setBronactualiteit(type.getBronactualiteit().getTime());
        entity.setBronbeschrijving(type.getBronbeschrijving());
        entity.setBronnauwkeurigheid(type.getBronnauwkeurigheid());
        entity.setObjectBeginTijd(type.getObjectBeginTijd().getTime());
        entity.setVisualisatieCode(type.getVisualisatieCode() != null ? type.getVisualisatieCode().longValue() : null);
    }

    @Override
    public FunctioneelGebied convertFunctioneelGebied(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        FunctioneelGebiedType f = (FunctioneelGebiedType) jaxbObject;

        FunctioneelGebied fg = new FunctioneelGebied();
        convertTop250NlObjectType(f, fg);

        fg.setTypeFunctioneelGebied(f.getTypeFunctioneelGebied().getValue());
        fg.setNaamFries(String.join(",", f.getNaamFries()));
        fg.setNaamNL(String.join(",", f.getNaamNL()));
        fg.setSoortnaam(f.getSoortnaam());

        fg.setGeometrie(gc.convertGeometry(f.getGeometrie()));
        return fg;
    }

    @Override
    public Gebouw convertGebouw(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        GebouwType g = (GebouwType) jaxbObject;

        Gebouw gb = new Gebouw();
        convertTop250NlObjectType(g, gb);
        String types = "";
        for (CodeType codeType : g.getTypeGebouw()) {
            if (types.length() != 0) {
                types += ",";
            }
            types += codeType.getValue();
        }

        gb.setTypeGebouw(types);
        gb.setStatus(g.getStatus() != null ? g.getStatus().getValue() : null);
        gb.setFysiekVoorkomen(g.getFysiekVoorkomen() != null ? g.getFysiekVoorkomen().getValue() : null);
        gb.setHoogteklasse(g.getHoogteklasse() != null ? g.getHoogteklasse().getValue() : null);
        gb.setHoogte(g.getHoogte());
        gb.setSoortnaam(g.getSoortnaam());
        gb.setNaam(String.join(",", g.getNaam()));

        gb.setGeometrie(gc.convertGeometry(g.getGeometrie()));
        return gb;
    }

    @Override
    public GeografischGebied convertGeografischGebied(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        GeografischGebiedType g = (GeografischGebiedType) jaxbObject;

        GeografischGebied gb = new GeografischGebied();
        convertTop250NlObjectType(g, gb);

        gb.setGeometrie(gc.convertGeometry(g.getGeometrie()));

        gb.setNaamFries(String.join(",", g.getNaamFries()));
        gb.setNaamNL(String.join(",", g.getNaamNL()));
        gb.setTypeGeografischGebied(g.getTypeGeografischGebied().getValue());
        return gb;
    }

    @Override
    public Inrichtingselement convertInrichtingselement(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        InrichtingselementType i = (InrichtingselementType) jaxbObject;

        Inrichtingselement ie = new Inrichtingselement();
        convertTop250NlObjectType(i, ie);

        ie.setGeometrie(gc.convertGeometry(i.getGeometrie()));

        ie.setSoortnaam(i.getSoortnaam());
        ie.setTypeInrichtingselement(i.getTypeInrichtingselement().getValue());

        return ie;
    }

    @Override
    public Plaats convertPlaats(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        PlaatsType p = (PlaatsType) jaxbObject;

        Plaats pl = new Plaats();
        convertTop250NlObjectType(p, pl);

        pl.setGeometrie(gc.convertGeometry(p.getGeometrie()));

        pl.setAantalInwoners(p.getAantalinwoners() != null ? p.getAantalinwoners().longValue() : null);
        pl.setNaamFries(p.getNaamFries());
        pl.setNaamNL(p.getNaamNL());
        pl.setNaamOfficieel(p.getNaamOfficieel());
        pl.setTypeGebied(p.getTypeGebied().getValue());

        return pl;
    }

    @Override
    public RegistratiefGebied convertRegistratiefGebied(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        RegistratiefGebiedType r = (RegistratiefGebiedType) jaxbObject;

        RegistratiefGebied rg = new RegistratiefGebied();
        convertTop250NlObjectType(r, rg);

        rg.setGeometrie(gc.convertGeometry(r.getGeometrie()));

        rg.setNaamFries(String.join(",", r.getNaamFries()));
        rg.setNaamNL(String.join(",", r.getNaamNL()));
        rg.setNaamOfficieel(r.getNaamOfficieel());
        rg.setNummer(String.join(",", r.getNummer()));
        rg.setTypeRegistratiefGebied(r.getTypeRegistratiefGebied().getValue());

        return rg;
    }

    @Override
    public Relief convertRelief(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        ReliefType r = (ReliefType) jaxbObject;

        Relief rg = new Relief();
        convertTop250NlObjectType(r, rg);

        rg.setGeometrie((LineString) gc.convertGeometry(r.getLijnGeometrie()));

        rg.setHoogteklasse(r.getHoogteklasse().getValue());
        rg.setTypeRelief(r.getTypeRelief().getValue());

        return rg;
    }

    @Override
    public Spoorbaandeel convertSpoorbaandeel(Object jaxbObject) throws IOException, SAXException,
            ParserConfigurationException, TransformerException, ClassCastException {

        SpoorbaandeelType r = (SpoorbaandeelType) jaxbObject;

        Spoorbaandeel rg = new Spoorbaandeel();
        convertTop250NlObjectType(r, rg);

        rg.setGeometrie((LineString) gc.convertGeometry(r.getGeometrie()));

        rg.setTypeInfrastructuur(r.getTypeInfrastructuur().getValue());
        rg.setTypeSpoorbaan(r.getTypeSpoorbaan().getValue());
        rg.setFysiekVoorkomen(r.getFysiekVoorkomen() != null ? r.getFysiekVoorkomen().getValue() : null);
        rg.setSpoorbreedte(r.getSpoorbreedte().getValue());
        rg.setAantalSporen(r.getAantalSporen().getValue());
        rg.setVervoerfunctie(r.getVervoerfunctie().getValue());
        rg.setElektrificatie(r.getElektrificatie() == BRTJaNeeWaardeType.JA);
        rg.setStatus(r.getStatus().getValue());
        rg.setBrugnaam(r.getBrugnaam());
        rg.setTunnelnaam(r.getTunnelnaam());
        rg.setBaanvaknaam(r.getBaanvaknaam());

        return rg;
    }

    @Override
    public Terrein convertTerrein(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        TerreinType r = (TerreinType) jaxbObject;

        Terrein rg = new Terrein();
        convertTop250NlObjectType(r, rg);

        rg.setGeometrie((Polygon) gc.convertGeometry(r.getGeometrieVlak()));

        rg.setNaam(r.getNaam());
        rg.setTypeLandgebruik(r.getTypeLandgebruik() != null ? r.getTypeLandgebruik().getValue() : null);

        return rg;
    }

    @Override
    public Waterdeel convertWaterdeel(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        WaterdeelType r = (WaterdeelType) jaxbObject;

        Waterdeel rg = new Waterdeel();
        convertTop250NlObjectType(r, rg);

        rg.setGeometrie(gc.convertGeometry(r.getGeometrie()));

        rg.setTypeWater(r.getTypeWater() != null ? r.getTypeWater().getValue() : null);
        rg.setBreedteklasse(r.getBreedteklasse() != null ? r.getBreedteklasse().getValue() : null);
        rg.setFysiekVoorkomen(r.getFysiekVoorkomen() != null ? r.getFysiekVoorkomen().getValue() : null);
        rg.setVoorkomen(r.getVoorkomen() != null ? r.getVoorkomen().getValue() : null);
        rg.setGetijdeinvloed(r.getGetijdeinvloed() == BRTJaNeeWaardeType.JA);
        rg.setVaarwegklasse(r.getVaarwegklasse() != null ? r.getVaarwegklasse().getValue() : null);
        rg.setNaamOfficieel(r.getNaamOfficieel());
        rg.setNaamNL(String.join(",", r.getNaamNL()));
        rg.setNaamFries(String.join(",", r.getNaamFries()));
        rg.setIsBAGnaam(r.getIsBAGnaam() == BRTJaNeeWaardeType.JA);
        rg.setSluisnaam(r.getSluisnaam());
        rg.setBrugnaam(r.getBrugnaam());
        return rg;
    }

    @Override
    public Wegdeel convertWegdeel(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        WegdeelType r = (WegdeelType) jaxbObject;

        Wegdeel rg = new Wegdeel();
        convertTop250NlObjectType(r, rg);

        String types = "";
        for (CodeType codeType : r.getFysiekVoorkomen()) {
            if (types.length() != 0) {
                types += ",";
            }
            types += codeType.getValue();
        }
        rg.setFysiekVoorkomen(types);

        rg.setGeometrie(gc.convertGeometry(r.getGeometrie()));
        rg.setTypeInfrastructuur(r.getTypeInfrastructuur() != null ? r.getTypeInfrastructuur().getValue() : null);
        rg.setTypeWeg(r.getTypeWeg() != null ? r.getTypeWeg().getValue() : null);
        rg.setHoofdverkeersgebruik(r.getHoofdverkeersgebruik() != null ? r.getHoofdverkeersgebruik().getValue() : null);
        rg.setVerhardingsbreedteklasse(r.getVerhardingsbreedteklasse() != null ? r.getVerhardingsbreedteklasse().getValue() : null);
        rg.setGescheidenRijbaan(r.getGescheidenRijbaan() == BRTJaNeeWaardeType.JA);
        rg.setVerhardingstype(r.getVerhardingstype() != null ? r.getVerhardingstype().getValue() : null);
        rg.setAantalRijstroken(r.getAantalRijstroken() != null ? r.getAantalRijstroken().longValue() : null);
        rg.setStatus(r.getStatus() != null ? r.getStatus().getValue() : null);
        rg.setNaam(String.join(",",r.getNaam()));
        rg.setIsBAGnaam(r.getIsBAGnaam() == BRTJaNeeWaardeType.JA);
        rg.setaWegnummer(String.join(",", r.getAWegnummer()));
        rg.setnWegnummer(String.join(",", r.getNWegnummer()));
        rg.seteWegnummer(String.join(",", r.getEWegnummer()));
        rg.setsWegnummer(String.join(",", r.getSWegnummer()));
        rg.setAfritnummer(r.getAfritnummer());
        rg.setAfritnaam(r.getAfritnaam());
        rg.setKnooppuntnaam(r.getKnooppuntnaam());
        rg.setBrugnaam(r.getBrugnaam());
        rg.setTunnelnaam(r.getTunnelnaam());
        return rg;
    }    

    @Override
    public PlanTopografie convertPlanTopografie(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
