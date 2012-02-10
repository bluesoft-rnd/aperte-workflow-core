package pl.net.bluesoft.awf.ext.droolsstep;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceAttribute;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceLog;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstanceSimpleAttribute;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;

import java.util.*;

/**
 * @author tlipski@bluesoft.net.pl
 */
@AliasName(name = "DroolsStep")
public class DroolsStep implements ProcessToolProcessStep {

    @AutoWiredProperty
    private String ruleUrl;

    @Override
	public String invoke(ProcessInstance processInstance, Map params) throws Exception {

		ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
        if (ruleUrl.startsWith("/")) {
	        ruleUrl = ctx.getSetting("drools.rules.baseurl") +
                    ruleUrl;
        }
        List facts = new ArrayList();
        facts.add(processInstance);
		for (ProcessInstanceAttribute attr : processInstance.getProcessAttributes()) {
			if (attr instanceof ProcessInstanceSimpleAttribute) //drools has to load the class again, does not use instance.getClass() - why?
				facts.add(attr);
		}

        Map<String, Object> globals = new HashMap<String, Object>();
        HashMap resultMap = new HashMap();
        globals.put("result", resultMap);
        DroolsUtils.processRules(facts,globals, ruleUrl);
		String logEntryVal = (String) resultMap.get("logEntry");
		if (logEntryVal != null) {
			ProcessInstanceLog logEntry = new ProcessInstanceLog();
			logEntry.setEntryDate(Calendar.getInstance());
			logEntry.setLogType(ProcessInstanceLog.LOG_TYPE_INFO);
			logEntry.setState(ctx.getProcessDefinitionDAO().getProcessStateConfiguration(processInstance));
			logEntry.setEventI18NKey(logEntryVal);
			processInstance.addProcessLog(logEntry);
		}
		return (String) resultMap.get("value");
	}
    public String getRuleUrl() {
        return ruleUrl;
    }

    public void setRuleUrl(String ruleUrl) {
        this.ruleUrl = ruleUrl;
    }
}
