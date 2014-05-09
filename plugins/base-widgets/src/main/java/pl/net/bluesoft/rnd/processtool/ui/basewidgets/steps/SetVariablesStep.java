package pl.net.bluesoft.rnd.processtool.ui.basewidgets.steps;


import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
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

    	ProcessInstance pi = step.getProcessInstance();

    	if("true".equals(applyToRoot))
    		pi = pi.getRootProcessInstance();

    	if(query == null)
    		return STATUS_ERROR;

    	String[] parts = query.split("[,;]");
    	for(String part : parts){
    		String[] assignment = part.split("[:=]");
    		if(assignment.length != 2)
    			continue;
    		
    		if(assignment[1].startsWith("\"") && assignment[1].endsWith("\""))
    			assignment[1] = assignment[1].substring(1, assignment[1].length() - 1);

            String key = assignment[0];
            String value = assignment[1];

			if (value.startsWith("#{") && value.endsWith("}")) {
				value = StepUtil.extractVariable(value, ProcessToolContext.Util.getThreadProcessToolContext(), pi);
			}

            pi.setSimpleAttribute(key, value);

    	}
    	return STATUS_OK;
    }

	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

}
