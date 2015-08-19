package pl.net.bluesoft.rnd.processtool.ui.basewidgets.steps;


import pl.net.bluesoft.rnd.processtool.model.BpmStep;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.util.StepUtil;

import java.util.Map;
import java.util.logging.Logger;

@AliasName(name = "SetVariablesStep")
public class SetVariablesStep implements ProcessToolProcessStep {
	
	@AutoWiredProperty(required = true)
	private String query;
	
	@AutoWiredProperty
	private String applyToRoot;

	private final static Logger logger = Logger.getLogger(SetVariablesStep.class.getName());

    @Override
    public String invoke(BpmStep step, Map<String, String> params) throws Exception
    {
		if(query == null) {
			return STATUS_ERROR;
		}

    	ProcessInstance pi = step.getProcessInstance();

    	if("true".equals(applyToRoot)) {
			pi = pi.getRootProcessInstance();
		}

		Map<String, String> map = StepUtil.evaluateQuery(query, pi);

		for (Map.Entry<String, String> entry : map.entrySet()) {
			pi.setSimpleAttribute(entry.getKey(), entry.getValue());
		}

    	return STATUS_OK;
    }
}
