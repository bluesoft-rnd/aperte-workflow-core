package pl.net.bluesoft.lot.casemanagement.model;

import org.hibernate.annotations.Index;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import javax.persistence.*;

/**
 * Created by pkuciapski on 2014-04-18.
 */
@Entity
@Table(name = "pt_case_stage")
@org.hibernate.annotations.Table(
        appliesTo = "pt_case_stage",
        indexes = {
                @Index(name = "idx_pt_case_stage_pk",
                        columnNames = {"id"}
                )
        })
public class CaseStage extends PersistentEntity {
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = Case.CASE_ID, nullable = false)
    @Index(name = "idx_pt_case_stage_case_id")
    private Case caseInstance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_definition_id", nullable = false)
    private CaseStateDefinition caseStateDefinition;

    public CaseStateDefinition getCaseStateDefinition() {
        return caseStateDefinition;
    }

    public void setCaseStateDefinition(CaseStateDefinition caseStateDefinition) {
        this.caseStateDefinition = caseStateDefinition;
    }

    public Case getCase() {
        return caseInstance;
    }

    public void setCase(Case caseInstance) {
        this.caseInstance = caseInstance;
    }
}
