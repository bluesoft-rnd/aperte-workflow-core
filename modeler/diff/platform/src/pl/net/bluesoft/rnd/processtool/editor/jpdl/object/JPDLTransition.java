package pl.net.bluesoft.rnd.processtool.editor.jpdl.object;

import org.json.JSONException;
import org.json.JSONObject;

public class JPDLTransition extends JPDLObject {
  
	protected JPDLTransition() {
		
	}
	
	private String target;
    private String targetName;
    
    //dla akcji-przyciskow
    private String skipSaving;
    private String autoHide;
    private String priority;
    
    //dla 'decision'
    private String condition;
	
	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}
	
	public String getTargetName() {
		return targetName;
	}

	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	public String getSkipSaving() {
		return skipSaving;
	}

	public void setSkipSaving(String skipSaving) {
		this.skipSaving = skipSaving;
	}

	public String getAutoHide() {
		return autoHide;
	}

	public void setAutoHide(String autoHide) {
		this.autoHide = autoHide;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public void fillBasicProperties(JSONObject json) throws JSONException {
		super.fillBasicProperties(json);
		target = json.getJSONObject("target").getString("resourceId");
		skipSaving = json.getJSONObject("properties").getString("skip-saving");
		autoHide = json.getJSONObject("properties").getString("auto-hide");
		priority = json.getJSONObject("properties").getString("priority");
		condition = json.getJSONObject("properties").getString("conditionexpression");
	}
	
	

	
}
