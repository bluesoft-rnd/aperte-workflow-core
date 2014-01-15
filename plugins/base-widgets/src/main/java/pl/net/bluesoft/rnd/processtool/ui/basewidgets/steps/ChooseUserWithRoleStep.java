package pl.net.bluesoft.rnd.processtool.ui.basewidgets.steps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.di.ObjectFactory;
import pl.net.bluesoft.rnd.processtool.model.BpmStep;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.roles.IUserRolesManager;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.util.StepUtil;

import java.util.Map;

/**
 * @author: "mpawlak@bluesoft.net.pl"
 */
@AliasName(name = "ChooseUserWithRoleStep")
public class ChooseUserWithRoleStep implements ProcessToolProcessStep
{
    @AutoWiredProperty
    private String roleName;

    @AutoWiredProperty
    private String assignePropertyName;

    @Autowired
    private IUserRolesManager userRolesManager;

    @Override
    public String invoke(BpmStep bpmStep, Map<String, String> params) throws Exception
    {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        ProcessInstance processInstance = bpmStep.getProcessInstance();
        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();

        UserData user = userRolesManager.getFirstUserWithRole(roleName);

        if(user == null)
            throw new RuntimeException("No user with role: "+roleName);

        processInstance.setSimpleAttribute(assignePropertyName, user.getLogin());

        return STATUS_OK;
    }
}
