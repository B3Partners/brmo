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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.WKTReader;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
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
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 *
 * @author Meine Toonen meinetoonen@b3partners.nl
 */
@RunWith(Parameterized.class)
public class DatabaseTest extends TestUtil {

    private final static Log LOG = LogFactory.getLog(DatabaseTest.class);
    private Database instance = null;
    private final WKTReader wkt = new WKTReader();
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private final String identificatie = "1616161616";
    private TopNLType type;

    @Parameters(name = "Type: {0}")
    public static Collection<Object[]> params() {
        return Arrays.asList(new Object[][]{
            {TopNLType.TOP10NL},
            {TopNLType.TOP50NL},
            {TopNLType.TOP100NL},
            {TopNLType.TOP250NL}
        }
        );
    }

    public DatabaseTest(TopNLType type) {
        this.type = type;
        this.useDB = true;
    }

    @Before
    public void before() throws SQLException {
        instance = new Database(datasource);
    }

    @Test
    public void testSaveHoogte() throws SQLException, ParseException, com.vividsolutions.jts.io.ParseException {
        LOG.debug("save");
        Geometry p = wkt.read("POINT (1 2)");
        Hoogte e = new Hoogte();
        getStandardTestTopNLEntity(e, type);
        e.setGeometrie(p);
        e.setReferentieVlak("uitgevlakt");
        e.setTypeHoogte("superhoog");
        e.setHoogte(16.06);

        instance.save(e);

        ResultSetHandler<Hoogte> h = new BeanHandler<>(Hoogte.class, new BasicRowProcessor(new DbUtilsGeometryColumnConverter(instance.getGjc())));

        Hoogte real = run.query("SELECT * FROM " + type.getType() + ".hoogte WHERE identificatie=?", h, identificatie);
        assertNotNull("Insert failed", real);
        testStandardTopNLEntity(real, e);

        assertEquals(e.getReferentieVlak(), real.getReferentieVlak());
        assertEquals(e.getTypeHoogte(), real.getTypeHoogte());
        assertEquals(e.getHoogte(), real.getHoogte());
        assertEquals(p, real.getGeometrie());
    }

    @Test
    public void testSavePlanTopografie() throws SQLException, ParseException, com.vividsolutions.jts.io.ParseException {
        Geometry p = wkt.read("POINT (1 2)");
        PlanTopografie e = new PlanTopografie();
        getStandardTestTopNLEntity(e, type);
        e.setGeometrie(p);
        e.setNaam("plannetje");
        e.setTypePlanTopografie("Typetje");

        instance.save(e);

        ResultSetHandler<PlanTopografie> h = new BeanHandler<>(PlanTopografie.class, new BasicRowProcessor(new DbUtilsGeometryColumnConverter(instance.getGjc())));

        PlanTopografie real = run.query("SELECT * FROM " + type.getType() + ".plantopografie WHERE identificatie=?", h, identificatie);
        assertNotNull("Insert failed", real);
        testStandardTopNLEntity(real, e);
        assertEquals(p, real.getGeometrie());
        assertEquals(e.getNaam(), real.getNaam());
        assertEquals(e.getTypePlanTopografie(), real.getTypePlanTopografie());
    }

    @Test
    public void testSaveGebouw() throws SQLException, ParseException, com.vividsolutions.jts.io.ParseException {
        Geometry p = wkt.read("POINT (1 2)");
        Gebouw e = new Gebouw();
        getStandardTestTopNLEntity(e, type);
        e.setGeometrie(p);
        e.setHoogte(16.06);
        e.setTypeGebouw("flat");
        e.setStatus("heel");
        e.setFysiekVoorkomen("vervallen");
        e.setHoogteklasse("superduperhoog");
        e.setSoortnaam("superflat");
        e.setNaam("flatje");
        e.setNaamFries("flaotjah");

        instance.save(e);

        ResultSetHandler<Gebouw> h = new BeanHandler<>(Gebouw.class, new BasicRowProcessor(new DbUtilsGeometryColumnConverter(instance.getGjc())));

        Gebouw real = run.query("SELECT * FROM " + type.getType() + ".gebouw WHERE identificatie=?", h, identificatie);
        assertNotNull("Insert failed", real);
        testStandardTopNLEntity(real, e);

        assertEquals(e.getHoogte(), real.getHoogte());
        assertEquals(p, real.getGeometrie());
        assertEquals(e.getTypeGebouw(), real.getTypeGebouw());
        assertEquals(e.getStatus(), real.getStatus());
        assertEquals(e.getFysiekVoorkomen(), real.getFysiekVoorkomen());
        assertEquals(e.getHoogteklasse(), real.getHoogteklasse());
        assertEquals(e.getSoortnaam(), real.getSoortnaam());
        assertEquals(e.getNaam(), real.getNaam());
        assertEquals(e.getNaamFries(), real.getNaamFries());
    }

    @Test
    public void testSaveGeografischGebied() throws SQLException, ParseException, com.vividsolutions.jts.io.ParseException {
        Geometry p = wkt.read("POINT (1 2)");
        GeografischGebied e = new GeografischGebied();
        getStandardTestTopNLEntity(e, type);
        e.setGeometrie(p);
        e.setTypeGeografischGebied("landgoed");
        e.setNaamNL("Naamnl");
        e.setNaamFries("NaamFrysk");

        instance.save(e);

        ResultSetHandler<GeografischGebied> h = new BeanHandler<>(GeografischGebied.class, new BasicRowProcessor(new DbUtilsGeometryColumnConverter(instance.getGjc())));

        GeografischGebied real = run.query("SELECT * FROM " + type.getType() + ".geografischgebied WHERE identificatie=?", h, identificatie);
        assertNotNull("Insert failed", real);
        testStandardTopNLEntity(real, e);

        assertEquals(p, real.getGeometrie());
        assertEquals(e.getNaamNL(), real.getNaamNL());
        assertEquals(e.getNaamFries(), real.getNaamFries());
        assertEquals(e.getTypeGeografischGebied(), real.getTypeGeografischGebied());
    }

    @Test
    public void testSaveInrichtingselement() throws SQLException, ParseException, com.vividsolutions.jts.io.ParseException {
        Geometry p = wkt.read("POINT (1 2)");
        Inrichtingselement e = new Inrichtingselement();
        getStandardTestTopNLEntity(e, type);
        e.setGeometrie(p);
        e.setTypeInrichtingselement("Gesloten");
        e.setHoogteniveau(778L);
        e.setSoortnaam("MaxPen");
        e.setStatus("in gebruik");

        instance.save(e);

        ResultSetHandler<Inrichtingselement> h = new BeanHandler<>(Inrichtingselement.class, new BasicRowProcessor(new DbUtilsGeometryColumnConverter(instance.getGjc())));

        Inrichtingselement real = run.query("SELECT * FROM " + type.getType() + ".inrichtingselement WHERE identificatie=?", h, identificatie);
        assertNotNull("Insert failed", real);
        testStandardTopNLEntity(real, e);

        assertEquals(p, real.getGeometrie());
        assertEquals(e.getTypeInrichtingselement(), real.getTypeInrichtingselement());
        assertEquals(e.getHoogteniveau(), real.getHoogteniveau());
        assertEquals(e.getSoortnaam(), real.getSoortnaam());
        assertEquals(e.getStatus(), real.getStatus());
    }

    @Test
    public void testSavePlaats() throws SQLException, ParseException, com.vividsolutions.jts.io.ParseException {
        Geometry p = wkt.read("POINT (1 2)");
        Plaats e = new Plaats();
        getStandardTestTopNLEntity(e, type);
        e.setGeometrie(p);
        e.setTypeGebied("woonplaats");
        e.setAantalInwoners(16L);
        e.setNaamOfficieel("Gouda");
        e.setNaamNL("Gouda");
        e.setNaamFries("mompelmompel");
        instance.save(e);

        ResultSetHandler<Plaats> h = new BeanHandler<>(Plaats.class, new BasicRowProcessor(new DbUtilsGeometryColumnConverter(instance.getGjc())));

        Plaats real = run.query("SELECT * FROM " + type.getType() + ".plaats WHERE identificatie=?", h, identificatie);
        assertNotNull("Insert failed", real);
        testStandardTopNLEntity(real, e);

        assertEquals(p, real.getGeometrie());
        assertEquals(e.getTypeGebied(), real.getTypeGebied());
        assertEquals(e.getAantalInwoners(), real.getAantalInwoners());
        assertEquals(e.getNaamOfficieel(), real.getNaamOfficieel());
        assertEquals(e.getNaamNL(), real.getNaamNL());
        assertEquals(e.getNaamFries(), real.getNaamFries());
    }

    @Test
    public void testSaveRegistratiefGebied() throws SQLException, ParseException, com.vividsolutions.jts.io.ParseException {
        Geometry p = wkt.read("POINT (1 2)");
        RegistratiefGebied e = new RegistratiefGebied();
        getStandardTestTopNLEntity(e, type);
        e.setGeometrie(p);
        e.setTypeRegistratiefGebied("woonplaats");
        e.setNummer("16");
        e.setNaamOfficieel("Gouda");
        e.setNaamNL("Gouda");
        e.setNaamFries("mompelmompel");
        instance.save(e);

        ResultSetHandler<RegistratiefGebied> h = new BeanHandler<>(RegistratiefGebied.class, new BasicRowProcessor(new DbUtilsGeometryColumnConverter(instance.getGjc())));

        RegistratiefGebied real = run.query("SELECT * FROM " + type.getType() + ".registratiefgebied WHERE identificatie=?", h, identificatie);
        assertNotNull("Insert failed", real);
        testStandardTopNLEntity(real, e);

        assertEquals(p, real.getGeometrie());
        assertEquals(e.getTypeRegistratiefGebied(), real.getTypeRegistratiefGebied());
        assertEquals(e.getNummer(), real.getNummer());
        assertEquals(e.getNaamOfficieel(), real.getNaamOfficieel());
        assertEquals(e.getNaamNL(), real.getNaamNL());
        assertEquals(e.getNaamFries(), real.getNaamFries());
    }

    @Test
    public void testSaveRelief() throws SQLException, ParseException, com.vividsolutions.jts.io.ParseException {
        LineString p = (LineString) wkt.read("LineString(1 2, 3 4, 5 6)");
        Relief e = new Relief();
        getStandardTestTopNLEntity(e, type);
        e.setGeometrie(p);
        e.setTaludHogeZijde(p);
        e.setTaludLageZijde(p);
        e.setTypeRelief("Berg");
        e.setHoogteklasse("Superhoog");
        e.setHoogteniveau(666L);

        instance.save(e);

        ResultSetHandler<Relief> h = new BeanHandler<>(Relief.class, new BasicRowProcessor(new DbUtilsGeometryColumnConverter(instance.getGjc())));

        Relief real = run.query("SELECT * FROM " + type.getType() + ".relief WHERE identificatie=?", h, identificatie);
        assertNotNull("Insert failed", real);
        testStandardTopNLEntity(real, e);

        assertEquals(p, real.getGeometrie());
        assertEquals(p, real.getTaludLageZijde());
        assertEquals(p, real.getTaludHogeZijde());
        assertEquals(e.getTypeRelief(), real.getTypeRelief());
        assertEquals(e.getHoogteklasse(), real.getHoogteklasse());
        assertEquals(e.getHoogteniveau(), real.getHoogteniveau());
    }

    @Test
    public void testSaveReliefEmptyTaludGeoms() throws SQLException, ParseException, com.vividsolutions.jts.io.ParseException {
        LineString p = (LineString) wkt.read("LineString(1 2, 3 4, 5 6)");
        Relief e = new Relief();
        getStandardTestTopNLEntity(e, type);
        e.setGeometrie(p);
        //  e.setTaludHogeZijde(p);
        //  e.setTaludLageZijde(p);
        e.setTypeRelief("Berg");
        e.setHoogteklasse("Superhoog");
        e.setHoogteniveau(666L);

        instance.save(e);

        ResultSetHandler<Relief> h = new BeanHandler<>(Relief.class, new BasicRowProcessor(new DbUtilsGeometryColumnConverter(instance.getGjc())));

        Relief real = run.query("SELECT * FROM " + type.getType() + ".relief WHERE identificatie=?", h, identificatie);
        assertNotNull("Insert failed", real);
        testStandardTopNLEntity(real, e);

        assertEquals(p, real.getGeometrie());
        assertEquals(null, real.getTaludLageZijde());
        assertEquals(null, real.getTaludHogeZijde());
        assertEquals(e.getTypeRelief(), real.getTypeRelief());
        assertEquals(e.getHoogteklasse(), real.getHoogteklasse());
        assertEquals(e.getHoogteniveau(), real.getHoogteniveau());
    }

    @Test
    public void testSaveReliefEmptyHoofdGeom() throws SQLException, ParseException, com.vividsolutions.jts.io.ParseException {
        LineString p = (LineString) wkt.read("LineString(1 2, 3 4, 5 6)");
        Relief e = new Relief();
        getStandardTestTopNLEntity(e, type);
        // e.setGeometrie(p);
        e.setTaludHogeZijde(p);
        e.setTaludLageZijde(p);
        e.setTypeRelief("Berg");
        e.setHoogteklasse("Superhoog");
        e.setHoogteniveau(666L);

        instance.save(e);

        ResultSetHandler<Relief> h = new BeanHandler<>(Relief.class, new BasicRowProcessor(new DbUtilsGeometryColumnConverter(instance.getGjc())));

        Relief real = run.query("SELECT * FROM " + type.getType() + ".relief WHERE identificatie=?", h, identificatie);
        assertNotNull("Insert failed", real);
        testStandardTopNLEntity(real, e);

        assertEquals(null, real.getGeometrie());
        assertEquals(p, real.getTaludLageZijde());
        assertEquals(p, real.getTaludHogeZijde());
        assertEquals(e.getTypeRelief(), real.getTypeRelief());
        assertEquals(e.getHoogteklasse(), real.getHoogteklasse());
        assertEquals(e.getHoogteniveau(), real.getHoogteniveau());
    }

    @Test
    public void testSaveSpoorbaandeel() throws SQLException, ParseException, com.vividsolutions.jts.io.ParseException {
        LineString p = (LineString) wkt.read("LineString(1 2, 3 4, 5 6)");
        Spoorbaandeel e = new Spoorbaandeel();
        getStandardTestTopNLEntity(e, type);
        e.setGeometrie(p);
        e.setTypeInfrastructuur("spoor");
        e.setTypeSpoorbaan("treinspoor");
        e.setFysiekVoorkomen("een treinspoort");
        e.setSpoorbreedte("smal");
        e.setAantalSporen("8");
        e.setVervoerfunctie("Mensen");
        e.setElektrificatie(true);
        e.setStatus("in gebruik");
        e.setBrugnaam("Brugge");
        e.setTunnelnaam("Tunnele");
        e.setBaanvaknaam("lalala");
        e.setHoogteniveau(1L);

        instance.save(e);

        ResultSetHandler<Spoorbaandeel> h = new BeanHandler<>(Spoorbaandeel.class, new BasicRowProcessor(new DbUtilsGeometryColumnConverter(instance.getGjc())));

        Spoorbaandeel real = run.query("SELECT * FROM " + type.getType() + ".spoorbaandeel WHERE identificatie=?", h, identificatie);
        assertNotNull("Insert failed", real);
        testStandardTopNLEntity(real, e);

        assertEquals(p, real.getGeometrie());
        assertEquals(e.getTypeInfrastructuur(), real.getTypeInfrastructuur());
        assertEquals(e.getTypeSpoorbaan(), real.getTypeSpoorbaan());
        assertEquals(e.getFysiekVoorkomen(), real.getFysiekVoorkomen());
        assertEquals(e.getSpoorbreedte(), real.getSpoorbreedte());
        assertEquals(e.getAantalSporen(), real.getAantalSporen());
        assertEquals(e.getVervoerfunctie(), real.getVervoerfunctie());
        assertEquals(e.getElektrificatie(), real.getElektrificatie());
        assertEquals(e.getStatus(), real.getStatus());
        assertEquals(e.getBrugnaam(), real.getBrugnaam());
        assertEquals(e.getTunnelnaam(), real.getTunnelnaam());
        assertEquals(e.getBaanvaknaam(), real.getBaanvaknaam());
        assertEquals(e.getHoogteniveau(), real.getHoogteniveau());
    }

    @Test
    public void testSaveTerrein() throws SQLException, ParseException, com.vividsolutions.jts.io.ParseException {
        Polygon p = (Polygon) wkt.read("Polygon((1 2, 3 4, 5 6, 1 2))");
        Terrein e = new Terrein();
        getStandardTestTopNLEntity(e, type);
        e.setGeometrie(p);
        e.setTypeLandgebruik("festivalterrein");
        e.setNaam("biddinghuizen");

        instance.save(e);

        ResultSetHandler<Terrein> h = new BeanHandler<>(Terrein.class, new BasicRowProcessor(new DbUtilsGeometryColumnConverter(instance.getGjc())));

        Terrein real = run.query("SELECT * FROM " + type.getType() + ".terrein WHERE identificatie=?", h, identificatie);
        assertNotNull("Insert failed", real);
        testStandardTopNLEntity(real, e);

        assertEquals(p, real.getGeometrie());
        assertEquals(e.getTypeLandgebruik(), real.getTypeLandgebruik());
        assertEquals(e.getNaam(), real.getNaam());
    }

    @Test
    public void testWaterdeel() throws SQLException, ParseException, com.vividsolutions.jts.io.ParseException {
        Geometry p = wkt.read("Polygon((1 2, 3 4, 5 6, 1 2))");
        Waterdeel e = new Waterdeel();
        getStandardTestTopNLEntity(e, type);
        e.setGeometrie(p);
        e.setTypeWater("Slotgracht");
        e.setBreedteklasse("middenmoat");
        e.setFysiekVoorkomen("nat");
        e.setVoorkomen("nat");
        e.setGetijdeinvloed(false);
        e.setVaarwegklasse("gevaarlijk");
        e.setNaamOfficieel("Slotgracht");
        e.setNaamNL("Slotgracht");
        e.setNaamFries("SloatGraogt");
        e.setIsBAGnaam(false);
        e.setSluisnaam("lala");
        e.setBrugnaam("Ophaal");
        e.setHoogteniveau(8L);
        e.setFunctie("functie");
        e.setHoofdAfwatering(true);
        instance.save(e);

        ResultSetHandler<Waterdeel> h = new BeanHandler<>(Waterdeel.class, new BasicRowProcessor(new DbUtilsGeometryColumnConverter(instance.getGjc())));

        Waterdeel real = run.query("SELECT * FROM " + type.getType() + ".waterdeel WHERE identificatie=?", h, identificatie);
        assertNotNull("Insert failed", real);
        testStandardTopNLEntity(real, e);

        assertEquals(p, real.getGeometrie());
        assertEquals(e.getTypeWater(), real.getTypeWater());
        assertEquals(e.getBreedteklasse(), real.getBreedteklasse());
        assertEquals(e.getFysiekVoorkomen(), real.getFysiekVoorkomen());
        assertEquals(e.getVoorkomen(), real.getVoorkomen());
        assertEquals(e.getGetijdeinvloed(), real.getGetijdeinvloed());
        assertEquals(e.getVaarwegklasse(), real.getVaarwegklasse());
        assertEquals(e.getNaamOfficieel(), real.getNaamOfficieel());
        assertEquals(e.getNaamNL(), real.getNaamNL());
        assertEquals(e.getNaamFries(), real.getNaamFries());
        assertEquals(e.getIsBAGnaam(), real.getIsBAGnaam());
        assertEquals(e.getSluisnaam(), real.getSluisnaam());
        assertEquals(e.getBrugnaam(), real.getBrugnaam());
        assertEquals(e.getHoogteniveau(), real.getHoogteniveau());
        assertEquals(e.getFunctie(), real.getFunctie());
        assertEquals(e.isHoofdAfwatering(), real.isHoofdAfwatering());
    }

    @Test
    public void testSaveFunctioneelgebied() throws SQLException, ParseException, com.vividsolutions.jts.io.ParseException {
        Geometry p = wkt.read("POINT (1 2)");
        FunctioneelGebied e = new FunctioneelGebied();
        getStandardTestTopNLEntity(e, type);
        e.setGeometrie(p);
        e.setNaamFries("boers");
        e.setNaamNL("normaal");
        e.setSoortnaam("iets");
        e.setTypeFunctioneelGebied("typerdepiep");

        instance.save(e);

        ResultSetHandler<FunctioneelGebied> h = new BeanHandler<>(FunctioneelGebied.class, new BasicRowProcessor(new DbUtilsGeometryColumnConverter(instance.getGjc())));

        FunctioneelGebied real = run.query("SELECT * FROM " + type.getType() + ".functioneelgebied WHERE identificatie=?", h, identificatie);
        assertNotNull("Insert failed", real);
        testStandardTopNLEntity(real, e);

        assertEquals(p, real.getGeometrie());
        assertEquals(e.getNaamFries(), real.getNaamFries());
        assertEquals(e.getNaamNL(), real.getNaamNL());
        assertEquals(e.getTypeFunctioneelGebied(), real.getTypeFunctioneelGebied());
        assertEquals(e.getSoortnaam(), real.getSoortnaam());
    }

    @Test
    public void testSaveWegdeel() throws SQLException, ParseException, com.vividsolutions.jts.io.ParseException {
        Geometry p = wkt.read("POINT (1 2)");
        Wegdeel e = new Wegdeel();
        getStandardTestTopNLEntity(e, type);
        e.setGeometrie(p);
        e.setHartGeometrie(p);
        e.setTypeInfrastructuur("Snelweg");
        e.setTypeWeg("snelweg");
        e.setHoofdverkeersgebruik("auto's");
        e.setFysiekVoorkomen("666 baansweg");
        e.setVerhardingsbreedteklasse("Vrij hard");
        e.setGescheidenRijbaan(false);
        e.setVerhardingstype("asfalt");
        e.setAantalRijstroken(666L);
        e.setHoogteniveau(1L);
        e.setStatus("in gebruik");
        e.setNaam("Route 666");
        e.setIsBAGnaam(false);
        e.setaWegnummer("666");
        e.setnWegnummer("667");
        e.seteWegnummer("668");
        e.setsWegnummer("669");
        e.setAfritnummer("8");
        e.setAfritnaam("Afrit 8");
        e.setKnooppuntnaam("Knooppunt lucifer");
        e.setBrugnaam("Vagevuur");
        e.setTunnelnaam("Hades");

        instance.save(e);

        ResultSetHandler<Wegdeel> h = new BeanHandler<>(Wegdeel.class, new BasicRowProcessor(new DbUtilsGeometryColumnConverter(instance.getGjc())));

        Wegdeel real = run.query("SELECT * FROM " + type.getType() + ".wegdeel WHERE identificatie=?", h, identificatie);
        assertNotNull("Insert failed", real);
        testStandardTopNLEntity(real, e);

        assertEquals(p, real.getGeometrie());
        assertEquals(p, real.getHartGeometrie());
        assertEquals(e.getTypeInfrastructuur(), real.getTypeInfrastructuur());
        assertEquals(e.getTypeWeg(), real.getTypeWeg());
        assertEquals(e.getHoofdverkeersgebruik(), real.getHoofdverkeersgebruik());
        assertEquals(e.getFysiekVoorkomen(), real.getFysiekVoorkomen());
        assertEquals(e.getVerhardingsbreedteklasse(), real.getVerhardingsbreedteklasse());
        assertEquals(e.getGescheidenRijbaan(), real.getGescheidenRijbaan());
        assertEquals(e.getVerhardingstype(), real.getVerhardingstype());
        assertEquals(e.getAantalRijstroken(), real.getAantalRijstroken());
        assertEquals(e.getHoogteniveau(), real.getHoogteniveau());
        assertEquals(e.getStatus(), real.getStatus());
        assertEquals(e.getNaam(), real.getNaam());
        assertEquals(e.getIsBAGnaam(), real.getIsBAGnaam());
        assertEquals(e.getaWegnummer(), real.getaWegnummer());
        assertEquals(e.getnWegnummer(), real.getnWegnummer());
        assertEquals(e.geteWegnummer(), real.geteWegnummer());
        assertEquals(e.getsWegnummer(), real.getsWegnummer());
        assertEquals(e.getAfritnummer(), real.getAfritnummer());
        assertEquals(e.getAfritnaam(), real.getAfritnaam());
        assertEquals(e.getKnooppuntnaam(), real.getKnooppuntnaam());
        assertEquals(e.getBrugnaam(), real.getBrugnaam());
        assertEquals(e.getTunnelnaam(), real.getTunnelnaam());
    }

    public void getStandardTestTopNLEntity(TopNLEntity e, TopNLType type) throws ParseException {
        e.setIdentificatie(identificatie);
        e.setBronactualiteit(sdf.parse("2016-06-16"));
        e.setBronbeschrijving("beschrijving");
        e.setBrontype("typje");
        e.setObjectBeginTijd(sdf.parse("2016-01-01"));
        e.setObjectEindTijd(sdf.parse("2016-01-02"));
        e.setVisualisatieCode(166L);
        e.setTopnltype(type.getType());
    }

    public void testStandardTopNLEntity(TopNLEntity real, TopNLEntity e) {
        assertEquals(e.getBronactualiteit(), real.getBronactualiteit());
        assertEquals(e.getBronbeschrijving(), real.getBronbeschrijving());
        assertEquals(e.getBronnauwkeurigheid(), real.getBronnauwkeurigheid());
        assertEquals(e.getBrontype(), real.getBrontype());
        assertEquals(e.getIdentificatie(), real.getIdentificatie());
        assertEquals(e.getObjectBeginTijd(), real.getObjectBeginTijd());
        assertEquals(e.getObjectEindTijd(), real.getObjectEindTijd());
        assertEquals(e.getTopnltype(), real.getTopnltype());
        assertEquals(e.getVisualisatieCode(), real.getVisualisatieCode());
    }
}
