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

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import nl.b3p.topnl.entities.Hoogte;
import nl.b3p.topnl.entities.TopNLEntity;
import nl.b3p.topnl.top250nl.FeatureCollectionT250NLType;
import nl.b3p.topnl.top250nl.FeatureCollectionT250NLType.FeatureMember;
import nl.b3p.topnl.top250nl.HoogteType;

/**
 *
 * @author Meine Toonen
 */
public class Top250NLConverter extends Converter{

    @Override
    public List<TopNLEntity> convert(Object jaxbObject) {
        List<TopNLEntity> entities = null;
        if(jaxbObject instanceof FeatureCollectionT250NLType){
            entities = convertFeatureCollection(jaxbObject);
        }else{
            entities = new ArrayList<>();
            entities.add(convertObject(jaxbObject));
        }
        return entities;
    }
    
    @Override
    public List<TopNLEntity> convertFeatureCollection(Object jaxbFeatureCollection) {
        FeatureCollectionT250NLType collection = (FeatureCollectionT250NLType)jaxbFeatureCollection;
        List<TopNLEntity> entities = new ArrayList<>();
        
        List<FeatureMember> featureMembers = collection.getFeatureMember();
        for (FeatureMember featureMember : featureMembers) {
            JAXBElement jaxbObject= featureMember.getTop250NlObject();
            Object obj = jaxbObject.getValue();
            TopNLEntity entity = convertObject(obj);
            entities.add(entity);
        }
        
        return entities;
    }
    
    @Override
    public TopNLEntity convertObject(Object featureMember){
        TopNLEntity entity = null;

        
       // if(featureMember.getClass() instanceof HoogteType){
       if(featureMember instanceof HoogteType){
            entity = convertHoogte(featureMember);
        }else{
            throw new IllegalArgumentException("Type not recognized: " + featureMember.getClass());
        }
        
        return entity;
    }

    @Override
    public Hoogte convertHoogte(Object jaxbObject) {
        Hoogte hoogte = new Hoogte();
        
        return hoogte;
    }

    
}
