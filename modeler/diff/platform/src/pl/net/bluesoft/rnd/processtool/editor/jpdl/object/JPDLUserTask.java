package pl.net.bluesoft.rnd.processtool.editor.jpdl.object;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pl.net.bluesoft.rnd.processtool.editor.Util;
import pl.net.bluesoft.rnd.processtool.editor.Widget;

public class JPDLUserTask extends JPDLTask {
  
	
	protected JPDLUserTask() {
	  
    }
	

	
	private Widget widget = null;
	private String commentary; 
	private String widgetName;
	
	private String assignee;
	private String swimlane;
	private String candidateGroups;
	
	
	public Widget getWidget() {
		return widget;
	}

	@Override
	public String toXML() { 
		StringBuffer sb = new StringBuffer();
		String taskConf = "";
		if (assignee != null && assignee.trim().length() > 0)
			taskConf = String.format("assignee=\"%s\"", assignee);
		else if (candidateGroups != null && candidateGroups.trim().length() > 0)
			taskConf = String.format("candidate-groups=\"%s\"", candidateGroups);
		else if (swimlane != null && swimlane.trim().length() > 0)
			taskConf = String.format("swimlane=\"%s\"", swimlane);
		
		sb.append(String.format("<task %s name=\"%s\" g=\"%s,%s,%s,%s\">\n", taskConf,name,x1,y1,TASK_X,TASK_Y));
		//sb.append(String.format("<description>Original ID: '%s'</description>\n", resourceId));
		sb.append(getTransitionsXML());
		sb.append("</task>\n");
		return sb.toString();
    }
	
	@Override
	public void fillBasicProperties(JSONObject json) throws JSONException {
		super.fillBasicProperties(json);
		String widgetJson = json.getJSONObject("properties").getString("aperte-conf");
		if (widgetJson != null && widgetJson.trim().length() != 0) {
		  widget = new Widget();
		  widgetJson = Util.replaceXmlEscapeCharacters(widgetJson);
		  JSONObject widgetJsonObj = new JSONObject(widgetJson);
		  commentary = widgetJsonObj.getString("commentary");
		  widgetName = widgetJsonObj.getString("name");
		  assignee = widgetJsonObj.optString("assignee");
		  swimlane = widgetJsonObj.optString("swimlane");
		  candidateGroups = widgetJsonObj.optString("candidate_groups");
		  JSONArray children = widgetJsonObj.optJSONArray("children");
		  JSONObject properties = widgetJsonObj.optJSONObject("properties");
		  JSONObject permissions = widgetJsonObj.optJSONObject("permissions");
		  widget.setWidgetId(widgetJsonObj.getString("widgetId"));
		  createWidgetTree(widget, children, properties, permissions);
		}
	}
	
	private void createWidgetTree(Widget w, JSONArray children, JSONObject properties, JSONObject permissions) throws JSONException {
		if (properties != null) {
			Iterator it = properties.keys();
			while(it.hasNext()) {
				String key = (String)it.next();
				Object value = properties.get(key);
				w.putAttribute(key, value);
			}
		}
		if (permissions != null) {
			Iterator it = permissions.keys();
			while(it.hasNext()) {
				String key = (String)it.next();
				Object value = permissions.get(key);
				w.putPermission(key, value);
			}
		}
		if (children != null) {
			for (int i = 0; i < children.length(); i++) {
				JSONObject obj = children.getJSONObject(i);
				Widget n = new Widget();
				n.setWidgetId(obj.getString("widgetId"));
				w.addChildWidget(n);
				createWidgetTree(n, obj.optJSONArray("children"), obj.optJSONObject("properties"), obj.optJSONObject("permissions"));
			}
		}
	}
	
	private String generateChildrenXML(List<Widget> list, boolean withChildrenTag) {
		if (list.isEmpty())
		  return "";
		
		StringBuffer sb = new StringBuffer();
		if (withChildrenTag)
		  sb.append("<children>\n");
		for (Widget w : list) {
			sb.append(String.format("<config.ProcessStateWidget className=\"%s\" priority=\"1\">\n", w.getWidgetId()));
			sb.append(generatePermissionsXML(w.getPermissionsMap()));
			sb.append(generateAttributesXML(w.getAttributesMap()));
			sb.append(generateChildrenXML(w.getChildrenList(), true));
			sb.append("</config.ProcessStateWidget>\n");
		}
		if (withChildrenTag)
		  sb.append("</children>\n");
		return sb.toString();
	}
	
	private String generateAttributesXML(Map<String,Object> attributesMap) {
		if (attributesMap.isEmpty())
			return "";
		StringBuffer sb = new StringBuffer();
		sb.append("<attributes>\n");
		for (String key : attributesMap.keySet()) {
			Object value = attributesMap.get(key);
			sb.append(String.format("<config.ProcessStateWidgetAttribute name=\"%s\" value=\"%s\"/>", key, value));
		}
		sb.append("</attributes>\n");
		return sb.toString();
	}
	
	private String generatePermissionsXML(Map<String,Object> permissionsMap) {
		if (permissionsMap.isEmpty())
			return "";
		StringBuffer sb = new StringBuffer();
		sb.append("<permissions>\n");
		for (String key : permissionsMap.keySet()) {
			Object value = permissionsMap.get(key);
			sb.append(String.format("<config.ProcessStateWidgetPermission privilegeName=\"%s\" roleName=\"%s\"/>", key, value));
		}
		sb.append("</permissions>\n");
		return sb.toString();
	}
	 
	public String generateWidgetXML() {
		StringBuffer sb = new StringBuffer();
		sb.append(String.format("<config.ProcessStateConfiguration description=\"description\" name=\"%s\" commentary=\"%s\">\n", name, commentary));
		sb.append("<widgets>\n");
		sb.append(generatePermissionsXML(widget.getPermissionsMap()));
		sb.append(generateAttributesXML(widget.getAttributesMap()));
		sb.append(generateChildrenXML(widget.getChildrenList(), false));
		sb.append("</widgets>\n");
		
		
		if (!outgoing.isEmpty()) {
		  sb.append("<actions>\n");
 		  for (String resId : outgoing.keySet()) {
 			JPDLTransition trans = outgoing.get(resId);
		    sb.append(String.format("<config.ProcessStateAction autohide=\"%s\" bpmName=\"%s\" buttonName=\"Default\" ", trans.getAutoHide(), trans.getName()));
		    sb.append(String.format("description=\"%s\" label=\"%s\" priority=\"%s\" skipSaving=\"%s\" />\n", trans.getTargetName(), trans.getTargetName(), trans.getPriority(), trans.getSkipSaving()));
 		  }
 		  sb.append("</actions>\n");
		}
		sb.append("</config.ProcessStateConfiguration>\n");
		return sb.toString();
	}

	public String getSwimlane() {
		return swimlane;
	}
	
	
}
