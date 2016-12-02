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

import java.io.IOException;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
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
import org.xml.sax.SAXException;

/**
 *
 * @author Meine Toonen
 */
public abstract class Converter {
    
    protected GeometryConverter gc = new GeometryConverter();
    
    public abstract List<TopNLEntity> convert(Object jaxbObject)  throws IOException, SAXException, ParserConfigurationException, TransformerException;
    
    public abstract TopNLEntity convertObject(Object jaxbObject)  throws IOException, SAXException, ParserConfigurationException, TransformerException;
    
    public abstract List<TopNLEntity> convertFeatureCollection(Object jaxbObject)  throws IOException, SAXException, ParserConfigurationException, TransformerException;
    
    public abstract Hoogte convertHoogte(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException;
    public abstract FunctioneelGebied convertFunctioneelGebied(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException;
    public abstract Gebouw convertGebouw(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException;
    public abstract GeografischGebied convertGeografischGebied(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException;
    public abstract Inrichtingselement convertInrichtingselement(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException;
    public abstract Plaats convertPlaats(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException;
    public abstract RegistratiefGebied convertRegistratiefGebied(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException;
    public abstract Relief convertRelief(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException;
    public abstract Spoorbaandeel convertSpoorbaandeel(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException;
    public abstract Terrein convertTerrein(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException;
    public abstract Waterdeel convertWaterdeel(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException;
    public abstract Wegdeel convertWegdeel(Object jaxbObject) throws IOException, SAXException, ParserConfigurationException, TransformerException;
    
}
