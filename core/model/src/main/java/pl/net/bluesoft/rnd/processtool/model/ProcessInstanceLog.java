package pl.net.bluesoft.rnd.processtool.model;

import static pl.net.bluesoft.util.lang.FormatUtil.nvl;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.SimpleFormatter;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.*;

import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;

/**
 * @author tlipski@bluesoft.net.pl
 */
@Entity
@Table(name = "pt_process_instance_log")
@org.hibernate.annotations.Table(
        appliesTo="pt_process_instance_log",
        indexes = {
                @Index(name = "idx_pt_log_pk",
                        columnNames = {"id"}
                )
        })
public class ProcessInstanceLog extends AbstractPersistentEntity {
	public static final String _ENTRY_DATE = "entryDate";
	public static final String _EVENT_I18N_KEY = "eventI18NKey";
	public static final String _ADDITIONAL_INFO = "additionalInfo";
	public static final String _LOG_VALUE = "logValue";
	public static final String _LOG_TYPE = "logType";
	public static final String _EXECUTION_ID = "executionId";
	public static final String _STATE = "state";
	public static final String _OWN_PROCESS_INSTANCE = "ownProcessInstance";
	public static final String _PROCESS_INSTANCE = "processInstance";
	public static final String _USER_LOGIN = "userLogin";
	public static final String _USER_SUBSTITUTE_LOGIN = "userSubstituteLogin";

	public static final String LOG_TYPE_START_PROCESS = "START_PROCESS";
	public static final String LOG_TYPE_CLAIM_PROCESS = "CLAIM_PROCESS";
	public static final String LOG_TYPE_PERFORM_ACTION = "PERFORM_ACTION";
	public static final String LOG_TYPE_INFO = "INFO";
    public static final String LOG_TYPE_PROCESS_CHANGE = "PROCESS_CHANGE";

	@Id
	@GeneratedValue(generator = "idGenerator")
	@GenericGenerator(
			name = "idGenerator",
			strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {
					@org.hibernate.annotations.Parameter(name = "initial_value", value = "" + 1),
					@org.hibernate.annotations.Parameter(name = "value_column", value = "_DB_ID"),
					@org.hibernate.annotations.Parameter(name = "sequence_name", value = "DB_SEQ_ID_PROC_INST_LOG")
			}
	)
	@Column(name = "id")
	protected Long id;

//	@Field
//	@CalendarBridge(resolution = Resolution.MINUTE)
	private Date entryDate;

	private String eventI18NKey;

	@Lob
//    @Type(type = "org.hibernate.type.MaterializedClobType")
    @Type(type = "org.hibernate.type.StringClobType")
    @Column(length = Integer.MAX_VALUE)
	private String additionalInfo;

    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    @Column(length = Integer.MAX_VALUE)
	private String logValue;

	private String logType;

    @Index(name="idx_pt_log_executionid")
	private String executionId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="process_state_id")
	private ProcessStateConfiguration state;

//    @XmlTransient
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="own_process_instance_id")
	private ProcessInstance ownProcessInstance;

	@ManyToOne
	@JoinColumn(name="process_instance_id")
	private ProcessInstance processInstance;

    @Index(name="idx_pt_log_login")
	private String userLogin;
	private String userSubstituteLogin;

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public Date getEntryDate() {
		return entryDate;
	}

    public String getFormattedDate(String format)
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        //TODO user timezone
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Europe/Warsaw"));
        return simpleDateFormat.format(getEntryDate());
    }

	public void setEntryDate(Date entryDate) {
		this.entryDate = entryDate;
	}

	public String getEventI18NKey() {
		return eventI18NKey;
	}

	public void setEventI18NKey(String eventI18NKey) {
		this.eventI18NKey = eventI18NKey;
	}

	public String getAdditionalInfo() {
		return additionalInfo;
	}

	public void setAdditionalInfo(String additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

	public ProcessStateConfiguration getState() {
		return state;
	}

	public void setState(ProcessStateConfiguration state) {
		this.state = state;
	}

    @XmlTransient
	public ProcessInstance getProcessInstance() {
		return processInstance;
	}
//    @XmlTransient
	public void setProcessInstance(ProcessInstance processInstance) {
		this.processInstance = processInstance;
	}

	public String getUserLogin() {
		return userLogin;
	}

	public void setUserLogin(String userLogin) {
		this.userLogin = userLogin;
	}

	public String getUserSubstituteLogin() {
		return userSubstituteLogin;
	}

	public void setUserSubstituteLogin(String userSubstituteLogin) {
		this.userSubstituteLogin = userSubstituteLogin;
	}

	public String getLogValue() {
		return logValue;
	}

	public void setLogValue(String logValue) {
		this.logValue = logValue;
	}

	public String getLogType() {
		return logType;
	}

	public void setLogType(String logType) {
		this.logType = logType;
	}

	public static final Comparator<ProcessInstanceLog> DEFAULT_COMPARATOR = new Comparator<ProcessInstanceLog>() {
		@Override
		public int compare(ProcessInstanceLog o1, ProcessInstanceLog o2) {
			Date now = new Date();
			return nvl(o2.getEntryDate(), now).compareTo(nvl(o1.getEntryDate(), now));
		}
	};

	public String getExecutionId()
	{
		return executionId;
	}

	public void setExecutionId(String executionId)
	{
		this.executionId = executionId;
	}

	@XmlTransient 
	public ProcessInstance getOwnProcessInstance() {
		if(ownProcessInstance == null)
			return processInstance;
		return ownProcessInstance;
	}

	public void setOwnProcessInstance(ProcessInstance ownProcessInstance) {
		this.ownProcessInstance = ownProcessInstance;
	}
}
