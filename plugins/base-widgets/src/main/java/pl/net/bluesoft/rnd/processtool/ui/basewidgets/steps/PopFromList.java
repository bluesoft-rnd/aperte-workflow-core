package pl.net.bluesoft.rnd.processtool.ui.basewidgets.steps;


import org.apache.commons.lang3.StringUtils;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.BpmStep;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.rnd.util.StepUtil;

import java.util.Arrays;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@AliasName(name = "PopFromList")
public class PopFromList implements ProcessToolProcessStep {
	
	@AutoWiredProperty
	private String variable;

    @AutoWiredProperty
    private String splitChar;

    @AutoWiredProperty
    private String toVariable;

	@AutoWiredProperty
	private String root = "false";

	private final static Logger logger = Logger.getLogger(PopFromList.class.getName());

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

        String[] variablesNames = variableValue.split(Pattern.quote(splitChar));

        Arrays.sort(variablesNames);

        String[] variablesNamesNew = new String[variablesNames.length - 1];

        for(int index=0;index< variablesNames.length - 1; index++)
            variablesNamesNew[index] = variablesNames[index];

        /* Get last element */
        pi.setSimpleAttribute(toVariable, variablesNames[variablesNames.length-1]);

        String newVariableValue = StringUtils.join(variablesNamesNew, Pattern.quote(splitChar));
        pi.setSimpleAttribute(variable, newVariableValue);


    	return STATUS_OK;
    }

}
