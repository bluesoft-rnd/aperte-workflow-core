package pl.net.bluesoft.rnd.processtool.ui.basewidgets.steps;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.bpm.ProcessToolBpmSession;
import pl.net.bluesoft.rnd.processtool.model.BpmStep;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.logging.Logger;

import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;

/**
 * Created with IntelliJ IDEA.
 * User: Rafał Surowiecki
 * Date: 17.06.14
 * Time: 14:00
 */
@AliasName(name = "GetStepDate")
public class GetStepDate implements ProcessToolProcessStep {
	@AutoWiredProperty
	private String stepName;

	@AutoWiredProperty
	private String attributeKey;

	@AutoWiredProperty
	private String dateFormat;

	@AutoWiredProperty
	private String required = "false";

	private final static Logger logger = Logger.getLogger(GetStepDate.class.getName());

	@Override
	public String invoke(BpmStep step, Map<String, String> params) throws Exception
	{
		if(stepName == null)
			return STATUS_ERROR;

		if(attributeKey == null)
			return STATUS_ERROR;

		if(dateFormat == null)
			return STATUS_ERROR;

		ProcessInstance pi = step.getProcessInstance();
		ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();

		ProcessToolBpmSession bpmSession = getRegistry().getProcessToolSessionFactory().createAutoSession();

		BpmTask task = bpmSession.getLastHistoryTaskByName(Long.parseLong(pi.getInternalId()), stepName);


		if(task == null)
			if(Boolean.parseBoolean(required))
				throw new RuntimeException("No task with given step name: "+stepName);
			else
				pi.setSimpleAttribute(attributeKey, "");
		else{
			Date finishDate = task.getFinishDate();
			if(finishDate == null){
				finishDate = new Date();
			}
			pi.setSimpleAttribute(attributeKey, new SimpleDateFormat(dateFormat).format(finishDate));
		}

		return STATUS_OK;
	}
}
