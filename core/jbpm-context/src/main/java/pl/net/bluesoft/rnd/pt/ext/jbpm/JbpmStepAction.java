package pl.net.bluesoft.rnd.pt.ext.jbpm;

import org.jbpm.api.model.Transition;
import org.jbpm.pvm.internal.model.ExecutionImpl;
import org.jbpm.pvm.internal.wire.WireContext;
import pl.net.bluesoft.rnd.processtool.ProcessToolContext;
import pl.net.bluesoft.rnd.processtool.dao.ProcessInstanceDAO;
import pl.net.bluesoft.rnd.processtool.model.BpmStep;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.nonpersistent.MutableBpmStep;
import pl.net.bluesoft.rnd.processtool.steps.ProcessToolProcessStep;
import pl.net.bluesoft.rnd.processtool.ui.widgets.annotations.PropertyAutoWiring;
import pl.net.bluesoft.util.lang.TaskWatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JbpmStepAction {
    private WireContext context;
    private String processInstanceId;
    private String stepName;
    private Map<String, String> params = new HashMap<String, String>();

    private static final Logger logger = Logger.getLogger(JbpmStepAction.class.getName());

    public String invoke() throws Exception {
    	final TaskWatch watch = new TaskWatch(this.getClass().getSimpleName() + ": " + stepName);
    	String res = watch.watchTask("total step processing", new Callable<String>() {
			@Override
			public String call() throws Exception {
		        return internalInvoke(watch);
			}

		});
    	
    	watch.stopAll();
    	logger.log(Level.INFO, watch.printSummary());

        return res;
    }

    private BpmStep prepareStep(ProcessInstance pi) {
        ExecutionImpl exec = context.getScopeInstance().getExecution();
        MutableBpmStep step = new MutableBpmStep();
        step.setProcessInstance(pi);
        step.setExecutionId(exec.getId());
        step.setStateName(exec.getActivityName());
        List<String> transitionNames = new ArrayList<String>();
        for (Transition transition : exec.getActivity().getOutgoingTransitions()) {
            transitionNames.add(transition.getDestination().getName());
        }
        step.setOutgoingTransitions(transitionNames);
        return step;
    }

	public String internalInvoke(final TaskWatch watch) {
		ProcessToolContext ctx = ProcessToolContext.Util.getThreadProcessToolContext();
		ProcessInstanceDAO dao = ctx.getProcessInstanceDAO();
		ProcessInstance pi = dao.getProcessInstance(Long.parseLong(processInstanceId));
		if (pi.getInternalId() == null) {
		    pi.setInternalId(context.getScopeInstance().getExecution().getProcessInstance().getId());
		    dao.saveProcessInstance(pi);
		}

		final ProcessToolProcessStep stepInstance = ctx.getRegistry().getStep(stepName);
		if (stepInstance == null) {
		    throw new IllegalArgumentException("No step defined by name: " + stepName);
		}
		String res;
		try {
		    PropertyAutoWiring.autowire(stepInstance, params);
		    final BpmStep step = prepareStep(pi);
		    res = watch.watchTask("actual step execution", new Callable<String>() {
				@Override
				public String call() throws Exception {
					return stepInstance.invoke(step, params);
				}
		    });
		} catch (Exception e) {
		    throw new RuntimeException(e);
		}
		ctx.updateContext(pi);
		return res;
	}
}
