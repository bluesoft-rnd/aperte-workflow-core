package pl.net.bluesoft.lot.casemanagement.model;

import org.hibernate.annotations.Index;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import javax.persistence.*;

/**
 * Created by pkuciapski on 2014-04-18.
 */
@Entity
@Table(name = "pt_case_state_widget_attr")
@org.hibernate.annotations.Table(
        appliesTo = "pt_case_state_widget_attr",
        indexes = {
                @Index(name = "idx_pt_case_state_wid_attr_pk",
                        columnNames = {"id"}
                )
        })
public class CaseStateWidgetAttribute extends PersistentEntity {
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = CaseStateWidget.CASE_STATE_WIDGET_ID, nullable = false)
    @Index(name = "idx_pt_case_state_wid_attr_id")
    private CaseStateWidget caseStateWidget;

    public CaseStateWidget getCaseStateWidget() {
        return caseStateWidget;
    }

    public void setCaseStateWidget(CaseStateWidget caseStateWidget) {
        this.caseStateWidget = caseStateWidget;
    }
}
