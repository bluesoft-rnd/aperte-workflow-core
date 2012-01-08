package pl.net.bluesoft.rnd.processtool.editor.jpdl.object;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class JPDLComponent extends JPDLObject {

	protected Map<String, JPDLTransition> outgoing = new HashMap<String, JPDLTransition>();
	
	public Map<String, JPDLTransition> getOutgoing() {
		return outgoing;
	}

	public void setOutgoing(Map<String, JPDLTransition> outgoing) {
		this.outgoing = outgoing;
	}
	
	public JPDLTransition getTransition(String resourceId) {
		return outgoing.get(resourceId);
	}
	
	public void putTransition(String resourceId, JPDLTransition transition) {
		outgoing.put(resourceId, transition);
	}
	
	public abstract String toXML();
	
	public void fillBasicProperties(JSONObject json) throws JSONException {
		super.fillBasicProperties(json);
		
		JSONArray array = json.getJSONArray("outgoing");
		
		for (int i = 0; i < array.length(); i++) {
			JSONObject arrObj = array.getJSONObject(i);
			outgoing.put(arrObj.getString("resourceId"), null);
		}
	}
	
	protected String getTransitionsXML() {
		StringBuffer sb = new StringBuffer();
		
		for (String targetResourceId : outgoing.keySet()) {
			JPDLTransition transition = outgoing.get(targetResourceId);
			sb.append(String.format("<transition name=\"%s\" to=\"%s\">\n", transition.getName(), transition.getTargetName()));
			if (transition.getCondition() != null && transition.getCondition().trim().length() > 0) {
				sb.append(String.format("<condition expr=\"#{%s}\"/>\n", transition.getCondition()));
			}
			//sb.append(String.format("<description>Original ID: '%s'</description>\n", transition.getResourceId()));
			sb.append("</transition>\n");
		}
		
		return sb.toString();
	}
}
