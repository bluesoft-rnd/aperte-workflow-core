package pl.net.bluesoft.casemanagement.exception;

/**
 * Created by pkuciapski on 2014-05-06.
 */
public class CaseNotFoundException extends CaseManagementException {
    public CaseNotFoundException(String s) {
        super(s);
    }

    public CaseNotFoundException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public CaseNotFoundException(Throwable throwable) {
        super(throwable);
    }
}
