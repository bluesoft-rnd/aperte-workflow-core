package pl.net.bluesoft.rnd.processtool.ui.basewidgets.queues.beans;

import org.apache.commons.lang3.StringUtils;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.web.view.IBpmTaskQueryCondition;
import pl.net.bluesoft.rnd.processtool.web.view.ITasksListViewBeanFactory;
import pl.net.bluesoft.rnd.processtool.web.view.TasksListViewBean;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;
import pl.net.bluesoft.util.lang.FormatUtil;

import java.util.Date;

import static pl.net.bluesoft.util.lang.Formats.nvl;

/**
 * Created by Maciej on 2014-09-02.
 */
public class DefaultQueueBeanFactory implements ITasksListViewBeanFactory
{
    @Override
    public TasksListViewBean createFrom(BpmTask task, I18NSource messageSource) {
        DefaultQueueBean taskBean = new DefaultQueueBean();
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

        if(task.getRootProcessInstance() != null) {
            taskBean.setProcessName(messageSource.getMessage(task.getRootProcessInstance().getDefinition().getDescription()));
        } else {
            taskBean.setProcessName(messageSource.getMessage(task.getProcessDefinition().getDescription()));
        }
        taskBean.setName(task.getTaskName());
        taskBean.setCode(nvl(processCode));
        taskBean.setCreationDate(task.getCreateDate());

        /* Translate group controlling to role name. In case of group, login will be not be transalted */
        taskBean.setGroup(messageSource.getMessage(task.getGroupId()));



        /* Add additional demand info */

        String creator = task.getCreator();
        taskBean.setCreator(creator);


        taskBean.setTaskId(task.getInternalTaskId());
        taskBean.setInternalProcessId(task.getInternalProcessId());
        taskBean.setBusinessStatus(messageSource.getMessage(processStatus));

        taskBean.setDeadline(getDeadline(task));
        taskBean.setTooltip(messageSource.getMessage(task.getProcessDefinition().getComment()));
        taskBean.setStep(messageSource.getMessage(task.getCurrentProcessStateConfiguration().getDescription()));

        return taskBean;
    }

    private Date getDeadline(BpmTask task)
    {
        return task.getDeadlineDate();
    }
}
