package pl.net.bluesoft.rnd.processtool.model.nonpersistent;

import java.io.Serializable;

/**
 * User: POlszewski
 * Date: 2013-06-24
 * Time: 15:44
 */
public class ProcessQueueBean implements ProcessQueue, Serializable {
	private String name;
	private String description;
	private boolean browsable=false;
	private int processCount;
	/**
	 * Added by user in queues portlet
	 */
	private boolean userAdded = false;

	public ProcessQueueBean() {
	}

	public ProcessQueueBean(String name, String description, boolean browsable) {
		this.name = name;
		this.description = description;
		this.browsable = browsable;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public boolean isBrowsable() {
		return browsable;
	}

	public void setBrowsable(boolean browsable) {
		this.browsable = browsable;
	}

	@Override
	public int getProcessCount() {
		return processCount;
	}

	public void setProcessCount(int processCount) {
		this.processCount = processCount;
	}

	@Override
	public String getDescription() {
		return description != null ? description : name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setUserAdded(boolean userAdded) {
		this.userAdded = userAdded;
	}

	@Override
	public boolean getUserAdded() {
		return userAdded;
	}
}