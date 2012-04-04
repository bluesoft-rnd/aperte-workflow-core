package pl.net.bluesoft.rnd.processtool.model.nonpersistent;

import pl.net.bluesoft.rnd.processtool.model.ProcessInstance;

import java.util.Collection;

/**
 * Non-persistent Process Queue data
 *
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessQueue implements java.io.Serializable {

	private String name;
	private String description;
	private boolean browsable=false;
	private long processCount;
	/**
	 * Added by user in queues portlet
	 */
	private Boolean userAdded = false;

	private Collection<ProcessInstance> processes;

	public ProcessQueue() {
	}

	public ProcessQueue(String name, String description, boolean browsable) {
		this.name = name;
		this.description = description;
		this.browsable = browsable;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isBrowsable() {
		return browsable;
	}

	public void setBrowsable(boolean browsable) {
		this.browsable = browsable;
	}

	public long getProcessCount() {
		return processCount;
	}

	public void setProcessCount(long processCount) {
		this.processCount = processCount;
	}

	public Collection<ProcessInstance> getProcesses() {
		return processes;
	}

	public void setProcesses(Collection<ProcessInstance> processes) {
		this.processes = processes;
	}

	public String getDescription() {
		return description != null ? description : name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setUserAdded(Boolean userAdded) {
		this.userAdded = userAdded;
	}

	public Boolean getUserAdded() {
		return userAdded;
	}
}
