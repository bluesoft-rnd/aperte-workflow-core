package pl.net.bluesoft.rnd.pt.ext.stepeditor;

import java.util.HashMap;
import java.util.Map;

public class TaskConfig {
   
	private String taskName;
    private Map<String,Object> params = new HashMap<String,Object>();
	
    public String getTaskName() {
		return taskName;
	}
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}
	public Map<String, Object> getParams() {
		return params;
	}
	public void setParams(Map<String, Object> params) {
		this.params = params;
	}
	public void addParam(String s, Object o) {
		params.put(s, o);
	}
    
    
   
   
}
