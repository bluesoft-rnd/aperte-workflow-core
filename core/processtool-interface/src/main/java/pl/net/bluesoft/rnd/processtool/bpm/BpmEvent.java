package pl.net.bluesoft.rnd.processtool.bpm;

import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;

import java.io.Serializable;

/**
 * BPM event, e.g. new instance or user action
 *
 * @author tlipski@bluesoft.net.pl
 */
public class BpmEvent implements Serializable {

	public enum Type {
		NEW_PROCESS, SIGNAL_PROCESS, ASSIGN_PROCESS, END_PROCESS
	}

	private Type eventType;
	private ProcessInstance processInstance;
	private UserData userData;

	public BpmEvent(Type eventType, ProcessInstance processInstance, UserData userData) {
		this.eventType = eventType;
		this.processInstance = processInstance;
		this.userData = userData;
	}

	public BpmEvent() {
	}

	public Type getEventType() {
		return eventType;
	}

	public void setEventType(Type eventType) {
		this.eventType = eventType;
	}

	public ProcessInstance getProcessInstance() {
		return processInstance;
	}

	public void setProcessInstance(ProcessInstance processInstance) {
		this.processInstance = processInstance;
	}

	public UserData getUserData() {
		return userData;
	}

	public void setUserData(UserData userData) {
		this.userData = userData;
	}
}
