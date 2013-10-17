package pl.net.bluesoft.rnd.processtool.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * User: POlszewski
 * Date: 2013-09-27
 * Time: 10:03
 */
@Entity
@Table(name = "pt_step_info")
public class StepInfo extends AbstractPersistentEntity {
	public static final String _TASK_ID = "taskId";
	public static final String _LOCALE = "locale";
	public static final String _MESSAGE = "message";

	@Id
	@GeneratedValue(generator = "idGenerator")
	@GenericGenerator(
			name = "idGenerator",
			strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {
					@org.hibernate.annotations.Parameter(name = "initial_value", value = "" + 1),
					@org.hibernate.annotations.Parameter(name = "value_column", value = "_DB_ID"),
					@org.hibernate.annotations.Parameter(name = "sequence_name", value = "DB_SEQ_ID_STEP_INFO")
			}
	)
	@Column(name = "id")
	protected Long id;

	//@Column(length = 10)
	private long taskId;
	@Column(length = 10)
	private String locale;
	private String message;

	public StepInfo() {}

	public StepInfo(long taskId, String locale, String message) {
		this.taskId = taskId;
		this.locale = locale;
		this.message = message;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public long getTaskId() {
		return taskId;
	}

	public void setTaskId(long taskId) {
		this.taskId = taskId;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}
