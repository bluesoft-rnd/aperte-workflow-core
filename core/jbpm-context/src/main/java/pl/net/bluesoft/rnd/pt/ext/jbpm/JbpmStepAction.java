package pl.net.bluesoft.rnd.pt.ext.jbpm;

import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.dao.ProcessInstanceDAO;
import pl.net.bluesoft.rnd.processtool.model.BpmStep;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.BpmStepBean;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.PropertyAutoWiring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class JbpmStepAction {
	public String invoke(String processInstanceId, String stepName, Map<String, String> params) throws Exception {
		ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
		ProcessInstanceDAO dao = ctx.getProcessInstanceDAO();
		ProcessInstance pi = dao.getProcessInstance(Long.parseLong(processInstanceId));

		ProcessToolProcessStep stepInstance = ctx.getRegistry().getStep(stepName);

		if (stepInstance == null) {
			throw new IllegalArgumentException("No step defined by name: " + stepName);
		}

		String res;
//		try {
			PropertyAutoWiring.autowire(stepInstance, params);
			BpmStep step = prepareStep(pi);
			res = stepInstance.invoke(step, params);
//		}
//		catch (Exception e) {
//			throw new RuntimeException(e);
//		}
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
