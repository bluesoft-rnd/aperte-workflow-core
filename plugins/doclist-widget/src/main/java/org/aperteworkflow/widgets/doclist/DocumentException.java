package org.aperteworkflow.widgets.doclist;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class DocumentException extends RuntimeException {

    public DocumentException(String message) {
        super(message);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public DocumentException(String message, Throwable cause) {
        super(message, cause);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public DocumentException(Throwable cause) {
        super(cause);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
