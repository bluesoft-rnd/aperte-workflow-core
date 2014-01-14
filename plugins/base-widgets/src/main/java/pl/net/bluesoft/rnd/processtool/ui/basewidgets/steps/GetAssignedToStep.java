package pl.net.bluesoft.rnd.processtool.ui.basewidgets.steps;


import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.BpmStep;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;

import java.util.Map;
import java.util.logging.Logger;

import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;

/**
 * Get last assigned person to task with given name
 */
@AliasName(name = "GetAssignedToStep")
public class GetAssignedToStep implements ProcessToolProcessStep {
	
	@AutoWiredProperty
	private String stepName;

    @AutoWiredProperty
    private String attributeKey;

    @AutoWiredProperty
    private String required = "false";

	private final static Logger logger = Logger.getLogger(GetAssignedToStep.class.getName());

    @Override
    public String invoke(BpmStep step, Map<String, String> params) throws Exception
    {
        if(stepName == null)
            return STATUS_ERROR;

        if(attributeKey == null)
            return STATUS_ERROR;

    	ProcessInstance pi = step.getProcessInstance();
        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();

        //TODO future architecture refactor
        ProcessToolBpmSession bpmSession = getRegistry().getProcessToolSessionFactory().createAutoSession();


        BpmTask task = bpmSession.getLastHistoryTaskByName(Long.parseLong(pi.getInternalId()), stepName);

        if(task == null)
            if(Boolean.parseBoolean(required))
                throw new RuntimeException("No task with given step name: "+stepName);
            else
                pi.setSimpleAttribute(attributeKey, "");
        else
            pi.setSimpleAttribute(attributeKey, task.getAssignee());

    	return STATUS_OK;
    }
}
