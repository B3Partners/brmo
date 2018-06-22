package nl.b3p.brmo.service.scanner;

import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.util.BrmoException;
import nl.b3p.brmo.persistence.staging.AutomatischProces;
import nl.b3p.brmo.persistence.staging.BGTMutatieServiceStandProces;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.stripesstuff.stripersist.Stripersist;

import javax.persistence.Transient;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import static nl.b3p.brmo.persistence.staging.AutomatischProces.ProcessingStatus.PROCESSING;

public class BGTMutatieServiceStandOphalen extends AbstractExecutableProces {
    private static final Log LOG = LogFactory.getLog(BGTMutatieServiceStandOphalen.class);

    private final BGTMutatieServiceStandProces config;

    @Transient
    private ProgressUpdateListener listener;

    public BGTMutatieServiceStandOphalen(BGTMutatieServiceStandProces config) {
        this.config = config;
    }
    @Override
    public void execute() throws BrmoException {
        this.execute(new ProgressUpdateListener() {
            @Override
            public void total(long total) {
            }

            @Override
            public void progress(long progress) {
            }

            @Override
            public void exception(Throwable t) {
                LOG.error(t);
            }

            @Override
            public void updateStatus(String status) {
            }

            @Override
            public void addLog(String log) {
            }
        });
    }

    @Override
    public void execute(ProgressUpdateListener listener) {
        this.listener = listener;
        config.setStatus(PROCESSING);
        config.setLastrun(new Date());
        Stripersist.getEntityManager().merge(config);
        Stripersist.getEntityManager().flush();

        StringBuilder sb = new StringBuilder(AutomatischProces.LOG_NEWLINE);
        String oldLog = config.getLogfile();
        if (oldLog != null) {
            if (oldLog.length() > OLD_LOG_LENGTH) {
                sb.append(oldLog.substring(oldLog.length() - OLD_LOG_LENGTH / 10));
            } else {
                sb.append(oldLog);
            }
        }

        long totaal = 0, aantalGeladen = 0;

        String msg = String.format("Het BGT GML Light ophalen proces met ID %d is gestart op %tc.", config.getId(), Calendar.getInstance());
        LOG.info(msg);
        listener.addLog(msg);
        sb.append(msg).append(AutomatischProces.LOG_NEWLINE);

        // opstellen verzoek
        String filter = config.getGeoFilter() ;
        msg = "Filter voor op te halen grid cellen is: " + filter;
        sb.append(msg).append(AutomatischProces.LOG_NEWLINE);
        LOG.info(msg);
        listener.addLog(msg);
        listener.updateStatus(msg);

        String bUrl = config.getOphaalUrl();
        LOG.info("BGT GML light ophaal basis url: " + bUrl);

        BrmoFramework brmo = null;

        String naam;

        // /api/v2/deltas/bgtv3/citygml/download-full
        StringBuilder sUrl = new StringBuilder();
        sUrl.append(config.getOphaalUrl())
                .append("/api/v2/deltas/")
                .append(config.getDataset())
                .append("/")
                .append(config.getFormat())
                .append("/")
                .append("/download-full")
                .append("/")
                ;


// moet dan iets zijn als:
// https://test.downloads.pdok.nl/api/v2/deltas/bgtv3/citygml/download-full?excludedtypes=plaatsbepalingspunt&geographischFilter=%7B%22layers%22%3A%5B%7B%22aggregateLevel%22%3A0%2C%22codes%22%3A%5B52764%5D%7D%5D%7D
        // uitvoeren verzoek


        // er komt een http response met de volgende headers:
/*
        Access-Control-Allow-Headers: SOAPAction,X-Requested-With,Content-Type,Origin,Authorization,Accept
        Access-Control-Allow-Methods: POST, GET, OPTIONS, HEAD
        Access-Control-Allow-Origin: *
        Access-Control-Max-Age: 1000
        Content-Disposition: attachment; filename=fulldownload.zip
        Content-Type: application/zip
        Date: Fri, 22 Jun 2018 13:17:46 GMT
        Strict-Transport-Security: max-age=31536000; includeSubDomains; preload
        Transfer-Encoding: chunked
        X-Cnection: close

                */

// in de zip een xml bestand (mogelijk meer dan 1) met naam bgtv3_all.xml
// dat begin als volgt
/*

<?xml version="1.0" encoding="UTF-8"?>
<mlb:bgtv3Mutaties
                xmlns="http://www.opengis.net/citygml/2.0"
        xmlns:gml="http://www.opengis.net/gml"
        xmlns:mlb="http://www.kadaster.nl/schemas/mutatielevering-bgtv3/1.0"
        xmlns:ml="http://www.kadaster.nl/schemas/mutatielevering-generiek/1.0"
        xmlns:imgeo="http://www.geostandaarden.nl/imgeo/2.1"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.kadaster.nl/schemas/mutatielevering-bgtv3/1.0 mutatielevering-bgtv3-1.0.xsd">
    <ml:mutatieBericht>
        <ml:dataset>bgtv3</ml:dataset>
        <ml:inhoud>
            <ml:gebied>52764</ml:gebied>
            <ml:aanleveringId>27d90524-2a24-46dd-8e94-cf4957a0041d</ml:aanleveringId>
            <ml:objectTypen>
                <ml:objectType>bak</ml:objectType>
                <ml:objectType>begroeidterreindeel</ml:objectType>
                <ml:objectType>bord</ml:objectType>
                <ml:objectType>buurt</ml:objectType>
                <ml:objectType>functioneelgebied</ml:objectType>
                <ml:objectType>gebouwinstallatie</ml:objectType>
                <ml:objectType>installatie</ml:objectType>
                <ml:objectType>kast</ml:objectType>
                <ml:objectType>kunstwerkdeel</ml:objectType>
                <ml:objectType>mast</ml:objectType>
                <ml:objectType>onbegroeidterreindeel</ml:objectType>
                <ml:objectType>ondersteunendwaterdeel</ml:objectType>
                <ml:objectType>ondersteunendwegdeel</ml:objectType>
                <ml:objectType>ongeclassificeerdobject</ml:objectType>
                <ml:objectType>openbareruimtelabel</ml:objectType>
                <ml:objectType>overbruggingsdeel</ml:objectType>
                <ml:objectType>overigbouwwerk</ml:objectType>
                <ml:objectType>overigescheiding</ml:objectType>
                <ml:objectType>paal</ml:objectType>
                <ml:objectType>pand</ml:objectType>
                <ml:objectType>put</ml:objectType>
                <ml:objectType>scheiding</ml:objectType>
                <ml:objectType>sensor</ml:objectType>
                <ml:objectType>spoor</ml:objectType>
                <ml:objectType>straatmeubilair</ml:objectType>
                <ml:objectType>tunneldeel</ml:objectType>
                <ml:objectType>vegetatieobject</ml:objectType>
                <ml:objectType>waterdeel</ml:objectType>
                <ml:objectType>waterinrichtingselement</ml:objectType>
                <ml:objectType>wegdeel</ml:objectType>
                <ml:objectType>weginrichtingselement</ml:objectType>
                <ml:objectType>wijk</ml:objectType>
            </ml:objectTypen>
        </ml:inhoud>
        <!-- meer metadatavelden, nog te bepalen -->
        <!-- Dag 1: aanmaken van de eerste versie. -->
        <ml:mutatieGroep>
            <ml:toevoeging objectType="begroeidterreindeel" objectId="L0003.07567eab30c6484380e6ca3b7b0215ff">
                <ml:wordt id="995b8cf9-cc85-11e7-99e1-0f07bc5cf403">
                    <mlb:bgtv3Object>
                    <cityObjectMember><PlantCover xmlns="http://www.opengis.net/citygml/vegetation/2.0" gml:id="b995b8cf9-cc85-11e7-99e1-0f07bc5cf403"><creationDate xmlns="http://www.opengis.net/citygml/2.0">2016-06-06</creationDate><imgeo:LV-publicatiedatum>2016-07-01T22:47:52.000</imgeo:LV-publicatiedatum><imgeo:relatieveHoogteligging>0</imgeo:relatieveHoogteligging><imgeo:inOnderzoek>false</imgeo:inOnderzoek><imgeo:tijdstipRegistratie>2016-06-06T09:13:54.000</imgeo:tijdstipRegistratie><imgeo:identificatie><imgeo:NEN3610ID><imgeo:namespace>NL.IMGeo</imgeo:namespace><imgeo:lokaalID>L0003.07567eab30c6484380e6ca3b7b0215ff</imgeo:lokaalID></imgeo:NEN3610ID></imgeo:identificatie><imgeo:bronhouder>L0003</imgeo:bronhouder><imgeo:bgt-status codeSpace="http://www.geostandaarden.nl/imgeo/def/2.1#Status">bestaand</imgeo:bgt-status><imgeo:plus-status codeSpace="http://www.geostandaarden.nl/imgeo/def/2.1#VoidReasonValue">geenWaarde</imgeo:plus-status><class codeSpace="http://www.geostandaarden.nl/imgeo/def/2.1#FysiekVoorkomenBegroeidTerrein">groenvoorziening</class><imgeo:geometrie2dBegroeidTerreindeel><gml:Polygon xmlns:gml="http://www.opengis.net/gml" srsName="urn:ogc:def:crs:EPSG::28992"><gml:exterior><gml:LinearRing><gml:posList srsDimension="2">232638.151 556888.890 232638.159 556889.090 232636.791 556889.142 232636.843 556890.512 232636.844 556890.699 232636.651 556890.720 232628.271 556891.041 232628.263 556890.841 232628.255 556890.641 232636.436 556890.328 232636.383 556888.958 232636.372 556888.773 232636.575 556888.750 232638.143 556888.690 232638.151 556888.890</gml:posList></gml:LinearRing></gml:exterior></gml:Polygon></imgeo:geometrie2dBegroeidTerreindeel><imgeo:begroeidTerreindeelOpTalud>false</imgeo:begroeidTerreindeelOpTalud><imgeo:kruinlijnBegroeidTerreindeel xsi:nil="true" nilReason="waardeOnbekend" /><imgeo:plus-fysiekVoorkomen codeSpace="http://www.geostandaarden.nl/imgeo/def/2.1#VoidReasonValue">waardeOnbekend</imgeo:plus-fysiekVoorkomen></PlantCover></cityObjectMember>
                    </mlb:bgtv3Object>
                </ml:wordt>
            </ml:toevoeging>
        </ml:mutatieGroep>

        */

        // uitlezen delta van de stand, die is nodig voor het opvragen van de volgende mutatie(s)


    }
}
