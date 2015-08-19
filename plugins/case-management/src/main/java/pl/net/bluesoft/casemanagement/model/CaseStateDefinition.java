package pl.net.bluesoft.casemanagement.model;

import org.hibernate.annotations.Index;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;
import pl.net.bluesoft.rnd.util.CollectionComparer;
import pl.net.bluesoft.util.lang.Lang;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

import static pl.net.bluesoft.casemanagement.model.Constants.CASES_SCHEMA;

/**
 * Created by pkuciapski on 2014-04-18.
 */
@Entity
@Table(name = "pt_case_state_definition", schema = CASES_SCHEMA)
@org.hibernate.annotations.Table(
        appliesTo = "pt_case_state_definition",
        indexes = {
                @Index(name = "idx_pt_case_state_def_pk",
                        columnNames = {"id"}
                )
        })
public class CaseStateDefinition extends PersistentEntity {
    public static final String TABLE = CASES_SCHEMA + "." + CaseStateDefinition.class.getAnnotation(Table.class).name();
    public static final String CASE_STATE_DEFINITION_ID = "case_state_definition_id";
    public static final String NAME = "name";

    @Column(name = NAME, nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = CaseDefinition.CASE_DEFINITION_ID)
    @Index(name = "idx_pt_case_state_def_id")
    private CaseDefinition definition;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = CASE_STATE_DEFINITION_ID)
    private Set<CaseStateWidget> widgets = new HashSet<CaseStateWidget>();

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = CaseStateDefinition.CASE_STATE_DEFINITION_ID)
    private Set<CaseStateRole> roles = new HashSet<CaseStateRole>();

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = CaseStateDefinition.CASE_STATE_DEFINITION_ID)
    private Set<CaseStateProcess> processes = new HashSet<CaseStateProcess>();

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

    public Set<CaseStateWidget> getWidgets() {
        return widgets;
    }

    public void setWidgets(Set<CaseStateWidget> widgets) {
        this.widgets = widgets;
    }

    public Set<CaseStateRole> getRoles() {
        return roles;
    }

    public void setRoles(Set<CaseStateRole> roles) {
        this.roles = roles;
    }

    public Set<CaseStateProcess> getProcesses() {
        return processes;
    }

    public void setProcesses(Set<CaseStateProcess> processes) {
        this.processes = processes;
    }

    public boolean hasProcess(String bpmDefinitionKey) {
        for (CaseStateProcess process : processes) {
            if (process.getBpmDefinitionKey().equals(bpmDefinitionKey))
                return true;
        }
        return false;
    }

	public CaseStateDefinition deepClone() {
		CaseStateDefinition result = new CaseStateDefinition();
		result.name = name;
		if (widgets != null) {
			for (CaseStateWidget widget : widgets) {
				result.widgets.add(widget.deepClone());
			}
		}
		if (roles != null) {
			for (CaseStateRole role : roles) {
				result.roles.add(role.deepClone());
			}
		}
		if (processes != null) {
			for (CaseStateProcess process : processes) {
				result.processes.add(process.deepClone());
			}
		}
		return result;
	}

	public boolean isSimilar(CaseStateDefinition stateDefinition) {
		return Lang.equals(name, stateDefinition.name) &&
				WIDGET_COMPARER.compare(widgets, stateDefinition.widgets) &&
				ROLE_COMPARER.compare(roles, stateDefinition.roles) &&
				PROCESS_COMPARER.compare(processes, stateDefinition.processes);
	}

	static final CollectionComparer<CaseStateWidget> WIDGET_COMPARER = new CollectionComparer<CaseStateWidget>() {
		@Override
		protected String getKey(CaseStateWidget item) {
			return item.getClassName() + "__" + item.getPriority();
		}

		@Override
		protected boolean compareItems(CaseStateWidget item1, CaseStateWidget item2) {
			return item1.isSimilar(item2);
		}
	};

	private static final CollectionComparer<CaseStateRole> ROLE_COMPARER = new CollectionComparer<CaseStateRole>() {
		@Override
		protected String getKey(CaseStateRole item) {
			return item.getPrivilegeName();
		}

		@Override
		protected boolean compareItems(CaseStateRole item1, CaseStateRole item2) {
			return item1.isSimilar(item2);
		}
	};

	private static final CollectionComparer<CaseStateProcess> PROCESS_COMPARER = new CollectionComparer<CaseStateProcess>() {
		@Override
		protected String getKey(CaseStateProcess item) {
			return item.getBpmDefinitionKey();
		}

		@Override
		protected boolean compareItems(CaseStateProcess item1, CaseStateProcess item2) {
			return item1.isSimilar(item2);
		}
	};
}
