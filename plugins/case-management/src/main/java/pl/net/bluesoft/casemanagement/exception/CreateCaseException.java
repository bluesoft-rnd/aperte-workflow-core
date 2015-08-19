package pl.net.bluesoft.casemanagement.exception;

/**
 * Created by pkuciapski on 2014-04-23.
 */
public class CreateCaseException extends CaseManagementException {
    public CreateCaseException(String s) {
        super(s);
    }

    public CreateCaseException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public CreateCaseException(Throwable throwable) {
        super(throwable);
    }
}
