package pl.net.bluesoft.casemanagement.model;

import org.hibernate.annotations.Index;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;
import pl.net.bluesoft.util.lang.Lang;

import javax.persistence.*;

import static pl.net.bluesoft.casemanagement.model.Constants.CASES_SCHEMA;

/**
 * Created by pkuciapski on 2014-05-16.
 */
@Entity
@Table(name = "pt_case_state_process", schema = CASES_SCHEMA)
@org.hibernate.annotations.Table(
        appliesTo = "pt_case_state_process",
        indexes = {
                @Index(name = "idx_pt_case_state_proc_pk",
                        columnNames = {"id"}
                )
        })
public class CaseStateProcess extends PersistentEntity {
    public static final String TABLE = CASES_SCHEMA + "." + CaseStateProcess.class.getAnnotation(Table.class).name();
    @Column(name = "bpm_definition_key", nullable = false)
    private String bpmDefinitionKey;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = CaseStateDefinition.CASE_STATE_DEFINITION_ID)
    @Index(name = "idx_pt_case_state_proc_def_id")
    private CaseStateDefinition stateDefinition;

    @Column(name = "process_label", nullable = false)
    private String processLabel;

    @Column(name = "process_action_type", nullable = false)
    private String processActionType;

    @Column(name = "process_priority", nullable = false)
    private String processPriority;

    @Column(name = "process_icon", nullable = false)
    private String processIcon;

    public String getBpmDefinitionKey() {
        return bpmDefinitionKey;
    }

    public void setBpmDefinitionKey(String bpmDefinitionKey) {
        this.bpmDefinitionKey = bpmDefinitionKey;
    }

    public CaseStateDefinition getStateDefinition() {
        return stateDefinition;
    }

    public void setStateDefinition(CaseStateDefinition stateDefinition) {
        this.stateDefinition = stateDefinition;
    }

    public String getProcessLabel() {
        return processLabel;
    }

    public void setProcessLabel(String processLabel) {
        this.processLabel = processLabel;
    }

    public String getProcessActionType() {
        return processActionType;
    }

    public void setProcessActionType(String processActionType) {
        this.processActionType = processActionType;
    }

    public String getProcessPriority() {
        return processPriority;
    }

    public void setProcessPriority(String processPriority) {
        this.processPriority = processPriority;
    }

    public String getProcessIcon() {
        return processIcon;
    }

    public void setProcessIcon(String processIcon) {
        this.processIcon = processIcon;
    }

    public CaseStateProcess deepClone() {
		CaseStateProcess result = new CaseStateProcess();
		result.bpmDefinitionKey = bpmDefinitionKey;
		return result;
	}

	public boolean isSimilar(CaseStateProcess process) {
		return Lang.equals(bpmDefinitionKey, process.bpmDefinitionKey);
	}
}
