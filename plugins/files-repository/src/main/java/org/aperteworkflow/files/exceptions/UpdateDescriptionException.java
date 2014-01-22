package org.aperteworkflow.files.exceptions;

/**
 * @author pwysocki@bluesoft.net.pl
 */
public class UpdateDescriptionException extends Throwable {
    public UpdateDescriptionException(String message) {
        super(message);
    }

    public UpdateDescriptionException(String message, Throwable cause) {
        super(message, cause);
    }
}
