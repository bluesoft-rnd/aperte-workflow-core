package pl.net.bluesoft.rnd.processtool.ui.basewidgets.steps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.BpmStep;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateConfiguration;
import pl.net.bluesoft.rnd.processtool.roles.IUserRolesManager;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.util.StepUtil;

import java.util.Map;

/**
 *
 * Set processStateConfigurationId as given attribute for ShadowWidget
 *
 * @author: "mpawlak@bluesoft.net.pl"
 */
@AliasName(name = "SetProcessStateConfigurationId")
public class SetProcessStateConfigurationId implements ProcessToolProcessStep
{
    @AutoWiredProperty
    private String stepName;

    @AutoWiredProperty
    private String attributeName;

    @AutoWiredProperty
    private Boolean lookInParentProcess = false;

    @Autowired
    private IUserRolesManager userRolesManager;

    @Override
    public String invoke(BpmStep bpmStep, Map<String, String> params) throws Exception
    {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        ProcessInstance processInstance = bpmStep.getProcessInstance();
        if(lookInParentProcess)
        {
            processInstance = processInstance.getRootProcessInstance();
        }


        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();

        for(ProcessStateConfiguration state: processInstance.getDefinition().getStates())
        {
            if(state.getName().equals(stepName))
            {
                processInstance.setSimpleAttribute(attributeName, state.getId().toString());
                return STATUS_OK;
            }
        }

        throw new RuntimeException("Process does not have state with given name: "+stepName);
    }
}
