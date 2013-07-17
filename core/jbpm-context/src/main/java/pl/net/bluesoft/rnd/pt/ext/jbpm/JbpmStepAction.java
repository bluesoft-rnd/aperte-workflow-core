package pl.net.bluesoft.rnd.pt.ext.jbpm;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.ReturningProcessToolContextCallback;
import pl.net.bluesoft.rnd.processtool.dao.ProcessInstanceDAO;
import pl.net.bluesoft.rnd.processtool.model.BpmStep;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.BpmStepBean;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.PropertyAutoWiring;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static pl.net.bluesoft.rnd.processtool.plugins.ProcessToolRegistry.Util.getRegistry;

public class JbpmStepAction {
	public String invoke(final String processInstanceId, final String stepName, final Map<String, String> params) throws Exception {
        return getRegistry().withProcessToolContext(new ReturningProcessToolContextCallback<String>() {
            @Override
            public String processWithContext(ProcessToolContext ctx) {
                return doInvoke(processInstanceId, stepName, params, ctx);
            }
        });
	}

    private String doInvoke(String processInstanceId, String stepName, Map<String, String> params, ProcessToolContext ctx) {
        ProcessInstanceDAO dao = ctx.getProcessInstanceDAO();

        ProcessInstance pi = dao.getProcessInstanceByInternalId(processInstanceId);

        ProcessToolProcessStep stepInstance = ctx.getRegistry().getStep(stepName);

        if (stepInstance == null) {
            throw new IllegalArgumentException("No step defined by name: " + stepName);
        }

        String res;

        PropertyAutoWiring.autowire(stepInstance, params);
        BpmStep step = prepareStep(pi);

        try {
            res = stepInstance.invoke(step, params);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        ctx.updateContext(pi);
        return res;
    }

    private BpmStep prepareStep(ProcessInstance pi) {
		BpmStepBean step = new BpmStepBean();
		step.setProcessInstance(pi);
		List<String> transitionNames = new ArrayList<String>();
//		for (Transition transition : getActivity().getOutgoingTransitions()) {//TODO to nie ma sensu!
//			transitionNames.add(transition.getDestination().getName());
//		}
		step.setOutgoingTransitions(transitionNames);
		return step;
	}
}
