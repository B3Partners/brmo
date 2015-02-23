/*
 * Copyright (C) 2015 B3Partners B.V.
 */
package nl.b3p.brmo.digikoppeling;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import static nl.b3p.brmo.digikoppeling.AfleverRequestHandling.STATUSCODE.ERROR;
import static nl.b3p.brmo.digikoppeling.AfleverRequestHandling.STATUSCODE.SUCCESS;
import org.apache.commons.io.FileUtils;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import org.custommonkey.xmlunit.jaxp13.Validator;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.exceptions.ConfigurationException;
import static org.hamcrest.core.Is.is;
import org.junit.After;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assume.assumeNotNull;
import org.junit.BeforeClass;

/**
 * Testcase voor {@link nl.b3p.brmo.digikoppeling.EbMSBerichtenProcessor}.
 *
 * @author Mark Prins <mark@b3partners.nl>
 */
public class EbMSBerichtenProcessorTest {

    private EbMSBerichtenProcessor instance;
    private Validator v;

    private static final String storeDirectory = "target/berichten/afleveren";

    @BeforeClass
    public static void initXmlUnit() {
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setIgnoreAttributeOrder(true);
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
    }

    @Before
    public void beforeTest() {
        instance = new EbMSBerichtenProcessor(storeDirectory);

        this.v = new Validator();
        final Source schema = new StreamSource(new File(
                "target/classes/META-INF/jaxb/xsd/digipoort-koppelvlak-1.2.xsd"));
        this.v.addSchemaSource(schema);
    }

    @After
    public void afterTest() {
        instance = null;
        v = null;
    }

    /**
     * XML testcase voor {@code digipoort-koppelvlak-1.2.xsd}, valideert de xsd.
     */
    @Test
    public void testDigipoortXSD() {
        try {
            assertThat("het schema bestand geldig is ", this.v.isSchemaValid(), is(true));
        } catch (final ConfigurationException e) {
            fail(e.getMessage());
        } catch (final Exception e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test of processAfleverRequest method, of class EbMSBerichtenProcessor.
     *
     * @throws java.lang.Exception if any
     */
    @Test
    public void testProcessAfleverRequest() throws Exception {
        InputStream input = this.getClass().getClassLoader()
                .getResourceAsStream("berichten/afleveren/BAGAfleverRequest_ebMS_2.0_v1.2.xml");

        File f = new File(storeDirectory, "BAGAfleverResponse_ebMS_2.0_v1.2.xml");
        OutputStream output = new FileOutputStream(f);

        assumeNotNull(input);
        assumeNotNull(output);

        instance.processAfleverRequest(input, output);

        final Source doc = new StreamSource(f);

        assertNotNull("Het bestand 'BAGAfleverResponse_ebMS_2.0_v1.2.xml' moet bestaan.", doc);
        assertThat("Het response bestand geldig is volgens het gebruikte digipoort schema.",
                this.v.isInstanceValid(doc), is(true));

        assertXpathEvaluatesTo(
                SUCCESS.toString(),
                "//*[local-name()='afleverResponse'][1]/*[local-name()='statuscode']",
                FileUtils.readFileToString(f, "UTF-8"));
    }

    /**
     * Test processAfleverRequest methode met een fout bericht.
     *
     * @throws java.lang.Exception if any
     */
    @Test
    public void testProcessAfleverRequestFout() throws Exception {
        InputStream input = this.getClass().getClassLoader()
                .getResourceAsStream("berichten/afleveren/foutBAGAfleverRequest_ebMS_2.0_v1.2.xml");

        File f = new File(storeDirectory, "foutBAGAfleverResponse_ebMS_2.0_v1.2.xml");
        OutputStream output = new FileOutputStream(f);

        assumeNotNull(input);
        assumeNotNull(output);

        instance.processAfleverRequest(input, output);

        final Source doc = new StreamSource(f);

        assertNotNull("Het bestand 'foutBAGAfleverResponse_ebMS_2.0_v1.2.xml' moet bestaan.", doc);
        assertThat("Het response bestand geldig is volgens het gebruikte digipoort schema.",
                this.v.isInstanceValid(doc), is(true));

        assertXpathEvaluatesTo(
                ERROR.toString(),
                "//*[local-name()='afleverFault'][1]/*[local-name()='foutcode']",
                FileUtils.readFileToString(f, "UTF-8"));
    }

}
