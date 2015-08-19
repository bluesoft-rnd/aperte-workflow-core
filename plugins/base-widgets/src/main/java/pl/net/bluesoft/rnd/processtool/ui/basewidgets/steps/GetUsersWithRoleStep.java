package pl.net.bluesoft.rnd.processtool.ui.basewidgets.steps;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.BpmStep;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.roles.IUserRolesManager;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author: "mpawlak@bluesoft.net.pl"
 */
@AliasName(name = "GetUsersWithRoleStep")
public class GetUsersWithRoleStep implements ProcessToolProcessStep
{
    @AutoWiredProperty
    private String roleName;

    @AutoWiredProperty
    private String outputPropertyName;

    @Autowired
    private IUserRolesManager userRolesManager;

    @Override
    public String invoke(BpmStep bpmStep, Map<String, String> params) throws Exception
    {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        ProcessInstance processInstance = bpmStep.getProcessInstance();
        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();

        Collection<UserData> users = userRolesManager.getUsersByRole(roleName);

        if(users.isEmpty())
            return STATUS_OK;

        Set<String> logins = new HashSet<String>();
        for(UserData user: users)
            logins.add(user.getLogin());

        processInstance.setSimpleAttribute(outputPropertyName, StringUtils.join(logins, ","));

        return STATUS_OK;
    }
}
