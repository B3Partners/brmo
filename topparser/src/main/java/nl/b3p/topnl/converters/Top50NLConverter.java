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

import nl.b3p.topnl.top50nl.FeatureMemberType;
import nl.b3p.topnl.top50nl.FunctioneelGebiedType;
import nl.b3p.topnl.top50nl.FysiekVoorkomenSpoorT50Type;
import nl.b3p.topnl.top50nl.FysiekVoorkomenWaterT50Type;
import nl.b3p.topnl.top50nl.FysiekVoorkomenWegT50Type;
import nl.b3p.topnl.top50nl.GebouwType;
import nl.b3p.topnl.top50nl.GeografischGebiedType;
import nl.b3p.topnl.top50nl.HoogteType;
import nl.b3p.topnl.top50nl.InrichtingselementType;
import nl.b3p.topnl.top50nl.JaNeeType;
import nl.b3p.topnl.top50nl.RegistratiefGebiedType;
import nl.b3p.topnl.top50nl.ReliefType;
import nl.b3p.topnl.top50nl.SpoorbaandeelType;
import nl.b3p.topnl.top50nl.TerreinType;
import nl.b3p.topnl.top50nl.Top50NlObjectType;
import nl.b3p.topnl.top50nl.Top50NlObjectType.Identificatie;
import nl.b3p.topnl.top50nl.TypeGebouwT50Type;
import nl.b3p.topnl.top50nl.WaterdeelType;
import nl.b3p.topnl.top50nl.WegdeelType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author Meine Toonen
 */
public class Top50NLConverter extends Converter {

    protected final static Log log = LogFactory.getLog(Top50NLConverter.class);
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
    public List<TopNLEntity> convertFeatureCollection(List jaxbFeatureCollection) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        List<TopNLEntity> entities = new ArrayList<>();

        for (Object fm : jaxbFeatureCollection) {
            FeatureMemberType featureMember = (FeatureMemberType)fm;
            JAXBElement jaxbObject = featureMember.getTop50NlObject();
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
            JAXBElement jaxbObject = fm.getTop50NlObject();
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
       /* } else if (featureMember instanceof PlaatsType) {
            entity = convertPlaats(featureMember);*/
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
        entity.setTopnltype(TopNLType.TOP50NL.getType());
        return entity;
    }

    @Override
    public Hoogte convertHoogte(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        HoogteType h = (HoogteType) jaxbObject;

        Hoogte hoogte = new Hoogte();
        convertTop50NlObjectType(h, hoogte);

        hoogte.setTypeHoogte(h.getTypeHoogte().value());

        hoogte.setHoogte(h.getHoogte());
        hoogte.setObjectEindTijd(h.getObjectEindTijd() != null ? h.getObjectEindTijd().getTime() : null);

        Element el = h.getGeometrie();
        Geometry geom = gc.convertGeometry(el);
        hoogte.setGeometrie(geom);

        return hoogte;
    }

    public String convertIdentificatie(Identificatie identificatie) {
        String idString = identificatie.getNEN3610ID().getNamespace().trim() + "." + identificatie.getNEN3610ID().getLokaalID();
        return idString;
    }

    private void convertTop50NlObjectType(Top50NlObjectType type, TopNLEntity entity) {
        entity.setIdentificatie(convertIdentificatie(type.getIdentificatie()));
        entity.setBrontype(type.getBrontype().value());
        entity.setBronactualiteit(type.getBronactualiteit().getTime());
        entity.setBronbeschrijving(type.getBronbeschrijving());
        entity.setObjectBeginTijd(type.getObjectBeginTijd().getTime());
        entity.setVisualisatieCode(type.getVisualisatieCode() != null ? type.getVisualisatieCode().longValue() : null);
    }

    @Override
    public FunctioneelGebied convertFunctioneelGebied(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        FunctioneelGebiedType f = (FunctioneelGebiedType) jaxbObject;

        FunctioneelGebied fg = new FunctioneelGebied();
        convertTop50NlObjectType(f, fg);

        fg.setTypeFunctioneelGebied(f.getTypeFunctioneelGebied().value());
        fg.setNaamFries(String.join(",", f.getNaamFries()));
        fg.setNaamNL(String.join(",", f.getNaamNL()));
        fg.setGeometrie(gc.convertGeometry(f.getGeometrie()));
        return fg;
    }

    @Override
    public Gebouw convertGebouw(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        GebouwType g = (GebouwType) jaxbObject;

        Gebouw gb = new Gebouw();
        convertTop50NlObjectType(g, gb);
        String types = "";
        for (TypeGebouwT50Type typeGebouwT50Type : g.getTypeGebouw()) {
            if (types.length() != 0) {
                types += ",";
            }
            types += typeGebouwT50Type.value();
        }
        gb.setTypeGebouw(types);
        gb.setFysiekVoorkomen(g.getFysiekVoorkomen() != null ? g.getFysiekVoorkomen().value() : null);
        gb.setHoogteklasse(g.getHoogteklasse() != null ? g.getHoogteklasse().value() : null);
        gb.setHoogte(g.getHoogte());
        gb.setNaam(String.join(",", g.getNaamNL()));
        gb.setNaamFries(String.join(",",g.getNaamFries()));

        gb.setGeometrie(gc.convertGeometry(g.getGeometrie()));
        return gb;
    }

    @Override
    public GeografischGebied convertGeografischGebied(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        GeografischGebiedType g = (GeografischGebiedType) jaxbObject;

        GeografischGebied gb = new GeografischGebied();
        convertTop50NlObjectType(g, gb);

        gb.setGeometrie(gc.convertGeometry(g.getGeometrie()));

        gb.setNaamFries(String.join(",", g.getNaamFries()));
        gb.setNaamNL(String.join(",", g.getNaamNL()));
        gb.setTypeGeografischGebied(g.getTypeGeografischGebied().value());
        return gb;
    }

    @Override
    public Inrichtingselement convertInrichtingselement(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        InrichtingselementType i = (InrichtingselementType) jaxbObject;

        Inrichtingselement ie = new Inrichtingselement();
        convertTop50NlObjectType(i, ie);

        ie.setGeometrie(gc.convertGeometry(i.getGeometrie()));

        ie.setTypeInrichtingselement(i.getTypeInrichtingsElement() != null ? i.getTypeInrichtingsElement().value() : null);
        ie.setStatus(i.getStatus().value());
        ie.setHoogteniveau(i.getHoogteNiveau().longValue());

        return ie;
    }

    @Override
    public Plaats convertPlaats(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        throw new IllegalArgumentException("Plaats does not exist in Top100NL");
    }

    @Override
    public RegistratiefGebied convertRegistratiefGebied(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        RegistratiefGebiedType r = (RegistratiefGebiedType) jaxbObject;

        RegistratiefGebied rg = new RegistratiefGebied();
        convertTop50NlObjectType(r, rg);

        rg.setGeometrie(gc.convertGeometry(r.getGeometrie()));

        rg.setNaamFries(String.join(",", r.getNaamFries()));
        rg.setNaamNL(String.join(",", r.getNaamNL()));
        rg.setNummer(String.join(",", r.getNummer()));
        rg.setTypeRegistratiefGebied(r.getTypeRegistratiefGebied() != null ? r.getTypeRegistratiefGebied().value() : null);
        return rg;
    }

    @Override
    public Relief convertRelief(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        ReliefType r = (ReliefType) jaxbObject;

        Relief rg = new Relief();
        convertTop50NlObjectType(r, rg);

        rg.setGeometrie((LineString) gc.convertGeometry(r.getGeometrie()));

        rg.setHoogteklasse(r.getHoogteklasse());
        rg.setTypeRelief(r.getTypeRelief() != null ? r.getTypeRelief().value() : null);
        rg.setHoogteniveau(r.getHoogteniveau().longValue());

        return rg;
    }

    @Override
    public Spoorbaandeel convertSpoorbaandeel(Object jaxbObject) throws IOException, SAXException,
            ParserConfigurationException, TransformerException, ClassCastException {

        SpoorbaandeelType r = (SpoorbaandeelType) jaxbObject;

        Spoorbaandeel rg = new Spoorbaandeel();
        convertTop50NlObjectType(r, rg);

        rg.setGeometrie((LineString) gc.convertGeometry(r.getGeometrie()));

        String fysieks = "";
        for (FysiekVoorkomenSpoorT50Type fk : r.getFysiekVoorkomen()) {
            if(fysieks.length() != 0){
                fysieks += ",";
            }
            fysieks += fk.value();
        }
        rg.setFysiekVoorkomen(fysieks);
        
        rg.setTypeSpoorbaan(r.getTypeSpoorbaan() != null ? r.getTypeSpoorbaan().value() : null);
        rg.setAantalSporen(r.getAantalSporen().toString());
        rg.setStatus(r.getStatus().value());
        rg.setHoogteniveau(r.getHoogteniveau().longValue());

        return rg;
    }

    @Override
    public Terrein convertTerrein(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        TerreinType r = (TerreinType) jaxbObject;

        Terrein rg = new Terrein();
        convertTop50NlObjectType(r, rg);

        rg.setGeometrie((Polygon) gc.convertGeometry(r.getGeometrie()));

        rg.setTypeLandgebruik(r.getTypeLandgebruik() != null ? r.getTypeLandgebruik().value() : null);

        return rg;
    }

    @Override
    public Waterdeel convertWaterdeel(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        WaterdeelType r = (WaterdeelType) jaxbObject;

        Waterdeel rg = new Waterdeel();
        convertTop50NlObjectType(r, rg);

        rg.setGeometrie(gc.convertGeometry(r.getGeometrie()));

        
        String fysieks = "";
        for (FysiekVoorkomenWaterT50Type fk : r.getFysiekVoorkomen()) {
            if(fysieks.length() != 0){
                fysieks += ",";
            }
            fysieks += fk.value();
        }
        rg.setFysiekVoorkomen(fysieks);
        rg.setTypeWater(r.getTypeWater() != null ? r.getTypeWater().value() : null);
        rg.setBreedteklasse( r.getBreedteklasse());
        rg.setHoogteniveau(r.getHoogteniveau().longValue());
        return rg;
    }

    @Override
    public Wegdeel convertWegdeel(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        WegdeelType r = (WegdeelType) jaxbObject;

        Wegdeel rg = new Wegdeel();
        convertTop50NlObjectType(r, rg);

        String fysieks = "";
        for (FysiekVoorkomenWegT50Type fk : r.getFysiekVoorkomen()) {
            if(fysieks.length() != 0){
                fysieks += ",";
            }
            fysieks += fk.value();
        }
        rg.setFysiekVoorkomen(fysieks);

        rg.setNaam(String.join(",",r.getStraatnaamNL()));
        rg.setGeometrie(gc.convertGeometry(r.getGeometrie()));
        rg.setTypeInfrastructuur(r.getTypeInfrastructuur() != null ? r.getTypeInfrastructuur().value() : null);
        rg.setTypeWeg(r.getTypeWeg() != null ? r.getTypeWeg().value() : null);
        rg.setVerhardingstype(r.getTypeVerharding().value());
        rg.setHoofdverkeersgebruik(r.getHoofdverkeersgebruik() != null ? r.getHoofdverkeersgebruik().value() : null);
        rg.setVerhardingsbreedteklasse(r.getVerhardingsbreedteklasse() );
        rg.setGescheidenRijbaan(r.getGescheidenRijbaan() == JaNeeType.JA);
        rg.setaWegnummer(String.join(",", r.getAWegnummer()));
        rg.setnWegnummer(String.join(",", r.getNWegnummer()));
        rg.seteWegnummer(String.join(",", r.getEWegnummer()));
        rg.setsWegnummer(String.join(",", r.getSWegnummer()));
        rg.setAfritnummer(r.getAfritnummer());
        rg.setAfritnaam(String.join(",",r.getAfritnaam()));
        rg.setKnooppuntnaam(String.join(",",r.getKnooppuntnaam()));
        rg.setBrugnaam(String.join(",",r.getBrugnaam()));
        rg.setTunnelnaam(String.join(",",r.getTunnelnaam()));
        rg.setHoogteniveau(r.getHoogteniveau().longValue());
        return rg;
    }

    @Override
    public PlanTopografie convertPlanTopografie(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
