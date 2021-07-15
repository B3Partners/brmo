package nl.b3p.brmo.util;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKTConstants;
import org.locationtech.jts.io.WKTWriter;

public class StandardLinearizedWKTWriter {

    private final WKTWriter writer = new WKTWriter();

    public String write(Geometry geometry) {
        String wkt = writer.write(geometry);

        // LINEARRING is non-standard WKT, but the JTS WKTWriter will write it anyway!
        if (wkt.startsWith(WKTConstants.LINEARRING)) {
            wkt = WKTConstants.LINESTRING + wkt.substring(WKTConstants.LINEARRING.length());
        }
        return wkt;
    }
}
