package pl.net.bluesoft.rnd.processtool.model.nonpersistent;

import pl.net.bluesoft.rnd.processtool.model.BpmStep;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;

import java.util.ArrayList;
import java.util.List;

public class MutableBpmStep implements BpmStep {
    private String executionId;
    private ProcessInstance processInstance;
    private String stateName;
    private List<String> outgoingTransitions = new ArrayList<String>();

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public void setProcessInstance(ProcessInstance processInstance) {
        this.processInstance = processInstance;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }

    public void setOutgoingTransitions(List<String> outgoingTransitions) {
        this.outgoingTransitions = outgoingTransitions;
    }

    @Override
    public String getExecutionId() {
        return executionId;
    }

    @Override
    public ProcessInstance getProcessInstance() {
        return processInstance;
    }

    @Override
    public String getStateName() {
        return stateName;
    }

    @Override
    public List<String> getOutgoingTransitions() {
        return outgoingTransitions;
    }
}
