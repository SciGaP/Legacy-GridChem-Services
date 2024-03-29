/*
 * ResourceBeanFactoryImpl.java
 *
 * Created on April 19, 2005, 2:59 PM
 */

package org.gridchem.service.wsclients.gpir;

import org.jdom.Element;
/**
 *
 * @author ericrobe
 */
public class VizResourceFactory implements ResourceFactory {
    
    public AbstractResourceBean createBean() {
        return new VizResourceBean(); 
    } 
    
    public GenericParser createParser(Element element) {
        return new VizResourceParser(element); 
    }
         
    
}
