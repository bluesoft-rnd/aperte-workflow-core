package org.aperteworkflow.widgets.doclist;

import java.io.InputStream;
import java.util.Map;

/**
 * @author tlipski@bluesoft.net.pl
 */
public interface DocumentEntry {

    String getName();
    InputStream getStream();
    String getMimeType();
    Map<String,String> getProperties();
    
    
}
