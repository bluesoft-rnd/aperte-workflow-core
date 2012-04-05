package pl.net.bluesoft.rnd.processtool.ui.queues;

import java.util.ArrayList;
import java.util.Collection;

import pl.net.bluesoft.rnd.processtool.model.config.ProcessQueueRight;

public class QueueBean {

	private String process;
	private String name;
	private String description;
	private Collection<ProcessQueueRight> rights = new ArrayList<ProcessQueueRight>();
	
	public void setProcess(String process) {
		this.process = process;
	}
	public String getProcess() {
		return process;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getDescription() {
		return description;
	}
	public void setRights(Collection<ProcessQueueRight> rights) {
		this.rights = rights;
	}
	public Collection<ProcessQueueRight> getRights() {
		return rights;
	}
	
}
