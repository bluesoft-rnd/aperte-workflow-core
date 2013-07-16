package pl.net.bluesoft.rnd.processtool.ui.basewidgets.steps;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.BpmStep;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;
import pl.net.bluesoft.rnd.processtool.model.config.ProcessStateAction;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.util.StepUtil;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;
import static pl.net.bluesoft.util.lang.cquery.CQuery.from;
import static pl.net.bluesoft.util.lang.cquery.Predicates.eq;

/**
 * User: POlszewski
 * Date: 2012-03-05
 * Time: 11:00
 */
@AliasName(name = "FinishTasksStep")
public class FinishTasksStep implements ProcessToolProcessStep {
    private static final Logger logger = Logger.getLogger(FinishTasksStep.class.getSimpleName());

    @AutoWiredProperty
    private String userLoginToFinishTask;

    @AutoWiredProperty
    private String taskNames;
    @AutoWiredProperty
    private String actionToPerform;

    @Override
    public String invoke(BpmStep bpmStep, Map<String, String> params) throws Exception
    {
        try
        {
            finishTask(bpmStep.getProcessInstance());
            return STATUS_OK;
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            return STATUS_ERROR;
        }
    }

    private void finishTask(ProcessInstance processInstance)
    {
        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();

        String attributeKey = StepUtil.extractVariable(userLoginToFinishTask, ctx, processInstance);

        UserData user = ctx.getUserDataDAO().loadUserByLogin(attributeKey);

        ProcessToolBpmSession bpmSession = getRegistry().getProcessToolSessionFactory().createSession(user);
        Set<String> allowedTaskNames = from(taskNames.split(",")).toSet();

        List<BpmTask> tasks = bpmSession.findProcessTasks(processInstance);
        for (BpmTask task : tasks)
        {
            if (allowedTaskNames.contains(task.getTaskName())) {
                Set<ProcessStateAction> actions = ctx.getProcessDefinitionDAO().getProcessStateConfiguration(task)
                        .getActions();

                ProcessStateAction action = from(actions).first(eq("bpmName", actionToPerform));
                bpmSession.performAction(action, task);
                logger.info("Finished user "+user.getLogin()+" task ["+task.getTaskName()+"]");
            }
        }
    }
}
