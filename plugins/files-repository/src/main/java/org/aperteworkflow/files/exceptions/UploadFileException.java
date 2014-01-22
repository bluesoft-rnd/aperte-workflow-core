package org.aperteworkflow.files.exceptions;

import java.io.IOException;

/**
 * @author pwysocki@bluesoft.net.pl
 */
public class UploadFileException extends Exception {

    public UploadFileException(String message, IOException cause) {
        super(message, cause);
    }
}
