/*
 * Copyright (C) 2017 B3Partners B.V.
 */
package nl.b3p.brmo.tool;

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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.sql.DataSource;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.RsgbProxy;
import nl.b3p.brmo.loader.entity.Bericht;
import nl.b3p.brmo.loader.entity.LaadProces;
import nl.b3p.brmo.loader.util.BrmoException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
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
            Option.builder().desc("database properties file").type(File.class).longOpt("dbprops").argName("bestand").hasArg().required().numberOfArgs(1).build()
        });

        modeOpts = Arrays.asList(new Option[]{
            Option.builder().desc("versie informatie van de verschillende schema's").longOpt("versieinfo").optionalArg(true).argName("format").build(),
            OptionBuilder.withDescription("Laad totaalstand of mutatie uit bestand (.zip of .xml) in database")
            .withArgName("bestand type")
            .hasArgs(2)
            .create("load"),
            OptionBuilder.withDescription("Geef overzicht van laadprocessen in database")
            .create("list"),
            OptionBuilder.withDescription("Verwijder laadprocessen in database (geef id weer met -list)")
            .withArgName("id")
            .hasArg(true)
            .withType(Integer.class)
            .create("delete"),
            OptionBuilder.withDescription("Transformeer alle 'STAGING_OK' berichten naar de rsgb.")
            .create("torsgb"),
            OptionBuilder.withDescription("Transformeer alle 'STAGING_OK' BGT light laadprocessen naar de rsgbbgt.")
            .create("torsgbbgt"),
            OptionBuilder.withDescription("Maak van berichten uit staging gezipte xml-files in de opgegeven directory. Dit zijn alleen BRK mutaties van GDS2 processen.")
            .withArgName("output directory")
            .hasArgs(1)
            .create("getmutations"),});

        Options options = new Options();

        dbOptions = new Options();
        for (Option o : dbOpts) {
            options.addOption(o);
            dbOptions.addOption(o);
        }

        OptionGroup g = new OptionGroup();
        g.setRequired(true);
        modeOptions = new Options();
        for (Option o : modeOpts) {
            g.addOption(o);
            modeOptions.addOption(o);
        }
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
        formatter.printUsage(pw, 80, "java -jar brmo-loader.jar <mode> <db-opties>");
        // formatter.printUsage(pw, 80, "java -cp \"brmo-loader.jar;databaseDriver.jar;lib/*\" nl.b3p.brmo.tool.Main <mode> <db-opties>");
        pw.print("\nActies:\n");
        formatter.printOptions(pw, W, modeOptions, 2, 2);
        pw.print("\nDatabase:\n");
        formatter.printOptions(pw, W, dbOptions, 2, 2);

        System.err.println(sw.toString());
    }

    public static void main(String[] args) {
        Options options = buildOptions();
        CommandLine cl = null;
        try {
            CommandLineParser parser = new DefaultParser();
            cl = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.printf("%s: %s\n\n", e.getClass().getSimpleName(), e.getMessage());
            printHelp();
            System.exit(1);
        }

        int exitcode = 0;
        try {
            BasicDataSource dsStaging = new BasicDataSource();
            BasicDataSource dsRsgb = null;
            BasicDataSource dsRsgbbgt = null;

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
                    throw new IllegalArgumentException("het database type wordt niet ondersteund of is niet opgegeven");
            }

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
                exitcode = list(dsStaging);
            } else if (cl.hasOption("load")) {
                exitcode = load(dsStaging, cl.getOptionValues("load"));
            } else if (cl.hasOption("delete")) {
                exitcode = delete(dsStaging, cl.getOptionValue("delete"));
            } else if (cl.hasOption("getmutations")) {
                exitcode = getMutations(dsStaging, cl.getOptionValues("getmutations"));
            } // ----------------
            // rsgb commando's
            else if (cl.hasOption("torsgb")) {
                exitcode = toRsgb(dsStaging, dsRsgb);
            } // ----------------
            // rsgbbgt commando's
            else if (cl.hasOption("torsgbbgt")) {
                exitcode = toRsgbBgt(dsStaging, dsRsgb, dsRsgbbgt);

            } // ----------------
            // alle schema's / databases
            else if (cl.hasOption("versieinfo")) {
                exitcode = versieInfo(dsStaging, dsRsgb, dsRsgbbgt, cl.getOptionValue("versieinfo"));
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
            exitcode = 1;
        }

        System.exit(exitcode);
    }

    private static int toRsgb(DataSource dataSourceStaging, DataSource dataSourceRsgb) throws BrmoException, InterruptedException {
        BrmoFramework brmo = new BrmoFramework(dataSourceStaging, dataSourceRsgb);
        brmo.setOrderBerichten(true);
        brmo.setErrorState("ignore");
        Thread t = brmo.toRsgb();
        t.join();
        brmo.closeBrmoFramework();
        return 0;
    }

    private static int toRsgbBgt(BasicDataSource dsStaging, BasicDataSource dsRsgb, BasicDataSource dsRsgbbgt) throws BrmoException, InterruptedException {
        BrmoFramework brmo = new BrmoFramework(dsStaging, dsRsgb, dsRsgbbgt);
        // bepaal welke bgtlight LP's er zijn om te transformeren
        final long[] lpIds = ArrayUtils.toPrimitive(brmo.getLaadProcessenIds("bestand_datum", "ASC", BrmoFramework.BR_BGTLIGHT, Bericht.STATUS.STAGING_OK.name()));
        brmo.setOrderBerichten(true);
        brmo.setErrorState("ignore");
        Thread t = brmo.toRsgb(RsgbProxy.BerichtSelectMode.BY_LAADPROCES, lpIds, null);
        t.join();

        brmo.closeBrmoFramework();
        return 0;
    }

    private static int versieInfo(DataSource dataSourceStaging, DataSource dataSourceRsgb, DataSource dataSourceRsgbBGT, String format) throws BrmoException {
        BrmoFramework brmo = new BrmoFramework(dataSourceStaging, dataSourceRsgb, dataSourceRsgbBGT);
        if (format.equalsIgnoreCase("json")) {
            // TODO json output
            System.out.println("TODO json output versieInfo");
        } else {
            System.out.println("staging versie: " + brmo.getStagingVersion());
            System.out.println("rsgb versie:    " + brmo.getRsgbVersion());
            System.out.println("rsgbbgt versie: " + brmo.getRsgbBgtVersion());
        }
        brmo.closeBrmoFramework();
        return 0;
    }

    private static int delete(DataSource ds, String id) throws BrmoException {
        long laadProcesId = 0;
        if (id != null && !id.isEmpty()) {
            laadProcesId = new Long(id);
        }

        BrmoFramework brmo = new BrmoFramework(ds, null);
        brmo.delete(laadProcesId);
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
    private static int list(DataSource ds) throws BrmoException {
        // TODO json support toevoegen?
        BrmoFramework brmo = new BrmoFramework(ds, null);
        List<LaadProces> processen = brmo.listLaadProcessen();

        if (processen.isEmpty()) {
            System.out.println("Geen processen gevonden.");
        } else {
            System.out.println("Aantal processen: " + processen.size());
            System.out.println("id, bestand_naam, bestand_datum, soort, status, contact");

            for (LaadProces lp : processen) {
                System.out.printf("%s,%s,%s,%s,%s,%s\n",
                        lp.getId(),
                        lp.getBestandNaam(),
                        lp.getBestandDatum(),
                        lp.getSoort(),
                        lp.getStatus(),
                        lp.getContactEmail());
            }
        }
        brmo.closeBrmoFramework();
        return 0;
    }

    private static int getMutations(DataSource ds, String[] opts) {
        Connection con = null;
        String dir = opts[0];
        int code = 0;
        try {
            con = ds.getConnection();
            Statement stmt = con.createStatement();
            String autoProcessen = "select id FROM automatisch_proces where dtype = 'GDS2OphaalProces'";
            ResultSet rs = stmt.executeQuery(autoProcessen);
            while (rs.next()) {
                Long id = rs.getLong(1);
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
        } catch (Exception ex) {
            LOG.error("Fout bij ophalen berichten/laadprocessen/automatische processen.", ex);
            code = 1;
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ex) {
                    LOG.error("Database verbinding sluiten is mislukt.", ex);
                }
            }
        }
        return code;
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
            try {
                out.close();
            } catch (IOException ex) {
                LOG.error(ex);
            }
        }
    }

    private static int load(DataSource ds, String[] opts) throws BrmoException {
        String fileName = opts[0];
        String brType = opts[1];
        BrmoFramework brmo = new BrmoFramework(ds, null);
        brmo.setOrderBerichten(true);
        brmo.setErrorState("ignore");
        brmo.loadFromFile(brType, fileName);
        brmo.closeBrmoFramework();
        return 0;
    }

}
