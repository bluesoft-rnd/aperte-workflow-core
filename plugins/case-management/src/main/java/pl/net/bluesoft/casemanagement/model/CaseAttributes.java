package pl.net.bluesoft.casemanagement.model;

/**
 * Created by pkuciapski on 2014-05-15.
 */
public enum CaseAttributes {
    COMMENTS("comments"),
    FILES("files"),
    CASE_ID("caseId");

    private final String value;

    CaseAttributes(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
