package pl.net.bluesoft.casemanagement.model;

import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

import javax.persistence.*;

import static pl.net.bluesoft.casemanagement.model.Constants.CASES_SCHEMA;

/**
 * User: POlszewski
 * Date: 2014-06-11
 */
@Entity
@Table(name = "pt_case_stage_s_l_attr", schema = CASES_SCHEMA)
@org.hibernate.annotations.Table(
        appliesTo = "pt_case_stage_s_l_attr",
        indexes = {
                @Index(name = "idx_pt_case_stg_s_l_a_stg_id",
                        columnNames = {CaseStage.CASE_STAGE_ID}
                )
        })
public class CaseStageSimpleLargeAttribute extends AbstractCaseAttributeBase {
    public static final String TABLE = CASES_SCHEMA + "." + CaseStageSimpleLargeAttribute.class.getAnnotation(Table.class).name();
	@Column(name = "value")
	@Lob
	@Type(type = "org.hibernate.type.StringClobType")
	private String value;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = CaseStage.CASE_STAGE_ID, nullable = true)
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

	@Override
	public String toString() {
		return getKey() + '=' + value;
	}
}
