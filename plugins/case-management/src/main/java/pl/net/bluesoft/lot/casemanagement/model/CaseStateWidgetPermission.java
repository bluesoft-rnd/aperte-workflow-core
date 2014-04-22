package pl.net.bluesoft.lot.casemanagement.model;

import org.hibernate.annotations.Index;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import javax.persistence.*;

/**
 * Created by pkuciapski on 2014-04-18.
 */
@Entity
@Table(name = "pt_case_state_widget_perm")
@org.hibernate.annotations.Table(
        appliesTo = "pt_case_state_widget_perm",
        indexes = {
                @Index(name = "idx_pt_case_state_wid_perm_pk",
                        columnNames = {"id"}
                )
        })
public class CaseStateWidgetPermission extends PersistentEntity {
    @Column(name = "role_name", nullable = false)
    private String roleName;

    @Column(name = "privilege_name", nullable = false)
    private String privilegeName;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = CaseStateWidget.CASE_STATE_WIDGET_ID, nullable = false)
    @Index(name = "idx_pt_case_state_wid_perm_id")
    private CaseStateWidget caseStateWidget;

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

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
}
