package pl.net.bluesoft.lot.casemanagement.model;

import org.hibernate.annotations.Index;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import javax.persistence.*;

/**
 * Created by pkuciapski on 2014-04-22.
 */
@Entity
@Table(name = "pt_case_stage_s_attr")
@org.hibernate.annotations.Table(
        appliesTo = "pt_case_stage_s_attr",
        indexes = {
                @Index(name = "idx_pt_case_stage_s_attr_pk",
                        columnNames = {"id"}
                )
        })
public class CaseStageSimpleAttribute extends AbstractCaseAttributeBase {
    @Column(name = "value")
    private String value;

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = CaseStage.CASE_STAGE_ID, nullable = false)
    private CaseStage stage;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public CaseStage getStage() {
        return stage;
    }

    public void setStage(CaseStage stage) {
        this.stage = stage;
    }
}
