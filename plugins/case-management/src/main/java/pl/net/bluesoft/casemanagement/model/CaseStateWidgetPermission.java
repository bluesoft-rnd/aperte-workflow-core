package pl.net.bluesoft.casemanagement.model;

import org.hibernate.annotations.Index;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;
import pl.net.bluesoft.rnd.processtool.model.config.IPermission;
import pl.net.bluesoft.util.lang.Lang;

import javax.persistence.*;

import static pl.net.bluesoft.casemanagement.model.Constants.CASES_SCHEMA;

/**
 * Created by pkuciapski on 2014-04-18.
 */
@Entity
@Table(name = "pt_case_state_widget_perm", schema = CASES_SCHEMA)
@org.hibernate.annotations.Table(
        appliesTo = "pt_case_state_widget_perm",
        indexes = {
                @Index(name = "idx_pt_case_state_wid_perm_pk",
                        columnNames = {"id"}
                )
        })
public class CaseStateWidgetPermission extends PersistentEntity implements IPermission {
    public static final String TABLE = CASES_SCHEMA + "." + CaseStateWidgetPermission.class.getAnnotation(Table.class).name();
    @Column(name = "role_name", nullable = false)
    private String roleName;

    @Column(name = "privilege_name", nullable = false)
    private String privilegeName;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = CaseStateWidget.CASE_STATE_WIDGET_ID)
    @Index(name = "idx_pt_case_state_wid_perm_id")
    private CaseStateWidget caseStateWidget;

    @Override
	public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    @Override
	public String getPrivilegeName() {
        return privilegeName;
    }

    public void setPrivilegeName(String privilegeName) {
        this.privilegeName = privilegeName;
    }

    public CaseStateWidget getCaseStateWidget() {
        return caseStateWidget;
    }

    public void setCaseStateWidget(CaseStateWidget caseStateWidget) {
        this.caseStateWidget = caseStateWidget;
    }

	public CaseStateWidgetPermission deepClone() {
		CaseStateWidgetPermission result = new CaseStateWidgetPermission();
		result.roleName = roleName;
		result.privilegeName = privilegeName;
		return result;
	}

	public boolean isSimilar(CaseStateWidgetPermission permission) {
		return Lang.equals(roleName, permission.roleName) && Lang.equals(privilegeName, permission.privilegeName);
	}
}
