package pl.net.bluesoft.rnd.processtool.model.processdata;

import org.hibernate.annotations.GenericGenerator;
import pl.net.bluesoft.rnd.processtool.model.AbstractPersistentEntity;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "pt_process_deadline")
public class ProcessDeadline extends AbstractPersistentEntity {
	@Id
	@GeneratedValue(generator = "idGenerator")
	@GenericGenerator(
			name = "idGenerator",
			strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {
					@org.hibernate.annotations.Parameter(name = "initial_value", value = "" + 1),
					@org.hibernate.annotations.Parameter(name = "value_column", value = "_DB_ID"),
					@org.hibernate.annotations.Parameter(name = "sequence_name", value = "DB_SEQ_ID_PROC_DEADLINE")
			}
	)
	@Column(name = "id")
	protected Long id;

    private String taskName;
    private Date dueDate;
    private String profileName;
    private String templateName;
    private String notifyUsersWithLogin;
    private String notifyUsersWithRole;
    private boolean skipAssignee;
    private boolean alreadyNotified;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="process_instance_id")
	private ProcessInstance processInstance;

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(Long id) {
		this.id = id;
	}

	public boolean isSkipAssignee() {
		return skipAssignee;
	}

	public void setSkipAssignee(boolean skipAssignee) {
		this.skipAssignee = skipAssignee;
	}

	public boolean isAlreadyNotified() {
		return alreadyNotified;
	}

	public void setAlreadyNotified(boolean alreadyNotified) {
		this.alreadyNotified = alreadyNotified;
	}

	public String getNotifyUsersWithLogin() {
        return notifyUsersWithLogin;
    }

    public void setNotifyUsersWithLogin(String notifyUsersWithLogin) {
        this.notifyUsersWithLogin = notifyUsersWithLogin;
    }

    public String getNotifyUsersWithRole() {
        return notifyUsersWithRole;
    }

    public void setNotifyUsersWithRole(String notifyUsersWithRole) {
        this.notifyUsersWithRole = notifyUsersWithRole;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

	public String getProfileName() {
		return profileName;
	}

	public void setProfileName(String profileName) {
		this.profileName = profileName;
	}

	public ProcessInstance getProcessInstance() {
		return processInstance;
	}

	public void setProcessInstance(ProcessInstance processInstance) {
		this.processInstance = processInstance;
	}
}
