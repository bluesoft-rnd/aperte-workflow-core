package pl.net.bluesoft.lot.casemanagement.model;

import org.hibernate.annotations.Index;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by pkuciapski on 2014-04-18.
 */
@Entity
@Table(name = "pt_case_definition")
@org.hibernate.annotations.Table(
        appliesTo = "pt_case_definition",
        indexes = {
                @Index(name = "idx_pt_case_definition_pk",
                        columnNames = {"id"}
                )
        })
public class CaseDefinition extends PersistentEntity {
    static final String CASE_DEFINITION_ID = "case_definition_id";
    public static final String NAME = "name";

    @Column(name = NAME, nullable = false, unique = true)
    // @Index(name = "idx_pt_case_definition_name")
    private String name;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = CASE_DEFINITION_ID)
    private Set<CaseStateDefinition> possibleStates = new HashSet<CaseStateDefinition>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "initial_case_state_def_id")
    private CaseStateDefinition initialState;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<CaseStateDefinition> getPossibleStates() {
        return possibleStates;
    }

    public void setPossibleStates(Set<CaseStateDefinition> possibleStates) {
        this.possibleStates = possibleStates;
    }

    public CaseStateDefinition getInitialState() {
        return initialState;
    }

    public void setInitialState(CaseStateDefinition initialState) {
        this.initialState = initialState;
    }
}
