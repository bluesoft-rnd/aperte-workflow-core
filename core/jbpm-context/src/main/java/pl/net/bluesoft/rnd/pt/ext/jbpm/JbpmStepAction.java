package pl.net.bluesoft.rnd.pt.ext.jbpm;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContextFactory.ExecutionType;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.dao.ProcessInstanceDAO;
import pl.net.bluesoft.rnd.processtool.model.BpmStep;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.BpmStepBean;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.PropertyAutoWiring;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;

public class JbpmStepAction {
	private static final Logger LOGGER = Logger.getLogger(JbpmStepAction.class.getName());

	public String invoke(String processInstanceId, String stepName) throws Exception {
		return invoke(processInstanceId, stepName, new HashMap<String, String>());
	}
	
	public String invoke(final String processInstanceId, final String stepName, final Map<String, String> params) throws Exception {
		long start = System.currentTimeMillis();
		try {
        return getRegistry().withProcessToolContext(new ReturningProcessToolContextCallback<String>() {
            @Override
            public String processWithContext(ProcessToolContext ctx) {
                return doInvoke(processInstanceId, stepName, params != null ? params : Collections.<String, String>emptyMap(), ctx);
            }
        }, ExecutionType.TRANSACTION);
		}
		finally {
			LOGGER.finest("[invoke] stepName=" + stepName + " t=" + (System.currentTimeMillis() - start));
		}
	}

    private String doInvoke(String processInstanceId, String stepName, Map<String, String> params, ProcessToolContext ctx) {
        ProcessInstanceDAO dao = ctx.getProcessInstanceDAO();

        ProcessInstance pi = dao.getProcessInstanceByInternalId(processInstanceId);

        ProcessToolProcessStep stepInstance = getRegistry().getGuiRegistry().createStep(stepName);

        if (stepInstance == null) {
            throw new IllegalArgumentException("No step defined by name: " + stepName);
        }

		PropertyAutoWiring.autowire(stepInstance, params, pi);
        BpmStep step = prepareStep(pi);
		String res;

		try {
            res = stepInstance.invoke(step, params);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return res;
    }

    private BpmStep prepareStep(ProcessInstance pi) {
		BpmStepBean step = new BpmStepBean();
		step.setProcessInstance(pi);
		return step;
	}
}
