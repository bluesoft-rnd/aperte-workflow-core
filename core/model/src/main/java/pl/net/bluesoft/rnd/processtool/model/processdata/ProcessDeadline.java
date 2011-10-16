package pl.net.bluesoft.rnd.processtool.model.processdata;

import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceAttribute;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "pt_ext_pi_deadline_attr")
public class ProcessDeadline extends ProcessInstanceAttribute {
    private String taskName;
    private Date dueDate;
    private String templateName;

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
}
