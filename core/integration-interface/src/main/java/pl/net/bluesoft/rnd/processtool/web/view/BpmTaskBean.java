package pl.net.bluesoft.rnd.processtool.web.view;

import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import java.util.Date;

import static pl.net.bluesoft.util.lang.Formats.nvl;

/**
 * Process Instance Bean
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class BpmTaskBean extends TasksListViewBean
{
	private static final long serialVersionUID = 8814138252434090661L;
	
	private String name;
	private String processName;
	private String code;
    private String businessStatus;
	private String assignee;

	private Date deadline;

	private String internalProcessId;
	private String processStateConfigurationId;
	private String tooltip;

    private String step;
	private String stepInfo;

	public static BpmTaskBean createFrom(BpmTask task, I18NSource messageSource)
	{
        BpmTaskBean processBean = new BpmTaskBean();
        String processStatusCode = task.getProcessInstance().getBusinessStatus();
        String processStatus = "";

        if(processStatusCode != null)
        {
            processStatus =  task.getProcessDefinition().getBpmDefinitionKey()+"."+processStatusCode;
        }
        else if(processStatusCode == null && task.getProcessInstance().getRootProcessInstance() != null)
        {
            processStatusCode =  task.getProcessInstance().getRootProcessInstance().getBusinessStatus();
            if(processStatusCode != null)
                processStatus =  task.getProcessInstance().getRootProcessInstance().getDefinition().getBpmDefinitionKey()+"."+processStatusCode;
        }
        else
            processStatus =  "";

        String processCode = task.getProcessInstance().getExternalKey();
        if(processCode == null && task.getProcessInstance().getRootProcessInstance() != null)
            processCode = task.getProcessInstance().getRootProcessInstance().getExternalKey();

        if(processCode == null)
            processCode = task.getProcessInstance().getInternalId();


		processBean.processName = messageSource.getMessage(task.getProcessDefinition().getDescription());
		processBean.name = task.getTaskName();
		processBean.code = nvl(processCode);
		processBean.setCreationDate(task.getCreateDate());
		processBean.assignee = task.getAssignee();
		processBean.setCreator(task.getCreator());
		processBean.setTaskId(task.getInternalTaskId());
		processBean.internalProcessId = task.getInternalProcessId();
        processBean.businessStatus = messageSource.getMessage(processStatus);
		processBean.processStateConfigurationId = task.getCurrentProcessStateConfiguration().getId().toString();
		processBean.deadline = task.getDeadlineDate();
		processBean.tooltip = messageSource.getMessage(task.getProcessDefinition().getComment());
		processBean.step = messageSource.getMessage(task.getCurrentProcessStateConfiguration().getDescription());
		processBean.stepInfo = task.getStepInfo();
		return processBean;
	}

    public String getBusinessStatus() {
        return businessStatus;
    }

    public void setBusinessStatus(String businessStatus) {
        this.businessStatus = businessStatus;
    }

    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getAssignee() {
		return assignee;
	}

	public void setAssignee(String assignee) {
		this.assignee = assignee;
	}

	public Date getDeadline() {
		return deadline;
	}

	public void setDeadline(Date deadline) {
		this.deadline = deadline;
	}

	public String getInternalProcessId() {
		return internalProcessId;
	}

	public void setInternalProcessId(String processId) {
		this.internalProcessId = processId;
	}

	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	public String getProcessStateConfigurationId() {
		return processStateConfigurationId;
	}

	public void setProcessStateConfigurationId(String processWidgetStatedIds) {
		this.processStateConfigurationId = processWidgetStatedIds;
	}

	public String getTooltip() {
		return tooltip;
	}

	public void setTooltip(String tooltip) {
		this.tooltip = tooltip;
	}

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

	public String getStepInfo() {
		return stepInfo;
	}

	public void setStepInfo(String stepInfo) {
		this.stepInfo = stepInfo;
	}
}
