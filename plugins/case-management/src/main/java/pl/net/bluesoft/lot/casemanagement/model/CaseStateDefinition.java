package pl.net.bluesoft.lot.casemanagement.model;

import org.hibernate.annotations.Index;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import javax.persistence.*;

/**
 * Created by pkuciapski on 2014-04-18.
 */
@Entity
@Table(name = "pt_case_state_definition")
@org.hibernate.annotations.Table(
        appliesTo = "pt_case_state_definition",
        indexes = {
                @Index(name = "idx_pt_case_state_def_pk",
                        columnNames = {"id"}
                )
        })
public class CaseStateDefinition extends PersistentEntity {
    @Column(name = "name", nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = CaseDefinition.CASE_DEFINITION_ID)
    @Index(name = "idx_pt_case_state_def_id")
    private CaseDefinition definition;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CaseDefinition getDefinition() {
        return definition;
    }

    public void setDefinition(CaseDefinition definition) {
        this.definition = definition;
    }
}
