package org.aperteworkflow.files.model;

/**
 * @author pwysocki@bluesoft.net.pl
 */
public class FileItemContent {
    private String contentType;
    private byte[] bytes;
    private String name;
    private String extension;
    private String filename;

    public FileItemContent() {
        super();
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public String getName() {
        return name;
    }

    public void setName(String fileName) {
        this.name = fileName;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String fileExt) {
        this.extension = fileExt;
    }

    public String getFilename() {
        if (filename == null) {
            return getName() + (getExtension() != null ? "." + getExtension() : "");
        }
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}

