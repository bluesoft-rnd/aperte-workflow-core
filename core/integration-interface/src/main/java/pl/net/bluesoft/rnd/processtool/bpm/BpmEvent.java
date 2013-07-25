package pl.net.bluesoft.rnd.processtool.bpm;

import pl.net.bluesoft.rnd.processtool.event.IEvent;
import pl.net.bluesoft.rnd.processtool.model.BpmTask;
import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;

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

	private final Type eventType;
	private final ProcessInstance processInstance;
    private final BpmTask task;
	private final String userLogin;

	public BpmEvent(Type eventType, ProcessInstance processInstance, BpmTask task, String userLogin) {
		this.eventType = eventType;
		this.processInstance = processInstance;
		this.task = task;
		this.userLogin = userLogin;
	}

	public BpmEvent(Type eventType, ProcessInstance processInstance, String userLogin) {
		this(eventType, processInstance, null, userLogin);
	}

	public BpmEvent(Type eventType, BpmTask task, String userLogin) {
        this(eventType, task.getProcessInstance(), task, userLogin);
	}

    public BpmTask getTask() {
        return task;
    }

	@Override
	public Type getEventType() {
		return eventType;
	}

	public ProcessInstance getProcessInstance() {
		return processInstance;
	}

	public String getUserLogin() {
		return userLogin;
	}

	@Override
	public String toString() {
		return "BpmEvent [eventType=" + eventType + ", processInstance=" + processInstance + ", task=" + task + ", userLogin=" + userLogin + ']';
	}
}
