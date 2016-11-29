/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.b3p.topnl.converters;

import java.io.InputStream;

/**
 *
 * @author meine
 */
public class ConverterFactory {
    
    /*protected final static Element STRI2006_ROOTELEMENT_MANIFEST = new Element("Manifest", Namespace.getNamespace("http://www.geonovum.nl/stri/2006/1"));
    protected final static Element STRI2006_ROOTELEMENT_GELEIDEFORMULIER = new Element("GeleideFormulier", Namespace.getNamespace("http://www.geonovum.nl/stri/2006/1"));

 */
    private Converter top250nlConverter = new Top250NLConverter();
    
    public ConverterFactory(){
        
    }
    
    public Converter getConverter(Object jaxbObject){
        return top250nlConverter;
    }
    
}
