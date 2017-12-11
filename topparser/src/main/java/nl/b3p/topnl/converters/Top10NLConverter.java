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
import com.vividsolutions.jts.geom.Point;
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
import nl.b3p.topnl.top10nl.BRTHogeEnLageZijdeType;
import nl.b3p.topnl.top10nl.BRTJaNeeWaardeType;
import nl.b3p.topnl.top10nl.CodeType;
import nl.b3p.topnl.top10nl.FeatureMemberType;
import nl.b3p.topnl.top10nl.FunctioneelGebiedType;
import nl.b3p.topnl.top10nl.GebouwType;
import nl.b3p.topnl.top10nl.HoogteType;
import nl.b3p.topnl.top10nl.RegistratiefGebiedType;
import nl.b3p.topnl.top10nl.SpoorbaandeelType;
import nl.b3p.topnl.top10nl.TerreinType;
import nl.b3p.topnl.top10nl.GeografischGebiedType;
import nl.b3p.topnl.top10nl.InrichtingselementType;
import nl.b3p.topnl.top10nl.PlaatsType;
import nl.b3p.topnl.top10nl.PlanTopografieType;
import nl.b3p.topnl.top10nl.ReliefType;
import nl.b3p.topnl.top10nl.Top10NlObjectType;
import nl.b3p.topnl.top10nl.Top10NlObjectType.Identificatie;
import nl.b3p.topnl.top10nl.WaterdeelType;
import nl.b3p.topnl.top10nl.WegdeelType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author Meine Toonen
 */
public class Top10NLConverter extends Converter {

    protected final static Log log = LogFactory.getLog(Top10NLConverter.class);
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
            JAXBElement jaxbObject = featureMember.getTop10NlObject();
            Object obj = jaxbObject.getValue();
            TopNLEntity entity = convertObject(obj);
            entities.add(entity);
        }

        return entities;
    }

    @Override
    public TopNLEntity convertObject(Object featureMember) throws IOException, SAXException, ParserConfigurationException, TransformerException,IllegalArgumentException {
        TopNLEntity entity = null;

         if(featureMember instanceof FeatureMemberType){
            FeatureMemberType fm = (FeatureMemberType)featureMember;
            JAXBElement jaxbObject = fm.getTop10NlObject();
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
        }else if (featureMember instanceof PlanTopografieType) {
            entity = convertPlanTopografie(featureMember);
        } else {
            throw new IllegalArgumentException("Type not recognized: " + featureMember.getClass());
        }

        log.debug("Converted type " + entity.getClass() + ", identificatie: " + entity.getIdentificatie());
        entity.setTopnltype(TopNLType.TOP10NL.getType());
        return entity;
    }

    @Override
    public Hoogte convertHoogte(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        HoogteType h = (HoogteType) jaxbObject;

        Hoogte hoogte = new Hoogte();
        convertTop10NlObjectType(h, hoogte);

        hoogte.setTypeHoogte(h.getTypeHoogte().getValue());

        hoogte.setHoogte(h.getHoogte());
        hoogte.setObjectEindTijd(h.getObjectEindTijd() != null ? h.getObjectEindTijd().getTime() : null);

        Element el = h.getGeometrie();
        Geometry geom = gc.convertGeometry(el);
        hoogte.setGeometrie(geom);
        hoogte.setReferentieVlak(h.getReferentievlak() != null ? h.getReferentievlak().getValue() : null );
        return hoogte;
    }

    public String convertIdentificatie(Identificatie identificatie) {
        String idString = identificatie.getNEN3610ID().getNamespace().trim() + "." + identificatie.getNEN3610ID().getLokaalID();
        return idString;
    }

    private void convertTop10NlObjectType(Top10NlObjectType type, TopNLEntity entity) {
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
        convertTop10NlObjectType(f, fg);

        fg.setTypeFunctioneelGebied(f.getTypeFunctioneelGebied().getValue());
        fg.setNaamFries(String.join(",", f.getNaamFries()));
        fg.setNaamNL(String.join(",", f.getNaamNL()));
        fg.setGeometrie(gc.convertGeometry(f.getGeometrie()));
        return fg;
    }

    @Override
    public Gebouw convertGebouw(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        GebouwType g = (GebouwType) jaxbObject;

        Gebouw gb = new Gebouw();
        convertTop10NlObjectType(g, gb);
        String types = "";
        for (CodeType typeGebouwT10Type : g.getTypeGebouw()) {
            if (types.length() != 0) {
                types += ",";
            }
            types += typeGebouwT10Type.getValue();
        }
        gb.setTypeGebouw(types);
        gb.setFysiekVoorkomen(g.getFysiekVoorkomen() != null ? g.getFysiekVoorkomen().getValue() : null);
        gb.setHoogteklasse(g.getHoogteklasse() != null ? g.getHoogteklasse().getValue() : null);
        gb.setHoogte(g.getHoogte());
        gb.setNaam(String.join(",", g.getNaam()));
        gb.setSoortnaam(g.getSoortnaam());
        gb.setStatus(g.getStatus() != null ? g.getStatus().getValue() : null);
        gb.setGeometrie(gc.convertGeometry(g.getGeometrie()));
        return gb;
    }

    @Override
    public GeografischGebied convertGeografischGebied(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        GeografischGebiedType g = (GeografischGebiedType) jaxbObject;

        GeografischGebied gb = new GeografischGebied();
        convertTop10NlObjectType(g, gb);

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
        convertTop10NlObjectType(i, ie);

        ie.setGeometrie(gc.convertGeometry(i.getGeometrie()));

        ie.setTypeInrichtingselement(i.getTypeInrichtingselement() != null ? i.getTypeInrichtingselement().getValue() : null);
        ie.setHoogteniveau(i.getHoogteniveau().longValue());

        return ie;
    }

    @Override
    public Plaats convertPlaats(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        PlaatsType p = (PlaatsType) jaxbObject;

        Plaats pl = new Plaats();
        convertTop10NlObjectType(p, pl);

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
        convertTop10NlObjectType(r, rg);

        rg.setGeometrie(gc.convertGeometry(r.getGeometrie()));

        rg.setNaamFries(String.join(",", r.getNaamFries()));
        rg.setNaamNL(String.join(",", r.getNaamNL()));
        rg.setNaamOfficieel(String.join(",", r.getNaamOfficieel()));
        rg.setNummer(String.join(",", r.getNummer()));
        rg.setTypeRegistratiefGebied(r.getTypeRegistratiefGebied() != null ? r.getTypeRegistratiefGebied().getValue() : null);
        return rg;
    }

    @Override
    public Relief convertRelief(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        ReliefType r = (ReliefType) jaxbObject;

        Relief rg = new Relief();
        convertTop10NlObjectType(r, rg);
        
        if(r.getLijnGeometrie() != null){
            rg.setGeometrie((LineString) gc.convertGeometry(r.getLijnGeometrie()));
        }
        
        if(r.getTaludGeometrie() != null){
            BRTHogeEnLageZijdeType hl = r.getTaludGeometrie().getBRTHogeEnLageZijde();
            Element[] els = hl.getHogeZijde();
            for (Element el : els) {
                LineString g = (LineString) gc.convertGeometry(el);
                if(el.getLocalName().equals("lageZijde")){
                    rg.setTaludLageZijde(g);
                }else if(el.getLocalName().equals("hogeZijde")){
                    rg.setTaludHogeZijde(g);
                }
            }
        }

        rg.setHoogteklasse(r.getHoogteklasse().getValue());
        rg.setTypeRelief(r.getTypeRelief() != null ? r.getTypeRelief().getValue() : null);
        rg.setHoogteniveau(r.getHoogteniveau().longValue());
        
        return rg;
    }

    @Override
    public Spoorbaandeel convertSpoorbaandeel(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException, ClassCastException {
        SpoorbaandeelType r = (SpoorbaandeelType) jaxbObject;

        Spoorbaandeel rg = new Spoorbaandeel();
        convertTop10NlObjectType(r, rg);

        final Geometry g = gc.convertGeometry(r.getGeometrie());
        if (g instanceof LineString) {
            rg.setGeometrie((LineString) g);
        } else {
            rg.setPuntGeometrie((Point) g);
        }

        String fysieks = "";
        for (CodeType fk : r.getFysiekVoorkomen()) {
            if(fysieks.length() != 0){
                fysieks += ",";
            }
            fysieks += fk.getValue();
        }
        rg.setFysiekVoorkomen(fysieks);
        
        rg.setTypeSpoorbaan(r.getTypeSpoorbaan() != null ? r.getTypeSpoorbaan().getValue() : null);
        rg.setAantalSporen(r.getAantalSporen() != null ? r.getAantalSporen().getValue() : null);
        rg.setStatus(r.getStatus().getValue());
        rg.setHoogteniveau(r.getHoogteniveau().longValue());
        rg.setTypeInfrastructuur(r.getTypeInfrastructuur() != null ? r.getTypeInfrastructuur().getValue() : null);
        rg.setSpoorbreedte(r.getSpoorbreedte() != null ? r.getSpoorbreedte().getValue() : null);
        rg.setVervoerfunctie(r.getVervoerfunctie()!= null ? r.getVervoerfunctie().getValue() : null);
        rg.setElektrificatie(r.getElektrificatie() != null ? r.getElektrificatie().equals(BRTJaNeeWaardeType.JA) : null);
        return rg;
    }

    @Override
    public Terrein convertTerrein(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        TerreinType r = (TerreinType) jaxbObject;

        Terrein rg = new Terrein();
        convertTop10NlObjectType(r, rg);

        rg.setGeometrie((Polygon) gc.convertGeometry(r.getGeometrieVlak()));

        rg.setTypeLandgebruik(r.getTypeLandgebruik() != null ? r.getTypeLandgebruik().getValue() : null);

        return rg;
    }

    @Override
    public PlanTopografie convertPlanTopografie(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        PlanTopografieType r = (PlanTopografieType) jaxbObject;

        PlanTopografie pt = new PlanTopografie();
        convertTop10NlObjectType(r, pt);

        pt.setGeometrie( gc.convertGeometry(r.getGeometrie()));
        pt.setNaam(r.getNaam());
        pt.setTypePlanTopografie(r.getTypeObject().getValue());
        
        return pt;
    }

    @Override
    public Waterdeel convertWaterdeel(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        WaterdeelType r = (WaterdeelType) jaxbObject;

        Waterdeel rg = new Waterdeel();
        convertTop10NlObjectType(r, rg);

        rg.setGeometrie(gc.convertGeometry(r.getGeometrie()));

        
        String fysieks = "";
        for (CodeType fk : r.getFysiekVoorkomen()) {
            if(fysieks.length() != 0){
                fysieks += ",";
            }
            fysieks += fk.getValue();
        }
        rg.setFysiekVoorkomen(fysieks);
        rg.setTypeWater(r.getTypeWater() != null ? r.getTypeWater().getValue() : null);
        rg.setBreedteklasse( r.getBreedteklasse() != null ? r.getBreedteklasse().getValue() : null);
        rg.setHoogteniveau(r.getHoogteniveau().longValue());
        rg.setGetijdeinvloed(r.getGetijdeinvloed() != null ? r.getGetijdeinvloed().equals(BRTJaNeeWaardeType.JA):false);
        rg.setHoofdAfwatering(r.getHoofdafwatering()!= null ? r.getHoofdafwatering().equals(BRTJaNeeWaardeType.JA):false);
        rg.setFunctie(r.getFunctie() != null ? r.getFunctie().getValue() : null);
        return rg;
    }

    @Override
    public Wegdeel convertWegdeel(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        WegdeelType r = (WegdeelType) jaxbObject;

        Wegdeel rg = new Wegdeel();
        convertTop10NlObjectType(r, rg);

        String fysieks = "";
        for (CodeType fk : r.getFysiekVoorkomen()) {
            if(fysieks.length() != 0){
                fysieks += ",";
            }
            fysieks += fk.getValue();
        }
        rg.setFysiekVoorkomen(fysieks);
        
        String typeweg = "";
        for (CodeType codeType : r.getTypeWeg()) {
            if (typeweg.length() != 0) {
                typeweg += ",";
            }
            typeweg += codeType.getValue();
        }

        rg.setTypeWeg(typeweg);
        
        
        String hoofdverkeersgebruikt = "";
        for (CodeType codeType : r.getHoofdverkeersgebruik()) {
            if (hoofdverkeersgebruikt.length() != 0) {
                hoofdverkeersgebruikt += ",";
            }
            hoofdverkeersgebruikt += codeType.getValue();
        }

        rg.setHoofdverkeersgebruik(hoofdverkeersgebruikt);
        
        rg.setNaam(String.join(",",r.getNaam()));
        
        Element [] els = r.getHartGeometrie();
        for (Element el : els) {
            String localname = el.getLocalName();
            Geometry g = gc.convertGeometry(el);
            if(localname.equals("hoofdGeometrie")){
                rg.setGeometrie(g);
            }else if(localname.equals("hartGeometrie")){
                rg.setHartGeometrie(g);
            }
        }
        rg.setTypeInfrastructuur(r.getTypeInfrastructuur() != null ? r.getTypeInfrastructuur().getValue() : null);
        rg.setVerhardingstype(r.getVerhardingstype().getValue());
        rg.setVerhardingsbreedteklasse(r.getVerhardingsbreedteklasse() != null ? r.getVerhardingsbreedteklasse().getValue() : null);
        rg.setGescheidenRijbaan(r.getGescheidenRijbaan() ==BRTJaNeeWaardeType.JA);
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
        rg.setStatus(r.getStatus().getValue());
        return rg;
    }
}
