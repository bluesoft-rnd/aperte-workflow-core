package pl.net.bluesoft.rnd.processtool.bpm;

import pl.net.bluesoft.rnd.processtool.model.UserData;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class BpmTask {
	private UserData owner;
	private String taskName;
	private String internalTaskId;

	public BpmTask() {
	}

	public BpmTask(UserData owner, String taskName) {
		this.owner = owner;
		this.taskName = taskName;
	}

	public String getInternalTaskId() {
		return internalTaskId;
	}

	public void setInternalTaskId(String internalTaskId) {
		this.internalTaskId = internalTaskId;
	}

	public UserData getOwner() {
		return owner;
	}

	public void setOwner(UserData owner) {
		this.owner = owner;
	}

	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
}
