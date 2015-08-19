package pl.net.bluesoft.casemanagement.model;

import org.hibernate.annotations.Index;
import pl.net.bluesoft.rnd.processtool.model.PersistentEntity;

import javax.persistence.*;

import static pl.net.bluesoft.casemanagement.model.Constants.CASES_SCHEMA;

/**
 * User: POlszewski
 * Date: 2014-08-09
 */
@Entity
@Table(name = "pt_case_stage_attr", schema = CASES_SCHEMA,
		uniqueConstraints = @UniqueConstraint(columnNames = {CaseStage.CASE_STAGE_ID, "key"})
)
@Inheritance(strategy = InheritanceType.JOINED)
@org.hibernate.annotations.Table(
		appliesTo = "pt_case_stage_attr",
		indexes = {
				@Index(name = "idx_pt_case_stage_attr_pk",
						columnNames = {"id"}
				),
				@Index(name = "idx_pt_case_stage_attr_cst_id", columnNames = CaseStage.CASE_STAGE_ID)
		}
)
public abstract class CaseStageAttribute extends PersistentEntity {
    public static final String TABLE = CASES_SCHEMA + "." + CaseStageAttribute.class.getAnnotation(Table.class).name();
	@Column(name = "key")
	private String key;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = CaseStage.CASE_STAGE_ID, nullable = true)
	private CaseStage stage;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public CaseStage getStage() {
		return stage;
	}

	public void setStage(CaseStage stage) {
		this.stage = stage;
	}
}
