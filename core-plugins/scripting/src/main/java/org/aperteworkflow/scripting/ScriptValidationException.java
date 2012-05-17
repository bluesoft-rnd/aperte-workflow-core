package org.aperteworkflow.scripting;

/**
 * Created by IntelliJ IDEA.
 * User: zmalinowski
 * Date: 2/28/12
 * Time: 4:50 PM
 */
public class ScriptValidationException extends Exception {

    public ScriptValidationException() {
    }

    public ScriptValidationException(Throwable cause) {
        super(cause);
    }

    public ScriptValidationException(String message) {
        super(message);
    }

    public ScriptValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
