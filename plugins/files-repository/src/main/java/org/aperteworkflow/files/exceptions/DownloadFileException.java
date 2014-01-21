package org.aperteworkflow.files.exceptions;

import java.io.FileNotFoundException;

/**
 * @author pwysocki@bluesoft.net.pl
 */
public class DownloadFileException extends Throwable {
    public DownloadFileException(String message) {
        super(message);
    }

    public DownloadFileException(String message, FileNotFoundException cause) {
        super(message, cause);
    }
}
