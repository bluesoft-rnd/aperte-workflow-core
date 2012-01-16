package pl.net.bluesoft.rnd.processtool.editor.jpdl.queue;

import java.util.SortedMap;
import java.util.TreeMap;

public class QueueDef {
	
	private String name = "";
	private String description = "";
	private SortedMap<Integer,QueueRight> rights = new TreeMap<Integer,QueueRight>();
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public SortedMap<Integer,QueueRight> getRights() {
		return rights;
	}
	public void setRights(SortedMap<Integer,QueueRight> rights) {
		this.rights = rights;
	}
	public void addRight(Integer tableId, QueueRight right) {
		rights.put(tableId, right);
	}
	public void clearRights() {
		rights.clear();
	}
	
	@Override
	public String toString() {
		return "QueueDef [description=" + description + ", name=" + name
				+ ", rights=" + rights + "]";
	}
	
}
