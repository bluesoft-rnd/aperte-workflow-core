package pl.net.bluesoft.rnd.processtool.model.nonpersistent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

	public ProcessQueueBean(ProcessQueue queue) {
		this.name = queue.getName();
		this.description = queue.getDescription();
		this.browsable = queue.isBrowsable();
		this.processCount = queue.getProcessCount();
		this.userAdded = queue.getUserAdded();
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

	public static List<ProcessQueueBean> asBeans(List<? extends ProcessQueue> list) {
		List<ProcessQueueBean> result = new ArrayList<ProcessQueueBean>();

		for (ProcessQueue processQueue : list) {
			result.add(new ProcessQueueBean(processQueue));
		}
		return result;
	}
}