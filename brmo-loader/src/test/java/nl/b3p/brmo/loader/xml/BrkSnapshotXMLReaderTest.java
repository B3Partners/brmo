/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.b3p.brmo.loader.xml;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import nl.b3p.brmo.loader.entity.BrkBericht;
import org.apache.commons.io.input.CloseShieldInputStream;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeNotNull;
import org.junit.Test;

/**
 * Testcases voor {@link nl.b3p.brmo.loader.xml.BrkSnapshotXMLReader}.
 *
 * @author mprins
 */
public class BrkSnapshotXMLReaderTest {

    private static final String mutSmallXml = "MUTBX01-ASN00T2939A12-20091130-1.xml";
    private static final int mutSmallXmlNieuwCount = 1;

    private static final String mutXML = "MUTBX01-ASN00T1660-20091119-1-singleline.xml";
    private static final String mutXMLReformatted = "MUTBX01-ASN00T1660-20091119-1-prettyprinted.xml";
    private static final int mutXmlNieuwCount = 1;

    private static final String mutZipName = "MUTBX01-ASN00T2939A12-20091130-1.zip";
    private static final int mutZipNameNieuwCount = 1;
    private static final String[] mutXmlsInZip = {"MUTBX01.xml"};

    // dit bestand zit in de DVD Proefbestanden BRK Levering oktober 2012 (Totaalstanden)
    // /mnt/v_b3p_projecten/BRMO/BRK/BRK_STUF_IMKAD/BRK/Levering(dvd)/Proefbestanden BRK Levering oktober 2012 (Totaalstanden)/20091130/
    // en staat op de ignore lijst omdat 't 18.5MB groot is
    private static final String standZipName = "BURBX01-ASN00-20091130-6000015280-9100000039.zip";
    // grep -o KadastraalObjectSnapshot BURBX01.xml | wc -w
    private static final int standZipSnapshotCount = 63104 / 2;
    private static final String[] standXmlsInZip = {"BURBX01.xml"};

    /**
     * Test next() methode met klein mutatie bestand.
     *
     * @throws Exception if any
     */
    @Test
    public void mutTestSmallXML() throws Exception {
        BrkSnapshotXMLReader bReader;
        BrkBericht brk = null;
        
        bReader = new BrkSnapshotXMLReader(BrkSnapshotXMLReader.class.getResourceAsStream(mutSmallXml));
        assertTrue(bReader.hasNext());
        int total = 0;
        while (bReader.hasNext()) {
            brk = bReader.next();
            System.out.println(
                    "datum: " + brk.getDatum()
                    + "\tobj ref: " + brk.getObjectRef()
                    + "\tvolgordenummer: " + brk.getVolgordeNummer()
            );
            total++;
        }
        assertEquals(mutSmallXmlNieuwCount, total);
        assertEquals("NL.KAD.OnroerendeZaak:53860293910012", brk.getObjectRef());
        assertEquals(new SimpleDateFormat("yyyy-MM-dd").parse("2009-11-30"),
                brk.getDatum());
        assertEquals(new Integer(1), brk.getVolgordeNummer());
    }

    /**
     * Test next() methode met groter mutatie bestand en vergelijk parsen van
     * file met en zonder extra regeleinden.
     *
     * @throws Exception if any
     */
    @Test
    public void mutTestLargeXML() throws Exception {
        BrkSnapshotXMLReader bReader;
        BrkBericht brk = null;
        BrkBericht brkReformatted = null;

        int total = 0;
        bReader = new BrkSnapshotXMLReader(BrkSnapshotXMLReader.class.getResourceAsStream(mutXML));
        assertTrue(bReader.hasNext());
        while (bReader.hasNext()) {
            brk = bReader.next();
            total++;
        }
        assertEquals(mutXmlNieuwCount, total);

        
        bReader = new BrkSnapshotXMLReader(BrkSnapshotXMLReader.class.getResourceAsStream(mutXMLReformatted));
        assertTrue(bReader.hasNext());
        total = 0;
        while (bReader.hasNext()) {
            brkReformatted = bReader.next();
            total++;
        }
        assertEquals(mutXmlNieuwCount, total);

        // vergelijk resultaat van de twee readers
        assertEquals("van beide laatste mutaties moet ObjectRef hetzelfde zijn",
                brk.getObjectRef(), brkReformatted.getObjectRef());
        assertEquals("van beide laatste mutaties moet datum van laatste hetzelfde zijn",
                brk.getDatum(), brkReformatted.getDatum());
        assertEquals("van beide laatste mutaties moet VolgordeNummer hetzelfde zijn",
                brk.getVolgordeNummer(), brkReformatted.getVolgordeNummer());
    }
    /**
     * Test next() methode met xml bestanden in een zip met mutaties.
     *
     * @throws Exception if any
     */
    @Test
    public void mutTestZipFile() throws Exception {
        ZipInputStream zis = new ZipInputStream(
                BrkSnapshotXMLReader.class.getResourceAsStream(mutZipName));
        int total = 0;
        try {
            ZipEntry entry = zis.getNextEntry();
            while (entry != null) {
                BrkSnapshotXMLReader brkreader = new BrkSnapshotXMLReader(new CloseShieldInputStream(zis));

                assertEquals("De bestandsnaam moet kloppen.", mutXmlsInZip[0], entry.getName());
                while (brkreader.hasNext()) {
                    BrkBericht brk = brkreader.next();
                    total++;
                }
                entry = zis.getNextEntry();
            }
        } finally {
            zis.close();
        }
        assertEquals("Verwacht dat het aantal mutaties gelijk is.", mutZipNameNieuwCount, total);
    }

    /**
     * Test next() methode met xml bestanden in een zip met mutaties.
     *
     * @throws Exception if any
     */
    @Test
    public void standTestZipFile() throws Exception {
        InputStream zipIn = BrkSnapshotXMLReader.class.getResourceAsStream(standZipName);
        assumeNotNull("Neem aan dat de 18.5MB zipfile er staat.", zipIn);

        ZipInputStream zis = new ZipInputStream(zipIn);
        int total = 0;
        try {
            ZipEntry entry = zis.getNextEntry();
            while (entry != null) {
                BrkSnapshotXMLReader brkreader = new BrkSnapshotXMLReader(new CloseShieldInputStream(zis));

                assertEquals("De bestandsnaam moet kloppen.", standXmlsInZip[0], entry.getName());
                while (brkreader.hasNext()) {
                    BrkBericht brk = brkreader.next();
                    total++;
                }
                entry = zis.getNextEntry();
            }
        } finally {
            zis.close();
        }
        assertEquals("Verwacht dat het aantal mutaties gelijk is.", standZipSnapshotCount, total);
    }
//    /**
//     * Test next() methode met leeg levering bestand.
//     *
//     * @throws Exception if any
//     */
//    @Test
//    public void testLvcEmptyXML() throws Exception {
//        BrkSnapshotXMLReader bReader;
//        bReader = new BrkSnapshotXMLReader(BrkSnapshotXMLReader.class.getResourceAsStream(lvcEmpty));
//        assertTrue(!bReader.hasNext());
//    }
}
