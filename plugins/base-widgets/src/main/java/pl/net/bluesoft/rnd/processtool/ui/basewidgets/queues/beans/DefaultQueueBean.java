package pl.net.bluesoft.rnd.processtool.ui.basewidgets.queues.beans;

import pl.net.bluesoft.rnd.processtool.web.view.TasksListViewBean;

import java.util.Date;

/**
 * Created by Maciej on 2014-09-02.
 */
public class DefaultQueueBean extends TasksListViewBean
{
    private String group;
    private Date creationDate;
    private String name;
    private String processName;
    private String code;
    private String businessStatus;
    private String creator;
    private String internalProcessId;
    private String tooltip;
    private String step;
    private boolean highlight;
    private Date deadline;

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProcessName() {
        return processName;
    }

    public void setProcessName(String processName) {
        this.processName = processName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getBusinessStatus() {
        return businessStatus;
    }

    public void setBusinessStatus(String businessStatus) {
        this.businessStatus = businessStatus;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getInternalProcessId() {
        return internalProcessId;
    }

    public void setInternalProcessId(String internalProcessId) {
        this.internalProcessId = internalProcessId;
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

    public boolean isHighlight() {
        return highlight;
    }

    public void setHighlight(boolean highlight) {
        this.highlight = highlight;
    }

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }
}
