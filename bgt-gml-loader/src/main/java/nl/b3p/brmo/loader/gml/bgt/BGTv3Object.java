package nl.b3p.brmo.loader.gml.bgt;


import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/*
 * Copyright (C) 2018 B3Partners B.V.
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

/**
 *
 * @author matthijsln
 */
public class BGTv3Object {

    public enum Type {
        TOEVOEGING,
        WIJZIGING,
        VERWIJDERING
    }

    private Type type = Type.TOEVOEGING;

    private String elementName;
    private String objectType;
    private String objectId;

    private Map<String,String> attributes = new HashMap();

    private Map<String,Object> geometries = new HashMap();

    public BGTv3Object() {
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getElementName() {
        return elementName;
    }

    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public Map<String, Object> getGeometries() {
        return geometries;
    }

    public void setGeometries(Map<String, Object> geometries) {
        this.geometries = geometries;
    }

    @Override
    public String toString() {
        char c;
        switch(type) {
            default:
            case TOEVOEGING: c = '+'; break;
            case WIJZIGING: c = '='; break;
            case VERWIJDERING: c = '-'; break;
        }
        return c + " " + objectType + ":" + objectId + ", " + attributes + ", " + geometries;
    }

    public void readFromXMLStream(XMLStreamReader streamReader) throws XMLStreamException {
        if(!streamReader.isStartElement() && !streamReader.getLocalName().equals("cityObjectMember")) {
            throw new IllegalArgumentException("Cannot read cityObjectMember: not at start element");
        }
        streamReader.nextTag();
        elementName = streamReader.getLocalName();
        boolean nested = false;
        while(true) {
            streamReader.nextTag();
            if(streamReader.isEndElement() && streamReader.getLocalName().equals(elementName)) {
                break;
            }
            String name = streamReader.getLocalName();

            streamReader.next();
            while(streamReader.isWhiteSpace()) {
                streamReader.next();
            }
            if(streamReader.isCharacters()) {
                attributes.put(name, streamReader.getText());
                streamReader.nextTag();
            } else if(streamReader.isEndElement()) {
                attributes.put(name, null);
            } else if(streamReader.isStartElement()) {
                if(streamReader.getNamespaceURI().equals("http://www.opengis.net/gml")) {
                    // TODO parse gml
                    geometries.put(name, streamReader.getLocalName());
                    while(!(streamReader.isEndElement() && streamReader.getLocalName().equals(name))) {
                        streamReader.next();
                    }
                }
            }
        }
    }
}
