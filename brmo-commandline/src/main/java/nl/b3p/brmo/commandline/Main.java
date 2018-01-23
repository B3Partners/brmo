/*
 * Copyright (C) 2017 B3Partners B.V.
 */
package nl.b3p.brmo.commandline;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.RsgbProxy;
import nl.b3p.brmo.loader.entity.Bericht;
import nl.b3p.brmo.loader.entity.LaadProces;
import nl.b3p.brmo.loader.util.BrmoDuplicaatLaadprocesException;
import nl.b3p.brmo.loader.util.BrmoException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import static nl.b3p.brmo.commandline.Main.sysexits.*;
import nl.b3p.brmo.loader.util.BrmoLeegBestandException;
import org.apache.commons.io.FileUtils;
import org.geotools.factory.GeoTools;
import org.geotools.util.logging.Logging;

/**
 * run:
 * {@code java -jar brmo-commandline.jar -versieinfo -dbprops commandline-example.properties}
 *
 * @author Boy de Wit
 * @author mprins
 */
public class Main {

    private static final Log LOG = LogFactory.getLog(Main.class);

    private static List<Option> modeOpts;

    private static List<Option> dbOpts;

    private static Options modeOptions;

    private static Options dbOptions;

    private static final Properties dbProps = new Properties();

    private static Options buildOptions() {

        dbOpts = Arrays.asList(new Option[]{
            Option.builder("db").desc("database properties file").type(File.class).longOpt("dbprops").argName("bestand").hasArg().required().numberOfArgs(1).build()
        });

        modeOpts = Arrays.asList(new Option[]{
            // info
            Option.builder("v").desc("Versie informatie van de verschillende schema's").longOpt("versieinfo")
            .optionalArg(true).numberOfArgs(1).argName("[format]").build(),
            Option.builder("l").desc("Geef overzicht van laadprocessen in staging database").longOpt("list")
            .optionalArg(true).numberOfArgs(1).argName("[format]").build(),
            Option.builder("s").desc("Geef aantallen van bericht status in staging database").longOpt("berichtstatus")
            .optionalArg(true).numberOfArgs(1).argName("[format]").build(),
            Option.builder("j").desc("Geef aantal berichten in job tabel van staging database").longOpt("jobstatus")
            .optionalArg(true).numberOfArgs(1).argName("[format]").build(),
            // laden
            Option.builder("a").desc("Laad totaalstand of mutatie uit bestand (.zip of .xml) in database").longOpt("load").hasArg(true)
            .numberOfArgs(2).argName("bestandsnaam <type-br> <[archief-directory]").build(),
            Option.builder("ad").desc("Laad stand of mutatie berichten (.zip of .xml) uit directory in database").longOpt("loaddir").hasArg(true).numberOfArgs(2).argName("directory> <type-br> <[archief-directory]").build(),
            // verwijderen
            Option.builder("d").desc("Verwijder laadprocessen in database (geef id weer met -list)").longOpt("delete")
            .hasArg().numberOfArgs(1).type(Integer.class).argName("id").build(),
            // transformeren
            Option.builder("t").desc("Transformeer alle 'STAGING_OK' berichten naar rsgb.").longOpt("torsgb")
            .optionalArg(true).numberOfArgs(1).argName("[error-state]").build(),
            Option.builder("tb").desc("Transformeer alle 'STAGING_OK' BGT-Light laadprocessen naar rsgbbgt.")
            .longOpt("torsgbbgt").optionalArg(true).numberOfArgs(1).argName("[loadingUpdate]").build(),
            // export
            Option.builder("e").desc("Maak van berichten uit staging gezipte xml-files in de opgegeven directory. Dit zijn alleen BRK mutaties van GDS2 processen.")
            .longOpt("exportgds").hasArg().numberOfArgs(1).type(File.class).argName("output-directory").build()
        });

        Options options = new Options();

        dbOptions = new Options();
        dbOpts.stream().map((Option o) -> {
            options.addOption(o);
            return o;
        }).forEach((o) -> {
            dbOptions.addOption(o);
        });

        OptionGroup g = new OptionGroup();
        g.setRequired(true);
        modeOptions = new Options();
        modeOpts.stream().map((o) -> {
            g.addOption(o);
            return o;
        }).forEach((o) -> {
            modeOptions.addOption(o);
        });
        options.addOptionGroup(g);

        return options;
    }

    private static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setOptionComparator(new Comparator<Option>() {
            @Override
            public int compare(Option lhs, Option rhs) {
                List[] lists = new List[]{modeOpts, dbOpts};
                for (List l : lists) {
                    int lhsIndex = l.indexOf(lhs);
                    if (lhsIndex != -1) {
                        return new Integer(lhsIndex).compareTo(l.indexOf(rhs));
                    }
                }
                return lhs.getArgName().compareTo(rhs.getArgName());
            }
        });
        final int W = 100;
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        formatter.printUsage(pw, 80, "java -jar brmo-commandline.jar --<actie> --dbprops <db-props>");
        formatter.printUsage(pw, 80, "[Oracle] java -cp \"brmo-commandline.jar;ojdbc8.jar;lib/*\" nl.b3p.brmo.commandline.Main --<actie> --dbprops <db-props>");
        pw.print("\n                [Omdat de Oracle jdbc driver niet gedistribueerd mag worden"
                + "\n                 dient deze zelf op de commando regel te worden opgegeven.]\n");
        pw.print("\nActies:\n");
        formatter.printOptions(pw, W, modeOptions, 2, 2);
        pw.print("Configuratie:\n");
        formatter.printOptions(pw, W, dbOptions, 2, 2);

        System.err.println(sw.toString());
    }

    public static void main(String... args) {
        Options options = buildOptions();
        CommandLine cl = null;
        try {
            CommandLineParser parser = new DefaultParser();
            cl = parser.parse(options, args);
        } catch (ParseException e) {
            LOG.fatal(e);
            printHelp();
            System.exit(EX_USAGE.code);
        }

        int exitcode = 0;
        BasicDataSource dsStaging = null;
        BasicDataSource dsRsgb = null;
        BasicDataSource dsRsgbbgt = null;

        try {
            dbProps.load(new FileInputStream(cl.getOptionValue("dbprops")));
            switch (dbProps.getProperty("dbtype")) {
                case "oracle":
                    Class.forName("oracle.jdbc.OracleDriver");
                    break;
                case "postgis":
                    Class.forName("org.postgresql.Driver");
                    break;
                case "jtds-sqlserver":
                    Class.forName("net.sourceforge.jtds.jdbc.Driver");
                    break;
                default:
                    throw new IllegalArgumentException("Het database type " + dbProps.getProperty("dbtype") + " wordt niet ondersteund of is niet opgegeven.");
            }

            dsStaging = new BasicDataSource();
            LOG.info("Verbinding maken met Staging database... ");
            dsStaging.setUrl(dbProps.getProperty("staging.url"));
            dsStaging.setUsername(dbProps.getProperty("staging.user"));
            dsStaging.setPassword(dbProps.getProperty("staging.password"));
            dsStaging.setConnectionProperties(dbProps.getProperty("staging.options", ""));

            // alleen rsgb verbinding maken als nodig
            if (cl.hasOption("torsgb") || cl.hasOption("versieinfo")) {
                LOG.info("Verbinding maken met RSGB database... ");
                dsRsgb = new BasicDataSource();
                dsRsgb.setUrl(dbProps.getProperty("rsgb.url"));
                dsRsgb.setUsername(dbProps.getProperty("rsgb.user"));
                dsRsgb.setPassword(dbProps.getProperty("rsgb.password"));
                dsRsgb.setConnectionProperties(dbProps.getProperty("rsgb.options", ""));
            }

            // alleen rsgbbgt verbinding maken als nodig
            if (cl.hasOption("versieinfo") || cl.hasOption("torsgbbgt")) {
                LOG.info("Verbinding maken met RSGB BGT database... ");
                dsRsgbbgt = new BasicDataSource();
                dsRsgbbgt.setUrl(dbProps.getProperty("rsgbbgt.url"));
                dsRsgbbgt.setUsername(dbProps.getProperty("rsgbbgt.user"));
                dsRsgbbgt.setPassword(dbProps.getProperty("rsgbbgt.password"));
                dsRsgbbgt.setConnectionProperties(dbProps.getProperty("rsgbbgt.options", ""));
            }

            // staging-only commando's
            if (cl.hasOption("list")) {
                exitcode = list(dsStaging, cl.getOptionValue("l", "text"));
            } else if (cl.hasOption("berichtstatus")) {
                exitcode = berichtStatus(dsStaging, cl.getOptionValue("berichtstatus", "text"));
            } else if (cl.hasOption("jobstatus")) {
                exitcode = jobStatus(dsStaging, cl.getOptionValue("jobstatus", "text"));
            } else if (cl.hasOption("load")) {
                // omdat we 2 verplichte argumenten hebben en 1 optionele die als String[]
                // worden doorgegeven een stream gebruike om eea aan mekaar te plakken
                exitcode = load(dsStaging, Stream.of(cl.getOptionValues("load"), cl.getArgs())
                        .flatMap(Stream::of).toArray(String[]::new));
            } else if (cl.hasOption("loaddir")) {
                exitcode = loaddir(dsStaging,
                        Stream.of(cl.getOptionValues("loaddir"), cl.getArgs())
                        .flatMap(Stream::of).toArray(String[]::new));
            } else if (cl.hasOption("delete")) {
                exitcode = delete(dsStaging, cl.getOptionValue("delete"));
            } else if (cl.hasOption("exportgds")) {
                exitcode = getMutations(dsStaging, cl.getOptionValues("exportgds"));
            } // ----------------
            // rsgb commando's
            else if (cl.hasOption("torsgb")) {
                exitcode = toRsgb(dsStaging, dsRsgb, cl.getOptionValue("berichtstatus", "ignore"));
            } // ----------------
            // rsgbbgt commando's
            else if (cl.hasOption("torsgbbgt")) {
                exitcode = toRsgbBgt(dsStaging, dsRsgbbgt, cl.getOptionValue("torsgbbgt", "false"));
            } // ----------------
            // alle schema's / databases
            else if (cl.hasOption("versieinfo")) {
                exitcode = versieInfo(dsStaging, dsRsgb, dsRsgbbgt, cl.getOptionValue("versieinfo", "text"));
            }
        } catch (BrmoException | InterruptedException ex) {
            LOG.error("Fout tijdens uitvoeren met argumenten: " + Arrays.toString(args), ex);
            System.err.println(ex.getLocalizedMessage());
            exitcode = 1;
        } catch (ClassNotFoundException ex) {
            LOG.error("Database driver is niet gevonden.", ex);
            System.err.println(ex.getLocalizedMessage());
            exitcode = EX_DATAERR.code;
        } catch (IOException ex) {
            LOG.error("Het laden van het configuratie bestand is mislukt.", ex);
            System.err.println(ex.getLocalizedMessage());
            exitcode = EX_NOINPUT.code;
        }
        try {
            if (dsStaging != null) {
                dsStaging.close();
            }
            if (dsRsgb != null) {
                dsRsgb.close();
            }
            if (dsRsgbbgt != null) {
                dsRsgbbgt.close();
            }
        } catch (SQLException ex) {
            LOG.debug("Mogelijke fout tijdens afsluiten database verbindingen.", ex);
        }

        System.exit(exitcode);
    }

    private static int toRsgb(DataSource dataSourceStaging, DataSource dataSourceRsgb, String errorState) throws BrmoException, InterruptedException {
        LOG.info("Start staging naar rsgb transformatie.");
        BrmoFramework brmo = new BrmoFramework(dataSourceStaging, dataSourceRsgb);
        brmo.setOrderBerichten(true);
        brmo.setErrorState(errorState);
        Thread t = brmo.toRsgb();
        t.join();
        LOG.info("Klaar met staging naar rsgb transformatie.");
        brmo.closeBrmoFramework();
        return 0;
    }

    private static int toRsgbBgt(BasicDataSource dsStaging, BasicDataSource dsRsgbbgt, String loadingUpdate) throws BrmoException, InterruptedException {
        LOG.info("Start staging naar rsgbbgt transformatie.");
        try {
            GeoTools.init();
            Logging.ALL.setLoggerFactory("org.geotools.util.logging.Log4JLoggerFactory");

            // Geotools maakt een eigen database verbinding via jndi
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.apache.naming.java.javaURLContextFactory");
            System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.naming");
            InitialContext ic = new InitialContext();
            ic.createSubcontext("java:");
            ic.createSubcontext("java:comp");
            ic.createSubcontext("java:comp/env");
            ic.createSubcontext("java:comp/env/jdbc");
            ic.createSubcontext("java:comp/env/jdbc/brmo");
            ic.bind("java:comp/env/jdbc/brmo/rsgbbgt", dsRsgbbgt);
        } catch (ClassNotFoundException | IllegalArgumentException | NamingException ex) {
            LOG.debug("Er is iets misgegaan bij de GeoTools of JNDI initialisatie", ex);
        }

        BrmoFramework brmo = new BrmoFramework(dsStaging, null, dsRsgbbgt);
        // bepaal welke bgtlight LP's er zijn om te transformeren
        final long[] lpIds = ArrayUtils.toPrimitive(brmo.getLaadProcessenIds("bestand_datum", "ASC", BrmoFramework.BR_BGTLIGHT, Bericht.STATUS.STAGING_OK.name()));
        brmo.setOrderBerichten(loadingUpdate.equalsIgnoreCase("true"));
        Thread t = brmo.toRsgb(RsgbProxy.BerichtSelectMode.BY_LAADPROCES, lpIds, null);
        t.join();
        LOG.info("Klaar met staging naar rsgbbgt transformatie.");
        brmo.closeBrmoFramework();
        return 0;
    }

    private static int versieInfo(DataSource dataSourceStaging, DataSource dataSourceRsgb, DataSource dataSourceRsgbBGT, String format) throws BrmoException {
        BrmoFramework brmo = new BrmoFramework(dataSourceStaging, dataSourceRsgb, dataSourceRsgbBGT);
        if (format.equalsIgnoreCase("json")) {
            StringBuilder sb = new StringBuilder("{");
            sb.append("\"staging_versie\":\"").append(brmo.getStagingVersion()).append("\",")
                    .append("\"rsgb_versie\":\"").append(brmo.getRsgbVersion()).append("\",")
                    .append("\"rsgbbgt_versie\":\"").append(brmo.getRsgbBgtVersion()).append("\"}");
            System.out.println(sb);
        } else {
            System.out.println("staging versie: " + brmo.getStagingVersion());
            System.out.println("rsgb    versie: " + brmo.getRsgbVersion());
            System.out.println("rsgbbgt versie: " + brmo.getRsgbBgtVersion());
        }
        brmo.closeBrmoFramework();
        return 0;
    }

    /**
     * Geef een lijst van alle laadprocessen.
     *
     * @param ds staging datasource
     * @return exitcode {@code 0} als succesvol
     * @throws BrmoException als er iets mis gaat in benaderen data
     */
    private static int list(DataSource ds, String format) throws BrmoException {
        LOG.info("Ophalen laadproces informatie.");
        BrmoFramework brmo = new BrmoFramework(ds, null);
        List<LaadProces> processen = brmo.listLaadProcessen();

        if (format.equalsIgnoreCase("json")) {
            StringBuilder sb = new StringBuilder();
            sb.append("{\"aantal\":").append(processen.size());
            if (!processen.isEmpty()) {
                sb.append(",\"laadprocessen\":[");
                processen.stream().forEach((lp) -> {
                    sb.append("{")
                            .append("\"id\":").append(lp.getId()).append(",")
                            .append("\"bestand_naam\":\"").append(lp.getBestandNaam()).append("\",")
                            .append("\"bestand_datum\":\"").append(lp.getBestandDatum()).append("\",")
                            .append("\"soort\":\"").append(lp.getSoort()).append("\",")
                            .append("\"status\":\"").append(lp.getStatus()).append("\",")
                            .append("\"contact\":\"").append(lp.getContactEmail()).append("\"},");
                });
                sb.deleteCharAt(sb.length() - 1);
                sb.append("]");
            }
            sb.append("}");
            System.out.println(sb);
        } else if (processen.isEmpty()) {
            System.out.println("Geen laadprocessen gevonden.");
        } else {
            System.out.println("Aantal laadprocessen: " + processen.size());
            System.out.println("id, bestand_naam, bestand_datum, soort, status, contact");

            processen.stream().forEach((lp) -> {
                System.out.printf("%s,%s,%s,%s,%s,%s\n",
                        lp.getId(),
                        lp.getBestandNaam(),
                        lp.getBestandDatum(),
                        lp.getSoort(),
                        lp.getStatus(),
                        lp.getContactEmail());
            });
        }
        brmo.closeBrmoFramework();
        return 0;
    }

    private static int berichtStatus(DataSource ds, String format) throws BrmoException {
        LOG.info("Ophalen bericht status informatie.");
        BrmoFramework brmo = new BrmoFramework(ds, null);
        long staging_ok = brmo.getCountBerichten(null, null, null, Bericht.STATUS.STAGING_OK.name());
        long staging_nok = brmo.getCountBerichten(null, null, null, Bericht.STATUS.STAGING_NOK.name());
        long rsgb_ok = brmo.getCountBerichten(null, null, null, Bericht.STATUS.RSGB_OK.name());
        long rsgb_nok = brmo.getCountBerichten(null, null, null, Bericht.STATUS.RSGB_NOK.name());
        long rsgb_bag_nok = brmo.getCountBerichten(null, null, null, Bericht.STATUS.RSGB_BAG_NOK.name());
        long rsgb_outdated = brmo.getCountBerichten(null, null, null, Bericht.STATUS.RSGB_OUTDATED.name());
        long archive = brmo.getCountBerichten(null, null, null, Bericht.STATUS.ARCHIVE.name());

        if (format.equalsIgnoreCase("json")) {
            System.out.printf(
                    "{\"status\":[{\"STAGING_OK\":%s},{\"STAGING_NOK\":%s},{\"RSGB_OK\":%s},{\"RSGB_NOK\":%s},{\"RSGB_BAG_NOK\":%s},{\"RSGB_OUTDATED\":%s},{\"ARCHIVE\":%s}]}\n",
                    staging_ok, staging_nok, rsgb_ok, rsgb_nok, rsgb_bag_nok, rsgb_outdated, archive
            );
        } else {
            System.out.println("status, aantal");
            System.out.printf("STAGING_OK,%s\n", staging_ok);
            System.out.printf("STAGING_NOK,%s\n", staging_nok);
            System.out.printf("RSGB_OK,%s\n", rsgb_ok);
            System.out.printf("RSGB_NOK,%s\n", rsgb_nok);
            System.out.printf("RSGB_BAG_NOK,%s\n", rsgb_bag_nok);
            System.out.printf("RSGB_OUTDATED,%s\n", rsgb_outdated);
            System.out.printf("ARCHIVE,%s\n", archive);
        }

        brmo.closeBrmoFramework();
        return 0;
    }

    private static int jobStatus(DataSource ds, String format) throws BrmoException {
        LOG.info("Ophalen staging job informatie.");
        BrmoFramework brmo = new BrmoFramework(ds, null);
        long count = brmo.getCountJob();

        if (format.equalsIgnoreCase("json")) {
            System.out.printf("{\"jobs\":%s}\n", count);
        } else {
            System.out.println("aantal");
            System.out.println(count);
        }
        brmo.closeBrmoFramework();
        return 0;
    }

    private static int delete(DataSource ds, String id) throws BrmoException {
        LOG.info("Verwijderen laadproces " + id + " met aanhangende berichten uit staging.");
        long laadProcesId = 0;
        if (id != null && !id.isEmpty()) {
            laadProcesId = new Long(id);
        }

        BrmoFramework brmo = new BrmoFramework(ds, null);
        brmo.delete(laadProcesId);
        brmo.closeBrmoFramework();
        return 0;
    }

    private static int getMutations(DataSource ds, String... opts) {
        LOG.info("Start export GDS2 berichten naar " + opts[0]);
        Connection con = null;
        String dir = opts[0];
        int exitcode = 0;
        try {
            LOG.info("Ophalen automatisch proces(sen) waarmee GDS2 berichten zijn geladen.");
            con = ds.getConnection();
            Statement stmt = con.createStatement();
            String autoProcessen = "select id FROM automatisch_proces where dtype = 'GDS2OphaalProces'";
            ResultSet rs = stmt.executeQuery(autoProcessen);
            while (rs.next()) {
                Long id = rs.getLong(1);
                LOG.info("Ophalen laadprocessen voor automatisch proces: " + id);
                String processen = "select id,bestand_naam from laadproces where automatisch_proces = " + id;
                Statement laadprocesStmt = con.createStatement();
                ResultSet lpRs = laadprocesStmt.executeQuery(processen);
                while (lpRs.next()) {
                    Long lpId = lpRs.getLong(1);
                    String bestandsNaam = lpRs.getString(2);
                    String berichten = "select id, br_orgineel_xml, object_ref from bericht where laadprocesid = " + lpId;
                    Statement berichtStmt = con.createStatement();
                    ResultSet berRs = berichtStmt.executeQuery(berichten);
                    while (berRs.next()) {
                        LOG.info("Bericht " + id + " - " + bestandsNaam);
                        String xml = berRs.getString("br_orgineel_xml");
                        writeXML(xml, bestandsNaam, dir);
                    }
                }
            }
        } catch (SQLException ex) {
            LOG.error("Fout bij ophalen berichten/laadprocessen/automatische processen.", ex);
            exitcode = EX_UNAVAILABLE.code;
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ex) {
                    LOG.warn("Database verbinding sluiten is mislukt.", ex);
                }
            }
        }
        return exitcode;
    }

    private static void writeXML(String xml, String filename, String directory) {
        ZipOutputStream out = null;
        try {
            final File f = new File(directory + "/" + filename + ".zip");
            out = new ZipOutputStream(new FileOutputStream(f));
            ZipEntry e = new ZipEntry(filename);
            out.putNextEntry(e);
            byte[] data = xml.getBytes();
            out.write(data, 0, data.length);
            out.closeEntry();
            out.close();
        } catch (IOException ex) {
            LOG.error("Schrijven van bestand " + filename + " in " + directory + " is mislukt.", ex);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ex) {
                    LOG.warn("Sluiten .zip file is mislukt.", ex);
                }
            }
        }
    }

    /**
     * verwerk xml of zip bestand naar staging db.
     *
     * @param ds datasource
     * @param opts array met {vollidig pad naar bestand, type BR, optionele
     * archief directory}
     * @return {@code 0} als succesvol
     * @throws BrmoException in geval van niet laadfout
     */
    private static int load(DataSource ds, String... opts) throws BrmoException {
        String fileName = opts[0];
        String brType = opts[1];
        String archiefDir = opts.length == 3 ? opts[2] : null;

        LOG.debug(String.format("Begin laden van bestand: %s, type %s", fileName, brType));
        BrmoFramework brmo = new BrmoFramework(ds, null);
        brmo.setOrderBerichten(true);
        brmo.setErrorState("ignore");
        brmo.loadFromFile(brType, fileName);
        brmo.closeBrmoFramework();
        LOG.info(String.format("Klaar met laden van bestand: %s, type %s", fileName, brType));
        archiveerBestand(fileName, archiefDir);
        return 0;
    }

    /**
     * verwerk xml en zip betsanden uit directory naar staging db.
     *
     * @param ds datasource
     * @param opts array met {directory, type BR, optionele archief directory}
     * @return {@code 0} als succesvol,{@code EX_DATAERR} in geval van een
     * waarschuwing
     * @throws BrmoException in geval van niet herstalebare laadfout
     */
    private static int loaddir(DataSource ds, String... opts) throws BrmoException {
        String scanDir = opts[0];
        String brType = opts[1];
        String archiefDir = opts.length == 3 ? opts[2] : null;

        int exitcode = 0;
        if (!scanDir.endsWith(File.separator)) {
            scanDir += File.separator;
        }
        LOG.info(String.format("Begin laden van directory: %s, type %s", scanDir, brType));

        File dir = new File(scanDir);
        if (dir.isDirectory()) {
            boolean withWarnings = false;
            String[] fNames = dir.list((File f, String name) -> {
                return (name.endsWith(".xml") || name.endsWith(".XML") || name.endsWith(".zip") || name.endsWith(".ZIP"));
            });
            BrmoFramework brmo = new BrmoFramework(ds, null);
            brmo.setOrderBerichten(true);
            brmo.setErrorState("ignore");

            for (String fName : fNames) {
                try {
                    LOG.debug(String.format("Begin laden van bestand: %s, type %s", fName, brType));
                    brmo.loadFromFile(brType, scanDir + fName);
                    LOG.info(String.format("Klaar met laden van bestand: %s, type %s", fName, brType));
                } catch (BrmoDuplicaatLaadprocesException dup) {
                    LOG.warn(String.format("Laden duplicaat bestand %s overgeslagen. Oorzaak: %s", fName, dup.getLocalizedMessage()));
                    withWarnings = true;
                } catch (BrmoLeegBestandException leeg) {
                    LOG.warn(String.format("Laden 'leeg' bestand %s overgeslagen. Oorzaak: %s", fName, leeg.getLocalizedMessage()));
                    withWarnings = true;
                } finally {
                    brmo.closeBrmoFramework();
                }
                archiveerBestand(scanDir + fName, archiefDir);
            }
            if (withWarnings) {
                exitcode = EX_DATAERR.code;
            }
        } else {
            throw new BrmoException("Opgegeven directory " + scanDir + " is geen directory.");
        }
        LOG.info(String.format("Klaar met laden van directory: %s, type %s", scanDir, brType));
        return exitcode;
    }

    /**
     * verplaats bestand naar archief directory.
     *
     * @param fileName te verplaatsen bestandsnaam (volledig pad)
     * @param archiefDir doel directory (in geval {@code null} dat wordt
     * verplaatsen overgeslagen)
     */
    private static void archiveerBestand(final String fileName, final String archiefDir) {
        if (archiefDir != null) {
            File archiefDirectory = new File(archiefDir);
            LOG.debug(String.format("Archiveren %s naar %s.", fileName, archiefDir));
            try {
                FileUtils.moveFileToDirectory(new File(fileName), archiefDirectory, true);
                LOG.debug(String.format("Bestand %s is naar archief %s verplaatst.", fileName, archiefDirectory));
            } catch (IOException e) {
                LOG.error(String.format("Bestand %s is NIET naar archief %s verplaatst, oorzaak: (%s).", fileName, archiefDirectory, e.getLocalizedMessage()));
            }
        }
    }

    static enum sysexits {
        // zie: https://www.freebsd.org/cgi/man.cgi?query=sysexits&apropos=0&sektion=3&manpath=OpenBSD+5.8&arch=default&format=html
        EX_USAGE(64), EX_DATAERR(65), EX_NOINPUT(66), EX_UNAVAILABLE(69), EX_CANTCREAT(73), EX_IOERR(74);
        int code;

        private sysexits(final int code) {
            this.code = code;
        }
    }

}
