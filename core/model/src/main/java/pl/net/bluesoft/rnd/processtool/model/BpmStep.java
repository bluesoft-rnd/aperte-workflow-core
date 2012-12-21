package pl.net.bluesoft.rnd.processtool.model;

import java.io.Serializable;
import java.util.List;

public interface BpmStep extends Serializable {
    String getExecutionId();

    ProcessInstance getProcessInstance();

    String getStateName();

    List<String> getOutgoingTransitions();
}
