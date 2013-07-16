package pl.net.bluesoft.rnd.processtool.model;

import java.util.List;

public interface BpmStep {
	ProcessInstance getProcessInstance();

	List<String> getOutgoingTransitions();
}
