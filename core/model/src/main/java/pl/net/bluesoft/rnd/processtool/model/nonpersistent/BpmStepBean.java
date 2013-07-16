package pl.net.bluesoft.rnd.processtool.model.nonpersistent;

import pl.net.bluesoft.rnd.processtool.model.BpmStep;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class BpmStepBean implements BpmStep, Serializable {
    private ProcessInstance processInstance;
    private List<String> outgoingTransitions = new ArrayList<String>();

    public void setProcessInstance(ProcessInstance processInstance) {
        this.processInstance = processInstance;
    }

    public void setOutgoingTransitions(List<String> outgoingTransitions) {
        this.outgoingTransitions = outgoingTransitions;
    }

    @Override
    public ProcessInstance getProcessInstance() {
        return processInstance;
    }

    @Override
    public List<String> getOutgoingTransitions() {
		throw new RuntimeException("Could not determine outgoing transitions for step");
    }
}
