/*
* Copyright (C) 2022 B3Partners B.V.
*
* SPDX-License-Identifier: MIT
*
*/
package nl.b3p.brmo.bag2.loader;

import nl.b3p.brmo.bag2.xml.leveringsdocument.BAGExtractLevering;
import nl.b3p.brmo.bag2.xml.leveringsdocument.GebiedRegistratief;
import nl.b3p.brmo.bag2.xml.leveringsdocument.LVCExtract;
import nl.b3p.brmo.bag2.xml.leveringsdocument.SelectieGegevens;
import org.junit.jupiter.api.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class LeveringsdocumentUnmarshalTest {
    @Test
    void testUnmarshal() throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(BAGExtractLevering.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        BAGExtractLevering levering = (BAGExtractLevering) unmarshaller.unmarshal(BAG2TestFiles.getTestFile("Leveringsdocument-BAG-Extract.xml"));
        assertNotNull(levering);
        SelectieGegevens selectieGegevens = levering.getSelectieGegevens();
        assertNotNull(levering.getSelectieGegevens());
        LVCExtract lvcExtract = selectieGegevens.getLVCExtract();
        assertNotNull(lvcExtract);
        assertEquals("2021-10-15", lvcExtract.getStandTechnischeDatum().toString());
        GebiedRegistratief gebiedRegistratief = levering.getSelectieGegevens().getGebiedRegistratief();
        assertNotNull(gebiedRegistratief);
        assertNotNull(gebiedRegistratief.getGebiedGEM());
        assertEquals(1, gebiedRegistratief.getGebiedGEM().getGemeenteCollectie().getGemeente().size());
        assertEquals("Stichtse Vecht", gebiedRegistratief.getGebiedGEM().getGemeenteCollectie().getGemeente().get(0).getGemeenteNaam());
    }
}
