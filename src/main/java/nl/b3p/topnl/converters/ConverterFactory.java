/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.b3p.topnl.converters;

import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import nl.b3p.topnl.TopNLType;

/**
 *
 * @author meine
 */
public class ConverterFactory {
    
    /*protected final static Element STRI2006_ROOTELEMENT_MANIFEST = new Element("Manifest", Namespace.getNamespace("http://www.geonovum.nl/stri/2006/1"));
    protected final static Element STRI2006_ROOTELEMENT_GELEIDEFORMULIER = new Element("GeleideFormulier", Namespace.getNamespace("http://www.geonovum.nl/stri/2006/1"));

 */
    private Converter top250nlConverter = new Top250NLConverter();
    private JAXBContext context250nl = null;
    private JAXBContext context100nl = null;
    
    public ConverterFactory() throws JAXBException{
        
        context250nl = JAXBContext.newInstance("nl.b3p.topnl.top250nl");
        context100nl = JAXBContext.newInstance("nl.b3p.topnl.top100nl");
    }
    
    public Converter getConverter(Object jaxbObject){
        return top250nlConverter;
    }
    
    public JAXBContext getContext(TopNLType type) {
        switch (type) {
            case TOP100NL:
                return context100nl;
            case TOP250NL:
                return context250nl;
            default:
                throw new IllegalArgumentException("TopNL type " + type.getType() + " not (yet) supported.");
        }
    }

}
