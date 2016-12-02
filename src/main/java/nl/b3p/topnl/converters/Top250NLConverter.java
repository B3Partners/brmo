/*
 * Copyright (C) 2016 B3Partners B.V.
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
package nl.b3p.topnl.converters;

import com.vividsolutions.jts.geom.Geometry;
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
import nl.b3p.topnl.entities.RegistratiefGebied;
import nl.b3p.topnl.entities.Relief;
import nl.b3p.topnl.entities.Spoorbaandeel;
import nl.b3p.topnl.entities.Terrein;
import nl.b3p.topnl.entities.TopNLEntity;
import nl.b3p.topnl.entities.Waterdeel;
import nl.b3p.topnl.entities.Wegdeel;
import nl.b3p.topnl.top250nl.FeatureCollectionT250NLType;
import nl.b3p.topnl.top250nl.FeatureCollectionT250NLType.FeatureMember;
import nl.b3p.topnl.top250nl.FunctioneelGebiedType;
import nl.b3p.topnl.top250nl.GebouwType;
import nl.b3p.topnl.top250nl.GeografischGebiedType;
import nl.b3p.topnl.top250nl.HoogteType;
import nl.b3p.topnl.top250nl.InrichtingselementType;
import nl.b3p.topnl.top250nl.PlaatsType;
import nl.b3p.topnl.top250nl.RegistratiefGebiedType;
import nl.b3p.topnl.top250nl.ReliefType;
import nl.b3p.topnl.top250nl.SpoorbaandeelType;
import nl.b3p.topnl.top250nl.TerreinType;
import nl.b3p.topnl.top250nl.Top250NlObjectType;
import nl.b3p.topnl.top250nl.Top250NlObjectType.Identificatie;
import nl.b3p.topnl.top250nl.WaterdeelType;
import nl.b3p.topnl.top250nl.WegdeelType;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 *
 * @author Meine Toonen
 */
public class Top250NLConverter extends Converter {

    @Override
    public List<TopNLEntity> convert(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        List<TopNLEntity> entities = null;
        if (jaxbObject instanceof FeatureCollectionT250NLType) {
            entities = convertFeatureCollection(jaxbObject);
        } else {
            entities = new ArrayList<>();
            entities.add(convertObject(jaxbObject));
        }
        return entities;
    }

    @Override
    public List<TopNLEntity> convertFeatureCollection(Object jaxbFeatureCollection) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        FeatureCollectionT250NLType collection = (FeatureCollectionT250NLType) jaxbFeatureCollection;
        List<TopNLEntity> entities = new ArrayList<>();

        List<FeatureMember> featureMembers = collection.getFeatureMember();
        for (FeatureMember featureMember : featureMembers) {
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

        if (featureMember instanceof HoogteType) {
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
        entity.setVisualisatieCode(type.getVisualisatieCode().longValue());
    }

    @Override
    public FunctioneelGebied convertFunctioneelGebied(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        FunctioneelGebiedType f = (FunctioneelGebiedType) jaxbObject;

        FunctioneelGebied fg = new FunctioneelGebied();
        convertTop250NlObjectType(f, fg);
        
        fg.setTypeFunctioneelGebied(f.getTypeFunctioneelGebied().getValue());
        fg.setNaamFries(String.join (",",f.getNaamFries()));
        fg.setNaamNL(String.join(",",f.getNaamNL()));
        fg.setSoortnaam(f.getSoortnaam());
        
        fg.setGeometrie(gc.convertGeometry(f.getGeometrie()));
        return fg;
    }

    @Override
    public Gebouw convertGebouw(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public GeografischGebied convertGeografischGebied(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Inrichtingselement convertInrichtingselement(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Plaats convertPlaats(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public RegistratiefGebied convertRegistratiefGebied(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Relief convertRelief(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Spoorbaandeel convertSpoorbaandeel(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Terrein convertTerrein(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Waterdeel convertWaterdeel(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Wegdeel convertWegdeel(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
