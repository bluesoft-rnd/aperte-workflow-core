package pl.net.bluesoft.rnd.processtool.model.report;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import pl.net.bluesoft.rnd.processtool.model.AbstractPersistentEntity;

import javax.persistence.*;

/**
 * User: POlszewski
 * Date: 2013-08-21
 * Time: 14:30
 */
@Entity
@Table(name = "pt_report")
public class Report extends AbstractPersistentEntity {
	public static final String _REPORT_ID = "reportId";
	public static final String _DESCRIPTION = "description";

	@Id
	@GeneratedValue(generator = "idGenerator")
	@GenericGenerator(
			name = "idGenerator",
			strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {
					@org.hibernate.annotations.Parameter(name = "initial_value", value = "" + 1),
					@org.hibernate.annotations.Parameter(name = "value_column", value = "_DB_ID"),
					@org.hibernate.annotations.Parameter(name = "sequence_name", value = "DB_SEQ_ID_PROC_REPORT")
			}
	)
	private Long id;

	private String reportId;
	private String description;

	@Lob
	@Type(type = "org.hibernate.type.StringClobType")
	@Column(length = Integer.MAX_VALUE)
	private String definition;

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public String getReportId() {
		return reportId;
	}

	public void setReportId(String reportId) {
		this.reportId = reportId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDefinition() {
		return definition;
	}

	public void setDefinition(String definition) {
		this.definition = definition;
	}
}
