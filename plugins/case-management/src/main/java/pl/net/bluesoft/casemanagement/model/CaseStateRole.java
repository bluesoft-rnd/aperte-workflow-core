package pl.net.bluesoft.casemanagement.model;

import org.hibernate.annotations.Index;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;
import pl.net.bluesoft.util.lang.Lang;

import javax.persistence.*;

import static pl.net.bluesoft.casemanagement.model.Constants.CASES_SCHEMA;

/**
 * Created by pkuciapski on 2014-05-06.
 */
@Entity
@Table(name = "pt_case_state_role", schema = CASES_SCHEMA)
@org.hibernate.annotations.Table(
        appliesTo = "pt_case_state_role",
        indexes = {
                @Index(name = "idx_pt_case_state_role_pk",
                        columnNames = {"id"}
                )
        })
public class CaseStateRole extends PersistentEntity {
    public static final String TABLE = CASES_SCHEMA + "." + CaseStateRole.class.getAnnotation(Table.class).name();
    public static final String PRIVILEGE_EDIT = "EDIT";
    public static final String PRIVILEGE_VIEW = "VIEW";

    @Column(name = "role_name", nullable = false)
    private String roleName;

    @Column(name = "privilege_name", nullable = false)
    private String privilegeName;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = CaseStateDefinition.CASE_STATE_DEFINITION_ID)
    @Index(name = "idx_pt_case_state_role_def_id")
    private CaseStateDefinition stateDefinition;

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public CaseStateDefinition getStateDefinition() {
        return stateDefinition;
    }

    public void setStateDefinition(CaseStateDefinition stateDefinition) {
        this.stateDefinition = stateDefinition;
    }

    public String getPrivilegeName() {
        return privilegeName;
    }

    public void setPrivilegeName(String privilegeName) {
        this.privilegeName = privilegeName;
    }

	public CaseStateRole deepClone() {
		CaseStateRole result = new CaseStateRole();
		result.roleName = roleName;
		result.privilegeName = privilegeName;
		return result;
	}

	public boolean isSimilar(CaseStateRole role) {
		return Lang.equals(roleName, role.roleName) && Lang.equals(privilegeName, role.privilegeName);
	}
}
