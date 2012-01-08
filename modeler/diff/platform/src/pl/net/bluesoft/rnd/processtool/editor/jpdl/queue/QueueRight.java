package pl.net.bluesoft.rnd.processtool.editor.jpdl.queue;



public class QueueRight {

	private String roleName;
	private boolean browseAllowed;
	
	public QueueRight(){}
	
	public QueueRight(String roleName, boolean browseAllowed) {
		this.roleName = roleName;
		this.browseAllowed = browseAllowed;
	}
	
	public String getRoleName() {
		return roleName;
	}
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}
	public boolean isBrowseAllowed() {
		return browseAllowed;
	}
	public void setBrowseAllowed(boolean browseAllowed) {
		this.browseAllowed = browseAllowed;
	}
	
	@Override
	public String toString() {
		return "QueueRight [browseAllowed=" + browseAllowed + ", roleName="
				+ roleName + "]";
	}
	
}
