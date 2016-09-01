/*
 * Copyright (C) 2016 B3Partners B.V.
 */
package nl.b3p.brmo.bgt.util;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;
import java.io.IOException;
import java.net.URL;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.SchemaException;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.json.simple.parser.JSONParser;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * utilities mbt PDOK BGT GML light formaat en de dwnload website.
 *
 * @author mprins
 */
public class PDOKBGTLightUtil {

    private static final Log LOG = LogFactory.getLog(PDOKBGTLightUtil.class);
    /**
     * het featuretype van de {@code tileinfo.json} van de pdok bgt download
     * site.
     */
    public static final String TILEINFO_F_TYPE = "geom:Polygon:srid=28992,extractset:String,id:Integer";

    /**
     * Bepaald de lijst met grid ids.
     *
     * @param wktGeom aandachtsgebied
     * @param jsonUrl url naar de pdok bgt gml light index
     * @return lijst met grid ids
     *
     * @see #calculateGridIds(com.vividsolutions.jts.geom.Geometry,
     * org.geotools.data.simple.SimpleFeatureCollection)
     */
    public static Set<Integer> calculateGridIds(String wktGeom, String jsonUrl) {
        Geometry geometry = null;
        SimpleFeatureCollection fc = null;
        try {
            com.vividsolutions.jts.geom.GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
            WKTReader reader = new WKTReader(geometryFactory);
            geometry = reader.read(wktGeom);
            geometry.setSRID(28992);

            URL u = new URL(jsonUrl);
            JSONParser parser = new JSONParser();
            Object json = parser.parse(new java.io.InputStreamReader(u.openStream()));
            final FeatureJSON io = new FeatureJSON();
            try {
                final SimpleFeatureType jsonType = DataUtilities.createType("tileinfo", TILEINFO_F_TYPE);
                io.setFeatureType(jsonType);
            } catch (SchemaException se) {
                LOG.warn("Aanmaken van tileinfo featuretype is mislukt", se);
            }
            fc = (SimpleFeatureCollection) io.readFeatureCollection(json.toString());
        } catch (com.vividsolutions.jts.io.ParseException ex) {
            LOG.error("Parsen van de WKT selectie geometrie is mislukt", ex);
        } catch (IOException | org.json.simple.parser.ParseException io) {
            LOG.error("Ophalen of parsen van de geojson file met tiles is mislukt", io);
        }
        return calculateGridIds(geometry, fc);
    }

    public static Set<Integer> calculateGridIds(Geometry aoi, SimpleFeatureCollection grid) {
        final Set<Integer> ids = new TreeSet();
        SimpleFeature sf;
        aoi = aoi.buffer(10, 1);
        try (SimpleFeatureIterator sfi = grid.features()) {
            while (sfi.hasNext()) {
                sf = sfi.next();
                if (aoi.intersects((Geometry) sf.getDefaultGeometry())) {
                    LOG.debug(sf.getAttribute("id") + " Toevoegen aan de lijst van op te halen ids");
                    ids.add((Integer) (sf.getAttribute("id")));
                }
            }
        }
        LOG.debug("De lijst bevat de volgende id's: " + ids);
        return ids;
    }

    private PDOKBGTLightUtil() {
    }
}
