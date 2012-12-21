package pl.net.bluesoft.awf.ext.droolsstep;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.model.*;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AliasName;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.AutoWiredProperty;
import pl.net.bluesoft.util.lang.Strings;

import java.io.InputStream;
import java.util.*;

/**
 * @author tlipski@bluesoft.net.pl
 */
@AliasName(name = "DroolsStep")
public class DroolsStep implements ProcessToolProcessStep {
    @AutoWiredProperty
    private String ruleUrl;

    @AutoWiredProperty
    private String bundleResource;

	@Override
	public String invoke(BpmStep step, Map<String, String> params) throws Exception {
        ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
        DroolsUtils.DroolsResource resource;
        if (Strings.hasText(bundleResource)) {
            InputStream ruleStream = ctx.getRegistry().loadResource(bundleResource, ruleUrl);
            String fullRuleUrl = bundleResource.replace(".", "/") + "/" + ruleUrl;
            resource = new DroolsUtils.DroolsResource(fullRuleUrl, ruleStream);
        }
        else {
            if (ruleUrl.startsWith("/")) {
                ruleUrl = ctx.getSetting("drools.rules.baseurl") + ruleUrl;
            }
            resource = new DroolsUtils.DroolsResource(ruleUrl);
        }
        ProcessInstance processInstance = step.getProcessInstance();

        List facts = new ArrayList();
        facts.add(processInstance);
		for (ProcessInstanceAttribute attr : processInstance.getProcessAttributes()) {
			if (attr instanceof ProcessInstanceSimpleAttribute) {
                facts.add(attr);
            }
		}

        Map<String, Object> globals = new HashMap<String, Object>();
        HashMap resultMap = new HashMap();
        globals.put("result", resultMap);
        DroolsUtils.processRules(facts, globals, resource);
		String logEntryVal = (String) resultMap.get("logEntry");
		if (logEntryVal != null) {
			ProcessInstanceLog logEntry = new ProcessInstanceLog();
			logEntry.setEntryDate(Calendar.getInstance());
            logEntry.setLogType(ProcessInstanceLog.LOG_TYPE_INFO);
			//logEntry.setLogType(LogType.INFO);
            //TODO - process can be in a various states in that moment
//			logEntry.setState(ctx.getProcessDefinitionDAO().getProcessStateConfiguration(step.getStateName()));
			logEntry.setEventI18NKey(logEntryVal);
			logEntry.setOwnProcessInstance(processInstance);
			processInstance.getRootProcessInstance().addProcessLog(logEntry);
		}
		return (String) resultMap.get("value");
	}

    public String getRuleUrl() {
        return ruleUrl;
    }

    public void setRuleUrl(String ruleUrl) {
        this.ruleUrl = ruleUrl;
    }

    public String getBundleResource() {
        return bundleResource;
    }

    public void setBundleResource(String bundleResource) {
        this.bundleResource = bundleResource;
    }
}
