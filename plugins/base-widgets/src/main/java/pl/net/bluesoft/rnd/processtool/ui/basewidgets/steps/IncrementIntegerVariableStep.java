package pl.net.bluesoft.rnd.processtool.ui.basewidgets.steps;


import org.apache.commons.lang3.StringUtils;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.BpmStep;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.util.StepUtil;

import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@AliasName(name = "IncrementIntegerVariableStep")
public class IncrementIntegerVariableStep implements ProcessToolProcessStep {
	
	@AutoWiredProperty
	private String variable;

	@AutoWiredProperty
	private String root = "false";

	private final static Logger logger = Logger.getLogger(IncrementIntegerVariableStep.class.getName());

    @Override
    public String invoke(BpmStep step, Map<String, String> params) throws Exception
    {
        ProcessInstance parent = step.getProcessInstance().getParent();
    	ProcessInstance pi = step.getProcessInstance();

        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();

    	if(Boolean.parseBoolean(root))
            parent = pi.getRootProcessInstance();

    	if(variable == null)
    		return STATUS_ERROR;

        String variableValue = StepUtil.extractVariable(variable, ctx ,pi);

        Integer value = Integer.parseInt(variableValue);

        value = value + 1;

        pi.setSimpleAttribute(variable, value.toString());


    	return STATUS_OK;
    }


}
