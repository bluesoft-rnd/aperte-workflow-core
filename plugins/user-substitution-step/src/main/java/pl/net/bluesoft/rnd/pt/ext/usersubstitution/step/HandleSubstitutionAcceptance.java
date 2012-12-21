package pl.net.bluesoft.rnd.pt.ext.usersubstitution.step;

import org.aperteworkflow.util.liferay.LiferayBridge;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.BpmStep;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.UserSubstitution;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.util.lang.Formats;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@AliasName(name = "HandleSubstitutionAcceptanceStep")
public class HandleSubstitutionAcceptance implements ProcessToolProcessStep {
    private final static Logger logger = Logger.getLogger(HandleSubstitutionAcceptance.class.getName());

    @Override
    public String invoke(BpmStep step, Map params) throws Exception {
        ProcessInstance processInstance = step.getProcessInstance();
        try {
            ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
            UserSubstitution userSubstitution = new UserSubstitution();
            userSubstitution.setUser(processInstance.getCreator());
            userSubstitution.setDateFrom(Formats.parseShortDate(processInstance.getSimpleAttributeValue("dateFrom")));
            userSubstitution.setDateTo(Formats.parseShortDate(processInstance.getSimpleAttributeValue("dateTo")));
            String substituteUserLogin = processInstance.getSimpleAttributeValue("userSubstitute");
            UserData substituteUser = ctx.getUserDataDAO().loadUserByLogin(substituteUserLogin);
            if (substituteUser == null) {
                substituteUser = LiferayBridge.getLiferayUser(substituteUserLogin,
                        processInstance.getCreator().getCompanyId());
                ctx.getUserDataDAO().saveOrUpdate(substituteUser);
                if (substituteUser == null) {
                    logger.warning("Unable to determine application user by login: " + substituteUserLogin);
                    return STATUS_ERROR;
                }
            }
            userSubstitution.setUserSubstitute(substituteUser);
            ctx.getUserSubstitutionDAO().saveOrUpdate(userSubstitution);
            logger.warning("Added substitution for user " + userSubstitution.getUser().getLogin());
            return STATUS_OK;
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            return STATUS_ERROR;
        }
    }
}
