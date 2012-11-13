package pl.net.bluesoft.rnd.processtool.bpm;

import pl.net.bluesoft.rnd.processtool.event.IEvent;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;
import pl.net.bluesoft.rnd.processtool.model.UserData;

/**
 * BPM event, e.g. new instance or user action
 *
 * @author tlipski@bluesoft.net.pl, mpawlak@bluesoft.net.pl
 */
public class BpmEvent implements IEvent {

	public enum Type implements TypeMarker{
		NEW_PROCESS, 
		SIGNAL_PROCESS, 
		
		/** Task has been assigned to new person */
		ASSIGN_TASK, 
		
		/** Task has been finished */
		TASK_FINISHED,
		
		/** Event published when process is halted for example when subprocess has been created */
		PROCESS_HALTED, 
		
		/** Process is marked as FINISHED and no other actions will be performed */
		END_PROCESS
	}

	private Type eventType;
	private ProcessInstance processInstance;
    private BpmTask task;
	private UserData userData;

	public BpmEvent(Type eventType, ProcessInstance processInstance, UserData userData) {
		this.eventType = eventType;
		this.processInstance = processInstance;
		this.userData = userData;
	}

	public BpmEvent(Type eventType, BpmTask task, UserData userData) {
        this.eventType = eventType;
        this.processInstance = task.getProcessInstance();
        this.userData = userData;
        this.task = task;
	}

    public BpmTask getTask() {
        return task;
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

	@Override
	public String toString() {
		return "BpmEvent [eventType=" + eventType + ", processInstance=" + processInstance + ", task=" + task + ", userData=" + userData + "]";
	}
}
