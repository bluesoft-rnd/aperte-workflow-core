package org.aperteworkflow.contrib.document.providers.manager;

import java.io.InputStream;
import java.util.Map;

/**
 * Document interface - underlying document can be implemented as CMIS, Liferay, DB, etc.
 *
 * @author tlipski@bluesoft.net.pl
 */
public interface Document {
    
    String getPath();
    String getFilename();
    Map<String,String> getAttributes();
    void setContent(byte[] content);
    byte[] getContent();
    String getMimeType();
    //TODO more getters and setters and accessors


}
