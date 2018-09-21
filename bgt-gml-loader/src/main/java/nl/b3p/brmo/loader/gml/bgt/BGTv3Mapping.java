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
package nl.b3p.brmo.loader.gml.bgt;

import java.util.HashMap;
import java.util.Map;
import static nl.b3p.brmo.loader.gml.GMLLightFeatureTransformer.BEGINTIJD_NAME;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author matthijsln
 */
public class BGTv3Mapping {
    private final String objectType, table;

    public String getObjectType() {
        return objectType;
    }

    public String getTable() {
        return table;
    }

    public Map<String, Attribute> getAttributes() {
        return attributes;
    }

    public class Attribute {
        String sqlName, xmlName, xmlName3, sqlType;

        public Attribute() {
        }

        public Attribute(String sqlName, String xmlName) {
            this.sqlName = sqlName;
            this.xmlName= xmlName;
        }

        public String getXml3Name() {
            return xmlName3 != null ? xmlName3 : xmlName;
        }

        public String getSqlName() {
            return sqlName;
        }

        public String getSqlType() {
            return sqlType;
        }

        @Override
        public String toString() {
            return getXml3Name() + " (" + sqlType + ")";
        }
    }
    private final Map<String, Attribute> attributes = new HashMap();

    public BGTv3Mapping(String objectType, String table, NodeList datamodelAttributesList) {
        this.objectType = objectType;
        this.table = table;

        addDefaultMappings();

        for(int i = 0; i < datamodelAttributesList.getLength(); i++) {
            Node n = datamodelAttributesList.item(i);
            Attribute a = new Attribute();
            a.sqlName = n.getAttributes().getNamedItem("sqlname").getNodeValue();
            Node xmlnamev3 = n.getAttributes().getNamedItem("xmlnamev3");
            a.xmlName3 = xmlnamev3 != null ? xmlnamev3.getNodeValue() : null;
            a.xmlName = n.getAttributes().getNamedItem("xslname").getNodeValue();
            a.sqlType = n.getAttributes().getNamedItem("sqltype").getNodeValue();
            attributes.put(a.sqlName, a);
        }
    }

    private void addDefaultMappings() {
        Attribute a;
        a = new Attribute(BEGINTIJD_NAME, BGTv3Object.ATTRIBUTE_CREATION_DATE); attributes.put(a.sqlName, a);
        a = new Attribute("tijdstip_registratie", BGTv3Object.ATTRIBUTE_TIJDSTIP_REGISTRATIE); attributes.put(a.sqlName, a);
        a = new Attribute("bgt_status", "bgt-status"); attributes.put(a.sqlName, a);
        a = new Attribute("plus_status", "plus-status"); attributes.put(a.sqlName, a);
        a = new Attribute("relve_hoogteligging", "relatieveHoogteligging"); attributes.put(a.sqlName, a);
    }

    @Override
    public String toString() {
        return "BGTv3Mapping[" + objectType + " table " + table + ": " + attributes.toString() + "]";
    }
}
