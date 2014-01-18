package pl.net.bluesoft.rnd.processtool.ui.basewidgets.steps;

import pl.net.bluesoft.rnd.processtool.model.BpmStep;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;

import java.util.Map;

/**
 *
 * Set process business status
 *
 * For translation use {process-name}.{statusName}
 *
 * @author: "mpawlak@bluesoft.net.pl"
 */
@AliasName(name = "SetProcessBusinessStatus")
public class SetProcessBusinessStatus implements ProcessToolProcessStep
{
    @AutoWiredProperty(required = true)
    private String statusName;

    @Override
    public String invoke(BpmStep bpmStep, Map<String, String> params) throws Exception
    {

        ProcessInstance processInstance = bpmStep.getProcessInstance().getRootProcessInstance();

        if(statusName == null || statusName.isEmpty())
            return STATUS_ERROR;

        processInstance.setBusinessStatus(statusName);

        return STATUS_OK;
    }
}
