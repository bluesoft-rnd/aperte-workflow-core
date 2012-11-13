package pl.net.bluesoft.rnd.processtool.model;

import static pl.net.bluesoft.util.lang.FormatUtil.nvl;

import java.util.Calendar;
import java.util.Comparator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlTransient;

import org.hibernate.annotations.Type;

import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;

/**
 * @author tlipski@bluesoft.net.pl
 */
@Entity
@Table(name = "pt_process_instance_log")
public class ProcessInstanceLog extends PersistentEntity {
    public enum LogType {
        START, CLAIM, ACTION, INFO
    }

	public static final String LOG_TYPE_START_PROCESS = "START_PROCESS";
	public static final String LOG_TYPE_CLAIM_PROCESS = "CLAIM_PROCESS";
	public static final String LOG_TYPE_PERFORM_ACTION = "PERFORM_ACTION";
	public static final String LOG_TYPE_INFO = "INFO";

//	@Field
//	@CalendarBridge(resolution = Resolution.MINUTE)
	private Calendar entryDate;

	private String eventI18NKey;

	@Lob
//    @Type(type = "org.hibernate.type.MaterializedClobType")
    @Type(type = "org.hibernate.type.StringClobType")
    @Column(length = Integer.MAX_VALUE)
	private String additionalInfo;

	private String logValue;
	private String logType;
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


	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="user_id")
	private UserData user;

    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="user_substitute_id")
	private UserData userSubstitute;
    

	public ProcessInstanceLog() {
	}

	public Calendar getEntryDate() {
		return entryDate;
	}

	public void setEntryDate(Calendar entryDate) {
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

	public void setUser(UserData user) {
		this.user = user;
	}

	public UserData getUser() {
		return user;
	}

    public UserData getUserSubstitute() {
        return userSubstitute;
    }

    public void setUserSubstitute(UserData userSubstitute) {
        this.userSubstitute = userSubstitute;
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
			return nvl(o2.getEntryDate(), Calendar.getInstance()).compareTo(nvl(o1.getEntryDate(), Calendar.getInstance()));
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

	public ProcessInstance getOwnProcessInstance() {
		if(ownProcessInstance == null)
			return processInstance;
		return ownProcessInstance;
	}

	public void setOwnProcessInstance(ProcessInstance ownProcessInstance) {
		this.ownProcessInstance = ownProcessInstance;
	}
}
