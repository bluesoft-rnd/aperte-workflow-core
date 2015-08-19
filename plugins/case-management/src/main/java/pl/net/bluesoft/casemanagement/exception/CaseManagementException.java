package pl.net.bluesoft.casemanagement.exception;

/**
 * Created by pkuciapski on 2014-04-24.
 */
public class CaseManagementException extends Exception {
    public CaseManagementException(String s) {
        super(s);
    }

    public CaseManagementException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public CaseManagementException(Throwable throwable) {
        super(throwable);
    }
}
