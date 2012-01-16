package pl.net.bluesoft.rnd.processtool.editor.jpdl.object;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class JPDLTask extends JPDLComponent {
  
	protected String taskType;
	
	protected static final String TASK_X = "100";
	protected static final String TASK_Y = "50";
	
	public String getTaskType() {
		return taskType;
	}

	public void setTaskType(String taskType) {
		this.taskType = taskType;
	}
	
	@Override
	public void fillBasicProperties(JSONObject json) throws JSONException {
		super.fillBasicProperties(json);
		taskType = json.getJSONObject("properties").getString("tasktype");
	}
}
