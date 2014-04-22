package pl.net.bluesoft.lot.casemanagement.model;

import org.hibernate.annotations.Index;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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
    final static String CASE_STAGE_ID = "case_stage_id";

    @Column(name = "name")
    private String name;

    @Column(name = "start_date", nullable = false)
    private Date startDate;

    @Column(name = "end_date", nullable = true)
    private Date endDate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = Case.CASE_ID)
    @Index(name = "idx_pt_case_stage_case_id")
    private Case caseInstance;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "state_definition_id")
    private CaseStateDefinition caseStateDefinition;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = CASE_STAGE_ID)
    private Set<CaseStageSimpleAttribute> simpleAttributes = new HashSet<CaseStageSimpleAttribute>();

    public void setSimpleAttributes(Set<CaseStageSimpleAttribute> simpleAttributes) {
        this.simpleAttributes = simpleAttributes;
    }

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

    public Set<CaseStageSimpleAttribute> getSimpleAttributes() {
        return simpleAttributes;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
