package pl.net.bluesoft.casemanagement.model;

import org.hibernate.annotations.Index;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;
import pl.net.bluesoft.rnd.util.CollectionComparer;
import pl.net.bluesoft.util.lang.Lang;

import javax.persistence.*;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static pl.net.bluesoft.casemanagement.model.Constants.CASES_SCHEMA;

/**
 * Created by pkuciapski on 2014-04-18.
 */
@Entity
@Table(name = "pt_case_definition", schema = CASES_SCHEMA)
@org.hibernate.annotations.Table(
        appliesTo = "pt_case_definition",
        indexes = {
                @Index(name = "idx_pt_case_definition_pk",
                        columnNames = {"id"}
                )
        })
public class CaseDefinition extends PersistentEntity {
    public static final String TABLE = CASES_SCHEMA + "." + CaseDefinition.class.getAnnotation(Table.class).name();
    static final String CASE_DEFINITION_ID = "case_definition_id";
    public static final String NAME = "name";

    @Column(name = NAME, nullable = false, unique = true)
    private String name;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = CASE_DEFINITION_ID)
    private Set<CaseStateDefinition> possibleStates = new HashSet<CaseStateDefinition>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "initial_case_state_def_id")
    private CaseStateDefinition initialState;

	@Transient
	private String initialStateName;

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

	public String getInitialStateName() {
		return initialStateName;
	}

	public void setInitialStateName(String initialStateName) {
		this.initialStateName = initialStateName;
	}

	public CaseStateDefinition getState(String name) {
		for (CaseStateDefinition def : possibleStates) {
			if (def.getName().equals(name)) {
				return def;
			}
		}
		return null;
	}

	public boolean isSimilar(CaseDefinition definition) {
		return Lang.equals(name, definition.name) &&
				STATE_COMPARER.compare(possibleStates, definition.possibleStates) &&
				STATE_COMPARER.compare(initialState != null ? Collections.singleton(initialState) : null,
						definition.initialState != null ? Collections.singleton(definition.initialState) : null);
	}

	private static final CollectionComparer<CaseStateDefinition> STATE_COMPARER = new CollectionComparer<CaseStateDefinition>() {
		@Override
		protected String getKey(CaseStateDefinition item) {
			return item.getName();
		}

		@Override
		protected boolean compareItems(CaseStateDefinition item1, CaseStateDefinition item2) {
			return item1.isSimilar(item2);
		}
	};
}
