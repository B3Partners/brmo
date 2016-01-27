package nl.b3p.brmo.tool;

import java.io.File;
import java.io.FileNotFoundException;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.sql.DataSource;
import nl.b3p.brmo.loader.BrmoFramework;
import nl.b3p.brmo.loader.entity.LaadProces;
import nl.b3p.brmo.loader.util.BrmoException;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.dbcp.BasicDataSource;

/**
 *
 * @author Boy de Wit
 */
public class Main {

    private static final String BAG_BR_TYPE = "bag";
    private static final String BRK_BR_TYPE = "brk";

    private static final String xslPathBrk = "/xsl/brk-snapshot-to-rsgb-xml.xsl";
    private static final String xslPathBag = "/xsl/bag-mutatie-to-rsgb-xml.xsl";

    private static List<Option> stagingDbOpts;
    private static List<Option> modeOpts;
    private static List<Option> outDbOpts;

    private static Options stagingOptions, modeOptions, outDbOptions;

    private static Options buildOptions() {

        stagingDbOpts = Arrays.asList(new Option[]{
            OptionBuilder.withDescription("JDBC url Staging database, bv. 'jdbc:postgresql:staging' of 'jdbc:oracle:thin:@hostname:1521/sid'")
            .withArgName("url")
            .hasArg(true)
            .isRequired()
            .create("stagingurl"),
            OptionBuilder.withDescription("JDBC property Staging database, bijv. user=<user> en password=<password> (herhaal optie, inclusief stagingprop, voor meerdere properties)")
            .withArgName("property=value")
            .hasArgs()
            .isRequired()
            .withValueSeparator('=')
            .create("stagingprop")
        });

        modeOpts = Arrays.asList(new Option[]{
            OptionBuilder.withDescription("Laad totaalstand of mutatie uit bestand (.zip of .xml) in database")
            .withArgName("bestand type")
            .hasArgs(2)
            .create("load"),
            OptionBuilder.withDescription("Geef overzicht van laadprocessen in database")
            .create("list"),
            OptionBuilder.withDescription("Verwijder laadprocessen in database (geef id weer met -list)")
            .withArgName("id")
            .hasArg(true)
            .withType(Integer.class) // XXX werkt niet
            .create("delete"),
            OptionBuilder.withDescription("Inladen alle klaargezette berichten naar de rsgb.")
            .create("torsgb"),
            OptionBuilder.withDescription("Maak van berichten uit staging gezipte xml-files in de opgegeven directory. Dit zijn BRK mutaties van GDS2 processen")
            .withArgName("output directory")
            .hasArgs(1)
            .create("getmutations"),
        });

        outDbOpts = Arrays.asList(new Option[]{
            OptionBuilder.withDescription("JDBC url output-database, bv. 'jdbc:postgresql:rsgb' of 'jdbc:oracle:thin:@hostname:1521/sid'")
            .withArgName("url")
            .hasArg(true)
            .create("outdburl"),
            OptionBuilder.withDescription("JDBC property output-database, bijv. user=<user> en password=<password> (herhaal optie, inclusief outdbprop, voor meerdere properties)")
            .withArgName("property=value")
            .hasArgs()
            .withValueSeparator('=')
            .create("outdbprop"),
            OptionBuilder.withDescription("Database schema output-database, mogelijk nodig indien meerdere schema's met uitvoer tabellen bestaan en het user account rechten heeft op beide schema's zodat bij het ophalen van database metadata mogelijk niet in het goede schema wordt gekeken (Oracle)")
            .withArgName("schema")
            .hasArg()
            .create("outdbschema")
        });

        Options options = new Options();
        stagingOptions = new Options();
        for (Option o : stagingDbOpts) {
            options.addOption(o);
            stagingOptions.addOption(o);
        }
        outDbOptions = new Options();
        for (Option o : outDbOpts) {
            options.addOption(o);
            outDbOptions.addOption(o);
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
                List[] lists = new List[]{stagingDbOpts, modeOpts, outDbOpts};
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
        formatter.printUsage(pw, 80, "java -jar brmo-loader.jar <mode> <staging-db-opties> <out-db-opties>");
        formatter.printUsage(pw, 80, "java -cp \"brmo-loader.jar;databaseDriver.jar;lib/*\" nl.b3p.brmo.tool.Main <mode> <staging-db-opties> <out-db-opties>");
        pw.print("\nModes:\n");
        formatter.printOptions(pw, W, modeOptions, 2, 2);
        pw.print("\nStaging database gegevens:\n");
        formatter.printOptions(pw, W, stagingOptions, 2, 2);
        pw.print("\nUitvoer database gegevens:\n");
        formatter.printOptions(pw, W, outDbOptions, 2, 2);
        System.out.println(sw.toString());
    }

    public static void main(String[] args) {

        // -stagingurl jdbc:postgresql://kx1/staging -stagingprop user=staging
        // -stagingprop password=staging -outdburl jdbc:postgresql://kx1/rsgb
        // -outdbprop user=rsgb -outdbprop password=rsgb -standtodb 1
        Options options = buildOptions();
        CommandLine cl = null;
        try {
            CommandLineParser parser = new PosixParser();

            cl = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.printf("%s: %s\n\n", e.getClass().getSimpleName(), e.getMessage());
            printHelp();
            System.exit(1);
        }

        int code = 0;
        try {
            System.out.print("Maken verbinding met Staging database... ");
            if (cl.getOptionValue("stagingurl").contains(":oracle:")) {
                Class.forName("oracle.jdbc.OracleDriver");
            }
            
            BasicDataSource ds = new BasicDataSource();
            Properties stagingprops = cl.getOptionProperties("stagingprop");
            ds.setUrl(cl.getOptionValue("stagingurl"));
            ds.setUsername(stagingprops.getProperty("user"));
            ds.setPassword(stagingprops.getProperty("password"));
            ds.setConnectionProperties(cl.getOptionProperties("stagingprop").toString());

            if (cl.hasOption("list")) {
                code = list(ds);
            } else if (cl.hasOption("load")) {
                code = load(ds, cl.getOptionValues("load"));
            } else if (cl.hasOption("delete")) {
                code = delete(ds, cl.getOptionValue("delete"));
            } else if (cl.hasOption("torsgb")) {
                String outDbUrl = null;

                BasicDataSource dsOut = null;
                if (cl.hasOption("outdburl")) {
                    System.out.print("Maken verbinding met RSGB database... ");
                    outDbUrl = cl.getOptionValue("outdburl");
                    if (outDbUrl.contains(":oracle:")) {
                        Class.forName("oracle.jdbc.OracleDriver");
                    }
                    dsOut = new BasicDataSource();
                    Properties outdbProps = cl.getOptionProperties("outdbprop");
                    dsOut.setUrl(cl.getOptionValue("outdburl"));
                    dsOut.setUsername(outdbProps.getProperty("user"));
                    dsOut.setPassword(outdbProps.getProperty("password"));
                    dsOut.setConnectionProperties(cl.getOptionProperties("outdbprop").toString());
        
                    System.out.println("OK");
                }

                code = toRsgb(ds, dsOut);
            }else if(cl.hasOption("getmutations")){
                code = getMutations(ds,cl.getOptionValues("getmutations"));
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
            code = 1;
        }

        System.exit(code);
    }

    private static int toRsgb(DataSource dataSourceStaging, DataSource dataSourceRsgb) throws BrmoException {
        BrmoFramework brmo = new BrmoFramework(dataSourceStaging, dataSourceRsgb);
        brmo.toRsgb();
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

    private static int list(DataSource ds) throws BrmoException {
        BrmoFramework brmo = new BrmoFramework(ds, null);
        List<LaadProces> processen = brmo.listLaadProcessen();

        if (processen.isEmpty()) {
            System.err.println("Geen processen gevonden.");
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

    private static int getMutations(DataSource ds, String[] opts){
        Connection con = null;
        String dir = opts[0];
        try {
            con = ds.getConnection();

            Statement stmt = con.createStatement();
            String autoProcessen = "select id FROM automatisch_proces where dtype = 'GDS2OphaalProces'";
            ResultSet rs = stmt.executeQuery(autoProcessen);
            while(rs.next()){
                Long id = rs.getLong(1);
                String processen = "select id,bestand_naam from laadproces where automatisch_proces = " + id;
                Statement laadprocesStmt = con.createStatement();
                ResultSet lpRs = laadprocesStmt.executeQuery(processen);
                while(lpRs.next()){
                    Long lpId = lpRs.getLong(1);
                    String bestandsNaam = lpRs.getString(2);
                    String berichten = "select id, br_orgineel_xml, object_ref from bericht where laadprocesid = " + lpId;
                    Statement berichtStmt = con.createStatement();
                    ResultSet berRs = berichtStmt.executeQuery(berichten);
                    while(berRs.next()){
                        System.out.println("Bericht " + id + " - " + bestandsNaam);
                        String xml = berRs.getString("br_orgineel_xml");
                        writeXML(xml,bestandsNaam,dir);
                    }
                }
            }
        } catch (Exception ex) {
            System.err.println("Fout bij ophalen berichten/laadprocessen/automagische processen: " +ex.getLocalizedMessage() );
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException ex) {
                    System.err.println("Kan verbinding niet sluiten.");
                }
            }
        }
        return 0;
    }

    private static void writeXML(String xml, String filename, String directory){
        ZipOutputStream out = null;
        try {
            final File f = new File(directory + "/" + filename +".zip");
            out = new ZipOutputStream(new FileOutputStream(f));
            ZipEntry e = new ZipEntry(filename);
            out.putNextEntry(e);
            byte[] data = xml.getBytes();
            out.write(data, 0, data.length);
            out.closeEntry();
            out.close();
        } catch (FileNotFoundException ex) {
            System.err.println("File not found" + ex.getLocalizedMessage());
        } catch (IOException ex) {
            System.err.println("IOException " + ex.getLocalizedMessage());
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    private static int load(DataSource ds, String[] opts) throws BrmoException {
        String fileName = opts[0];
        String brType = opts[1];

        BrmoFramework brmo = new BrmoFramework(ds, null);
        brmo.loadFromFile(brType, fileName);
        brmo.closeBrmoFramework();

        return 0;
    }
}
