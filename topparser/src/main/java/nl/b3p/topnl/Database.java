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
package nl.b3p.topnl;

import com.vividsolutions.jts.io.ParseException;
import java.sql.SQLException;
import javax.sql.DataSource;
import nl.b3p.loader.jdbc.GeometryJdbcConverter;
import nl.b3p.loader.jdbc.GeometryJdbcConverterFactory;
import nl.b3p.topnl.converters.DbUtilsGeometryColumnConverter;
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
import org.apache.commons.dbutils.BasicRowProcessor;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @author Meine Toonen meinetoonen@b3partners.nl
 */
public class Database {

    protected final static Log log = LogFactory.getLog(Database.class);

    private final DataSource dataSource;
    private GeometryJdbcConverter gjc;
    private QueryRunner run;

    public Database(DataSource ds) throws SQLException {
        this.dataSource = ds;
        if (ds != null) {
            gjc = GeometryJdbcConverterFactory.getGeometryJdbcConverter(dataSource.getConnection());
            run = new QueryRunner(dataSource, gjc.isPmdKnownBroken());
        }
    }

    public void save(TopNLEntity entity) throws ParseException, SQLException {

        try {

            if (entity instanceof Hoogte) {
                saveHoogte(entity);
            } else if (entity instanceof FunctioneelGebied) {
                saveFunctioneelGebied(entity);
            } else if (entity instanceof Gebouw) {
                saveGebouw(entity);
            } else if (entity instanceof GeografischGebied) {
                saveGeografischGebied(entity);
            } else if (entity instanceof Inrichtingselement) {
                saveInrichtingselement(entity);
            } else if (entity instanceof Plaats) {
                savePlaats(entity);
            } else if (entity instanceof RegistratiefGebied) {
                saveRegistratiefGebied(entity);
            } else if (entity instanceof Relief) {
                saveRelief(entity);
            } else if (entity instanceof Spoorbaandeel) {
                saveSpoorbaandeel(entity);
            } else if (entity instanceof Terrein) {
                saveTerrein(entity);
            } else if (entity instanceof Waterdeel) {
                saveWaterdeel(entity);
            } else if (entity instanceof Wegdeel) {
                saveWegdeel(entity);
            } else if (entity instanceof PlanTopografie) {
                savePlanTopografie(entity);
            } else{
                throw new IllegalArgumentException("Type of entity not (yet) implemented.");
            }

        } catch (SQLException e) {
            log.error("Error inserting entity: ", e);
            throw e;
        }
    }

    private Gebouw saveGebouw(TopNLEntity entity) throws SQLException, ParseException {
        Gebouw h = (Gebouw) entity;

        ResultSetHandler<Gebouw> handler = new BeanHandler(Gebouw.class, new BasicRowProcessor(new DbUtilsGeometryColumnConverter(gjc)));
        Object nativeGeom = gjc.convertToNativeGeometryObject(h.getGeometrie());
        Object[] args = getVarargs(entity,
                h.getTypeGebouw(),
                h.getStatus(),
                h.getFysiekVoorkomen(),
                h.getHoogteklasse(),
                h.getHoogte(),
                h.getSoortnaam(),
                h.getNaam(),
                h.getNaamFries(),
                nativeGeom);
        Gebouw inserted = run.insert("INSERT INTO " + h.getTopnltype() + ".gebouw (" + getTopNLEntityColumns() + ",typegebouw,status,fysiekvoorkomen,hoogteklasse,hoogte,soortnaam,naam,naamfries,geometrie) VALUES (" + getTopNLEntityReplacementChars() + ",?,?,?,?,?,?,?,?,?)",
                handler,
                args);

        return inserted;
    }

    private PlanTopografie savePlanTopografie(TopNLEntity entity) throws SQLException, ParseException {
        PlanTopografie h = (PlanTopografie) entity;

        ResultSetHandler<PlanTopografie> handler = new BeanHandler(PlanTopografie.class, new BasicRowProcessor(new DbUtilsGeometryColumnConverter(gjc)));
        Object nativeGeom = gjc.convertToNativeGeometryObject(h.getGeometrie());
        Object[] args = getVarargs(entity,
                h.getTypePlanTopografie(),
                h.getNaam(),
                nativeGeom);
        PlanTopografie inserted = run.insert("INSERT INTO " + h.getTopnltype() + ".PlanTopoGrafie (" + getTopNLEntityColumns() + ",typePlanTopografie,naam,geometrie) VALUES (" + getTopNLEntityReplacementChars() + ",?,?,?)",
                handler,
                args);

        return inserted;
    }

    private GeografischGebied saveGeografischGebied(TopNLEntity entity) throws SQLException, ParseException {
        GeografischGebied h = (GeografischGebied) entity;

        ResultSetHandler<GeografischGebied> handler = new BeanHandler(GeografischGebied.class, new BasicRowProcessor(new DbUtilsGeometryColumnConverter(gjc)));
        Object nativeGeom = gjc.convertToNativeGeometryObject(h.getGeometrie());
        Object[] args = getVarargs(entity,
                h.getTypeGeografischGebied(),
                h.getNaamNL(),
                h.getNaamFries(),
                nativeGeom);
        GeografischGebied inserted = run.insert("INSERT INTO " + h.getTopnltype() + ".geografischgebied (" + getTopNLEntityColumns() + ",typeGeografischGebied,naamNL,naamFries,geometrie) VALUES (" + getTopNLEntityReplacementChars() + ",?,?,?,?)",
                handler,
                args);

        return inserted;
    }

    private Inrichtingselement saveInrichtingselement(TopNLEntity entity) throws SQLException, ParseException {
        Inrichtingselement h = (Inrichtingselement) entity;

        ResultSetHandler<Inrichtingselement> handler = new BeanHandler(Inrichtingselement.class, new BasicRowProcessor(new DbUtilsGeometryColumnConverter(gjc)));
        Object nativeGeom = gjc.convertToNativeGeometryObject(h.getGeometrie());
        Object[] args = getVarargs(entity,
                h.getTypeInrichtingselement(),
                h.getSoortnaam(),
                h.getStatus(),
                h.getHoogteniveau(),
                nativeGeom);
        Inrichtingselement inserted = run.insert("INSERT INTO " + h.getTopnltype() + ".inrichtingselement (" + getTopNLEntityColumns() + ",typeInrichtingselement,soortnaam,status,hoogteniveau,geometrie) VALUES (" + getTopNLEntityReplacementChars() + ",?,?,?,?,?)",
                handler,
                args);

        return inserted;
    }

    private Plaats savePlaats(TopNLEntity entity) throws SQLException, ParseException {
        Plaats h = (Plaats) entity;

        ResultSetHandler<Plaats> handler = new BeanHandler(Plaats.class, new BasicRowProcessor(new DbUtilsGeometryColumnConverter(gjc)));
        Object nativeGeom = gjc.convertToNativeGeometryObject(h.getGeometrie());
        Object[] args = getVarargs(entity,
                h.getTypeGebied(),
                h.getAantalInwoners(),
                h.getNaamOfficieel(),
                h.getNaamNL(),
                h.getNaamFries(),
                nativeGeom);
        Plaats inserted = run.insert("INSERT INTO " + h.getTopnltype() + ".plaats (" + getTopNLEntityColumns() + ",typeGebied,aantalInwoners,naamOfficieel,naamNL,naamFries,geometrie) VALUES (" + getTopNLEntityReplacementChars() + ",?,?,?,?,?,?)",
                handler,
                args);

        return inserted;
    }

    private RegistratiefGebied saveRegistratiefGebied(TopNLEntity entity) throws SQLException, ParseException {
        RegistratiefGebied h = (RegistratiefGebied) entity;

        ResultSetHandler<RegistratiefGebied> handler = new BeanHandler(RegistratiefGebied.class, new BasicRowProcessor(new DbUtilsGeometryColumnConverter(gjc)));
        Object nativeGeom = gjc.convertToNativeGeometryObject(h.getGeometrie());
        Object[] args = getVarargs(entity,
                h.getTypeRegistratiefGebied(),
                h.getNaamOfficieel(),
                h.getNaamNL(),
                h.getNaamFries(),
                h.getNummer(),
                nativeGeom);
        RegistratiefGebied inserted = run.insert("INSERT INTO " + h.getTopnltype() + ".registratiefgebied (" + getTopNLEntityColumns() + ",typeRegistratiefGebied,naamOfficieel,naamNL,naamFries,nummer,geometrie) VALUES (" + getTopNLEntityReplacementChars() + ",?,?,?,?,?,?)",
                handler,
                args);

        return inserted;
    }

    private Relief saveRelief(TopNLEntity entity) throws SQLException, ParseException {
        Relief h = (Relief) entity;

        ResultSetHandler<Relief> handler = new BeanHandler(Relief.class, new BasicRowProcessor(new DbUtilsGeometryColumnConverter(gjc)));
        Object nativeGeom = gjc.convertToNativeGeometryObject(h.getGeometrie());
        Object nativeLageZijde = gjc.convertToNativeGeometryObject(h.getTaludLageZijde());
        Object nativeHogeZijde = gjc.convertToNativeGeometryObject(h.getTaludHogeZijde());
        Object[] args = getVarargs(entity,
                h.getTypeRelief(),
                h.getHoogteklasse(),
                h.getHoogteniveau(),
                nativeGeom,
                nativeLageZijde,
                nativeHogeZijde);
        Relief inserted = run.insert("INSERT INTO " + h.getTopnltype() + ".relief (" + getTopNLEntityColumns() + ",typeRelief,hoogteklasse,hoogteniveau,geometrie,taludLageZijde,taludHogeZijde) VALUES (" + getTopNLEntityReplacementChars() + ",?,?,?,?,?,?)",
                handler,
                args);

        return inserted;
    }

    private Spoorbaandeel saveSpoorbaandeel(TopNLEntity entity) throws SQLException, ParseException {
        Spoorbaandeel h = (Spoorbaandeel) entity;

        ResultSetHandler<Spoorbaandeel> handler = new BeanHandler(Spoorbaandeel.class, new BasicRowProcessor(new DbUtilsGeometryColumnConverter(gjc)));
        Object nativeGeom = gjc.convertToNativeGeometryObject(h.getGeometrie());
        Object[] args = getVarargs(entity,
                h.getTypeInfrastructuur(),
                h.getTypeSpoorbaan(),
                h.getFysiekVoorkomen(),
                h.getSpoorbreedte(),
                h.getAantalSporen(),
                h.getVervoerfunctie(),
                h.getElektrificatie(),
                h.getStatus(),
                h.getBrugnaam(),
                h.getTunnelnaam(),
                h.getBaanvaknaam(),
                h.getHoogteniveau(),
                nativeGeom);
        Spoorbaandeel inserted = run.insert("INSERT INTO " + h.getTopnltype() + ".spoorbaandeel (" + getTopNLEntityColumns() + ",typeInfrastructuur,typeSpoorbaan,fysiekVoorkomen,spoorbreedte,aantalSporen,vervoerfunctie,elektrificatie,status,brugnaam,tunnelnaam,baanvaknaam,hoogteniveau,geometrie) VALUES (" + getTopNLEntityReplacementChars() + ",?,?,?,?,?,?,?,?,?,?,?,?,?)",
                handler,
                args);

        return inserted;
    }

    private Terrein saveTerrein(TopNLEntity entity) throws SQLException, ParseException {
        Terrein h = (Terrein) entity;

        ResultSetHandler<Terrein> handler = new BeanHandler(Terrein.class, new BasicRowProcessor(new DbUtilsGeometryColumnConverter(gjc)));
        Object nativeGeom = gjc.convertToNativeGeometryObject(h.getGeometrie());
        Object[] args = getVarargs(entity,
                h.getTypeLandgebruik(),
                h.getNaam(),
                nativeGeom);
        Terrein inserted = run.insert("INSERT INTO " + h.getTopnltype() + ".terrein (" + getTopNLEntityColumns() + ",typelandgebruik,naam,geometrie) VALUES (" + getTopNLEntityReplacementChars() + ",?,?,?)",
                handler,
                args);

        return inserted;
    }

    private Waterdeel saveWaterdeel(TopNLEntity entity) throws SQLException, ParseException {
        Waterdeel h = (Waterdeel) entity;

        ResultSetHandler<Waterdeel> handler = new BeanHandler(Waterdeel.class, new BasicRowProcessor(new DbUtilsGeometryColumnConverter(gjc)));
        Object nativeGeom = gjc.convertToNativeGeometryObject(h.getGeometrie());
        
        Object[] args = getVarargs(entity,
                h.getTypeWater(),
                h.getBreedteklasse(),
                h.getFysiekVoorkomen(),
                h.getVoorkomen(),
                h.getGetijdeinvloed(),
                h.getVaarwegklasse(),
                h.getNaamOfficieel(),
                h.getNaamNL(),
                h.getNaamFries(),
                h.getIsBAGnaam(),
                h.getSluisnaam(),
                h.getBrugnaam(),
                h.getHoogteniveau(),
                h.getFunctie(),
                h.isHoofdAfwatering(),
                nativeGeom);
        Waterdeel inserted = run.insert("INSERT INTO " + h.getTopnltype() + ".waterdeel (" + getTopNLEntityColumns() + ",typewater,breedteklasse,fysiekvoorkomen,voorkomen,getijdeinvloed,vaarwegklasse,naamofficieel,naamnl,naamfries,isbagnaam,sluisnaam,brugnaam,hoogteniveau,functie,hoofdAfwatering,geometrie) VALUES (" + getTopNLEntityReplacementChars() + ",?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                handler,
                args);

        return inserted;
    }

    private Wegdeel saveWegdeel(TopNLEntity entity) throws SQLException, ParseException {
        Wegdeel h = (Wegdeel) entity;

        ResultSetHandler<Wegdeel> handler = new BeanHandler(Wegdeel.class, new BasicRowProcessor(new DbUtilsGeometryColumnConverter(gjc)));
        Object nativeGeom = gjc.convertToNativeGeometryObject(h.getGeometrie());
        Object nativeHartGeom = gjc.convertToNativeGeometryObject(h.getHartGeometrie());
        Object[] args = getVarargs(entity,
                h.getTypeInfrastructuur(),
                h.getTypeWeg(),
                h.getHoofdverkeersgebruik(),
                h.getFysiekVoorkomen(),
                h.getVerhardingsbreedteklasse(),
                h.getGescheidenRijbaan(),
                h.getVerhardingstype(),
                h.getAantalRijstroken(),
                h.getHoogteniveau(),
                h.getStatus(),
                h.getNaam(),
                h.getIsBAGnaam(),
                h.getaWegnummer(),
                h.getnWegnummer(),
                h.geteWegnummer(),
                h.getsWegnummer(),
                h.getAfritnummer(),
                h.getAfritnaam(),
                h.getKnooppuntnaam(),
                h.getBrugnaam(),
                h.getTunnelnaam(),
                nativeHartGeom,
                nativeGeom);
        String query = "INSERT INTO " + h.getTopnltype() + ".wegdeel (" + getTopNLEntityColumns() + ",typeInfrastructuur,typeWeg,hoofdverkeersgebruik,fysiekVoorkomen,verhardingsbreedteklasse,gescheidenRijbaan,verhardingstype,aantalRijstroken,hoogteniveau,status,naam,isBAGnaam,aWegnummer,nWegnummer,eWegnummer,sWegnummer,afritnummer,afritnaam,knooppuntnaam,brugnaam,tunnelnaam,hartGeometrie,geometrie) VALUES (" + getTopNLEntityReplacementChars() + ",?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        Wegdeel inserted = run.insert(query,
                handler,
                args);

        return inserted;
    }

    private FunctioneelGebied saveFunctioneelGebied(TopNLEntity entity) throws SQLException, ParseException {
        FunctioneelGebied h = (FunctioneelGebied) entity;

        ResultSetHandler<FunctioneelGebied> handler = new BeanHandler(FunctioneelGebied.class, new BasicRowProcessor(new DbUtilsGeometryColumnConverter(gjc)));
        Object nativeGeom = gjc.convertToNativeGeometryObject(h.getGeometrie());
        Object[] args = getVarargs(entity,
                h.getTypeFunctioneelGebied(),
                h.getSoortnaam(),
                h.getNaamNL(),
                h.getNaamFries(),
                nativeGeom);
        FunctioneelGebied inserted = run.insert("INSERT INTO " + h.getTopnltype() + ".functioneelgebied (" + getTopNLEntityColumns() + ",typefunctioneelgebied,soortnaam,naamnl,naamfries,geometrie) VALUES (" + getTopNLEntityReplacementChars() + ",?,?,?,?,?)",
                handler,
                args);

        return inserted;
    }

    private Hoogte saveHoogte(TopNLEntity entity) throws SQLException, ParseException {
        Hoogte h = (Hoogte) entity;

        ResultSetHandler<Hoogte> handler = new BeanHandler(Hoogte.class, new BasicRowProcessor(new DbUtilsGeometryColumnConverter(gjc)));
        Object nativeGeom = gjc.convertToNativeGeometryObject(h.getGeometrie());
        Object[] args = getVarargs(entity,
                h.getTypeHoogte(),
                h.getReferentieVlak(),
                h.getHoogte(),
                nativeGeom);
        Hoogte inserted = run.insert("INSERT INTO " + h.getTopnltype() + ".hoogte (" + getTopNLEntityColumns() + ",typeHoogte,referentieVlak,hoogte, geometrie) VALUES (" + getTopNLEntityReplacementChars() + ",?,?,?,?)",
                handler,
                args);

        return inserted;
    }

    private String getTopNLEntityColumns() {
        return "identificatie,topnltype,brontype,bronactualiteit,bronbeschrijving,bronnauwkeurigheid,objectBeginTijd,objectEindTijd,visualisatieCode";
    }

    private String getTopNLEntityReplacementChars() {
        return "?,?,?,?,?,?,?,?,?";
    }

    private Object[] getVarargs(TopNLEntity entity, Object... specificArgs) {
        Object[] genericArgs = {
            entity.getIdentificatie(),
            entity.getTopnltype(),
            entity.getBrontype(),
            entity.getBronactualiteit() != null ? new java.sql.Date(entity.getBronactualiteit().getTime()) : null,
            entity.getBronbeschrijving(),
            entity.getBronnauwkeurigheid(),
            entity.getObjectBeginTijd() != null ? new java.sql.Date(entity.getObjectBeginTijd().getTime()) : null,
            entity.getObjectEindTijd() != null ? new java.sql.Date(entity.getObjectEindTijd().getTime()) : null,
            entity.getVisualisatieCode()
        };
        int numGeneric = genericArgs.length;
        Object[] completeArgs = new Object[numGeneric + specificArgs.length];
        System.arraycopy(genericArgs, 0, completeArgs, 0, numGeneric);

        for (int i = 0; i < specificArgs.length; i++) {
            Object specificArg = specificArgs[i];
            completeArgs[numGeneric + i] = specificArg;
        }

        return completeArgs;
    }

    public GeometryJdbcConverter getGjc() {
        return gjc;
    }

}
