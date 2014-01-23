package org.aperteworkflow.files.exceptions;

/**
 * @author pwysocki@bluesoft.net.pl
 */
public class UploadFileException extends Exception {
    public UploadFileException(String message) {
        super(message);
    }

    public UploadFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
