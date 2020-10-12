/*
 * Copyright (C) 2019 B3Partners B.V.
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
package nl.b3p.brmo.test.util.database;

import nl.b3p.loader.jdbc.GeometryJdbcConverter;
import nl.b3p.loader.jdbc.GeometryJdbcConverterFactory;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.ColumnListHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public final class ViewUtils {
    private static final Log LOG = LogFactory.getLog(ViewUtils.class);

    /**
     * maak een lijst van de materialized views.
     *
     * @param ds (rsgb) database koppeling
     * @return lijst met namen
     * @throws SQLException als de query op de databron mislukt
     */
    public static List<String> listAllMaterializedViews(final BasicDataSource ds) throws SQLException {
        List<String> mviews;
        try (Connection conn = ds.getConnection()) {
            final GeometryJdbcConverter geomToJdbc = GeometryJdbcConverterFactory.getGeometryJdbcConverter(conn);
            mviews = new QueryRunner(geomToJdbc.isPmdKnownBroken()).query(conn, geomToJdbc.getMViewsSQL(), new ColumnListHandler<>());
            LOG.debug("Gevonden materialized views: " + mviews);
        }
        return mviews;
    }

    /**
     * refresh alle materialized views in de database.
     * <i>NB</i> niet duidelijk of de database de views in de juiste volgorde teruggeeft...
     *
     * @param ds (rsgb) database koppeling
     * @throws SQLException bij het benaderen van de database (nb als de refresh mislukt om wat voor reden wordt dat alleen gelogd)
     */
    public static void refreshAllMaterializedViews(final BasicDataSource ds) throws SQLException {
        List<String> mviews = listAllMaterializedViews(ds);
        if (mviews != null && mviews.size() > 1) {
            refreshMViews(mviews.toArray(new String[mviews.size()]), ds);
        }
    }

    /**
     * refresh de gegeven lijst van materialized views in de database.
     *
     * @param mviews lijst van namen van views
     * @param ds     database koppeling
     * @throws SQLException bij het benaderen van de database (nb als de refresh mislukt om wat voor reden wordt dat alleen gelogd)
     */
    public static void refreshMViews(final String[] mviews, final BasicDataSource ds) throws SQLException {
        try (Connection conn = ds.getConnection()) {
            conn.setAutoCommit(true);
            final GeometryJdbcConverter geomToJdbc = GeometryJdbcConverterFactory.getGeometryJdbcConverter(conn);
            for (String mview : mviews) {
                try {
                    LOG.debug("Start verversen van materialized view: " + mview);
                    // "update" gebruiken omdat we bij oracle stored procedure benaderen
                    Object o = new QueryRunner(geomToJdbc.isPmdKnownBroken()).update(conn, geomToJdbc.getMViewRefreshSQL(mview));
                    LOG.trace("Klaar met verversen van materialized view: " + mview);
                } catch (SQLException sqle) {
                    LOG.error("Bijwerken van materialized view `" + mview + "` is mislukt. ", sqle);
                }
            }
        }
    }

    private ViewUtils() {
    }
}
