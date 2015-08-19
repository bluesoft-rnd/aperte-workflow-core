package pl.net.bluesoft.casemanagement.model;

/**
 * Created by pkuciapski on 2014-06-26.
 */
public enum CaseStageAttributes {
    STAGE_FILES("stageFiles"),
    COMMENTS("stageComments");

    private final String value;

    CaseStageAttributes(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
