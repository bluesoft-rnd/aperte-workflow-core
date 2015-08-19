package pl.net.bluesoft.casemanagement.exception;

/**
 * Created by pkuciapski on 2014-05-06.
 */
public class AddCaseStageException extends CaseManagementException {
    public AddCaseStageException(String s) {
        super(s);
    }

    public AddCaseStageException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public AddCaseStageException(Throwable throwable) {
        super(throwable);
    }
}
