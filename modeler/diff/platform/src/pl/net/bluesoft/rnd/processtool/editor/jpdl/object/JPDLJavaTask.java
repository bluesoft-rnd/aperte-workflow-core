package pl.net.bluesoft.rnd.processtool.editor.jpdl.object;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import pl.net.bluesoft.rnd.processtool.editor.Util;

public class JPDLJavaTask extends JPDLTask {
	
	private Map<String,String> stepDataMap = new HashMap<String,String>();
	
	protected JPDLJavaTask() {
		
	}
	
	@Override
	public String toXML() { 
		StringBuffer sb = new StringBuffer();
		sb.append(String.format("<java auto-wire=\"true\" cache=\"false\" class=\"pl.net.bluesoft.rnd.pt.ext.jbpm.JbpmStepAction\" g=\"%s,%s,%s,%s\" method=\"invoke\" name=\"%s\" var=\"result\">\n", x1,y1,TASK_X,TASK_Y,name));
		sb.append("<field name=\"stepName\">\n");
		sb.append(String.format("<string value=\"%s\"/>\n",taskType));
		sb.append("</field>\n");
		sb.append("<field name=\"params\">\n");
		sb.append("<map>\n");
		if (!stepDataMap.isEmpty()) {
			for (String key : stepDataMap.keySet()) {
				sb.append("<entry>\n");
				sb.append("<key>\n");
				sb.append(String.format("<string value=\"%s\"/>\n", key));
				sb.append("</key>\n");
				sb.append("<value>\n");
				sb.append(String.format("<string value=\"%s\"/>\n", stepDataMap.get(key)));
				sb.append("</value>\n");
				sb.append("</entry>\n");
			}
		}
		sb.append("</map>\n");
		sb.append("</field>\n");
		sb.append(getTransitionsXML());
		sb.append("</java>\n");
		return sb.toString();

    }

	@Override
	public void fillBasicProperties(JSONObject json) throws JSONException {
		super.fillBasicProperties(json);
		String stepDataJson = json.getJSONObject("properties").getString("aperte-conf");
		if (stepDataJson != null && stepDataJson.trim().length() != 0) {
		  stepDataJson = Util.replaceXmlEscapeCharacters(stepDataJson);
		  JSONObject stepDataJsonObj = new JSONObject(stepDataJson);
		  Iterator i = stepDataJsonObj.keys();
		  while(i.hasNext()) {
			String key = (String)i.next();
			String value = stepDataJsonObj.getString(key);
			stepDataMap.put(key, value);
		  }
		}
	}
	
}
