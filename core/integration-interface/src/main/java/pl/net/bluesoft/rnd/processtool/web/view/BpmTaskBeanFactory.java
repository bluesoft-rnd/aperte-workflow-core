package pl.net.bluesoft.rnd.processtool.web.view;

import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.util.i18n.I18NSource;

import static pl.net.bluesoft.util.lang.Formats.nvl;

/**
 * Created by Marcin Król on 2014-05-12.
 */
public class BpmTaskBeanFactory implements TasksListViewBeanFactory {

    public BpmTaskBean createFrom(BpmTask task, I18NSource messageSource)
    {
        BpmTaskBean taskBean = new BpmTaskBean();
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

        taskBean.setTaskAssigneDate(task.getCreateDate());
        taskBean.setProcessName(messageSource.getMessage(task.getProcessDefinition().getDescription()));
        taskBean.setName(task.getTaskName());
        taskBean.setCode(nvl(processCode));
        taskBean.setCreationDate(task.getCreateDate());
        taskBean.setAssignee(task.getAssignee());
        taskBean.setCreator(task.getCreator());
        taskBean.setTaskId(task.getInternalTaskId());
        taskBean.setInternalProcessId(task.getInternalProcessId());
        taskBean.setBusinessStatus(messageSource.getMessage(processStatus));
        taskBean.setProcessStateConfigurationId(task.getCurrentProcessStateConfiguration().getId().toString());
        taskBean.setDeadline(task.getDeadlineDate());
        taskBean.setTooltip(messageSource.getMessage(task.getProcessDefinition().getComment()));
        taskBean.setStep(messageSource.getMessage(task.getCurrentProcessStateConfiguration().getDescription()));
        taskBean.setStepInfo(task.getStepInfo());
        return taskBean;
    }

    @Override
    public IBpmTaskQueryCondition getBpmTaskQueryCondition() {
        /* Standard bpm task query */
        return new BpmTaskQueryCondition();
    }
}
