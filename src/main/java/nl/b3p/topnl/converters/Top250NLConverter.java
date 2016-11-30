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
import nl.b3p.topnl.entities.Hoogte;
import nl.b3p.topnl.entities.TopNLEntity;
import nl.b3p.topnl.top250nl.FeatureCollectionT250NLType;
import nl.b3p.topnl.top250nl.FeatureCollectionT250NLType.FeatureMember;
import nl.b3p.topnl.top250nl.HoogteType;
import nl.b3p.topnl.top250nl.Top250NlObjectType.Identificatie;
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
        } else {
            throw new IllegalArgumentException("Type not recognized: " + featureMember.getClass());
        }

        return entity;
    }

    @Override
    public Hoogte convertHoogte(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException {
        HoogteType h = (HoogteType) jaxbObject;

        Hoogte hoogte = new Hoogte();

        hoogte.setIdentificatie(convertIdentificatie(h.getIdentificatie()));
        hoogte.setBrontype(h.getBrontype().getValue());
        hoogte.setBronactualiteit(h.getBronactualiteit().getTime());
        hoogte.setBronbeschrijving(h.getBronbeschrijving());
        hoogte.setBronnauwkeurigheid(h.getBronnauwkeurigheid());
        hoogte.setObjectBeginTijd(h.getObjectBeginTijd().getTime());
        hoogte.setVisualisatieCode(h.getVisualisatieCode().longValue());
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
}
