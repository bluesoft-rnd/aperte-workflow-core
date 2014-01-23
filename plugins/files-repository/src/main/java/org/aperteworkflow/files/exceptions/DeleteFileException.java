package org.aperteworkflow.files.exceptions;

import java.io.IOException;

/**
 * @author pwysocki@bluesoft.net.pl
 */
public class DeleteFileException extends Exception {
    public DeleteFileException(String message) {
        super(message);
    }

    public DeleteFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
