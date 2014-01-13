package pl.net.bluesoft.rnd.processtool.ui.basewidgets.steps;


import pl.net.bluesoft.rnd.processtool.model.BpmStep;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;

import java.util.Map;
import java.util.logging.Logger;

@AliasName(name = "CopyVariablesStep")
public class CopyVariablesStep implements ProcessToolProcessStep {
	
	@AutoWiredProperty
	private String variables;
	
	@AutoWiredProperty
	private String root = "false";

    @AutoWiredProperty
    private String toParentProcess = "false";

	private final static Logger logger = Logger.getLogger(CopyVariablesStep.class.getName());

    @Override
    public String invoke(BpmStep step, Map<String, String> params) throws Exception
    {
        ProcessInstance parent = step.getProcessInstance().getParent();
    	ProcessInstance pi = step.getProcessInstance();

    	if(Boolean.parseBoolean(root))
            parent = pi.getRootProcessInstance();

    	if(variables == null)
    		return STATUS_ERROR;

        if(Boolean.parseBoolean(toParentProcess))
            copyVariables(pi, parent);
        else
            copyVariables(parent, pi);
    	return STATUS_OK;
    }

    private void copyVariables(ProcessInstance fromProcessInstance, ProcessInstance toProcessInstance)
    {
        String[] variablesNames = variables.split("[,;]");
        for(String variableKey : variablesNames)
        {
            String variableValue = fromProcessInstance.getSimpleAttributeValue(variableKey);
            toProcessInstance.setSimpleAttribute(variableKey, variableValue);

        }
    }


}
