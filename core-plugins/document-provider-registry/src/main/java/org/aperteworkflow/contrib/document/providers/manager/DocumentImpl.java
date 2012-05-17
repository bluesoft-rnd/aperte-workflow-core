package org.aperteworkflow.contrib.document.providers.manager;

import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: zmalinowski
 * Date: 1/26/12
 * Time: 4:32 PM
 */
public class DocumentImpl implements Document{

    public DocumentImpl(String path, String filename, byte[] content) {
        this.path = path;
        this.filename = filename;
        this.content = content;
        attributes = new HashMap<String, String>();
    }

    private String path;
    private String filename;
    private Map<String, String> attributes;
    private byte[] content;

    
    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getFilename() {
        return filename;
    }

    @Override
    public Map<String, String> getAttributes() {
        return attributes;
    }

    @Override
    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public byte[] getContent() {
        return content;
    }

    @Override
    public String getMimeType() {
        if(filename == null)
        return null;
        return URLConnection.guessContentTypeFromName(filename);

    }
}
