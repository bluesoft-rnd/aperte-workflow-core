package pl.net.bluesoft.rnd.processtool.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Widget {
  
	private List<Widget> childrenList = new ArrayList<Widget>();
	private String widgetId;
	private Map<String,Object> attributesMap = new HashMap<String,Object>();
	private Map<String,Object> permissionsMap = new HashMap<String,Object>();
	
	public void addChildWidget(Widget w) {
		childrenList.add(w);
	}

	public List<Widget> getChildrenList() {
		return childrenList;
	}

	public void setChildrenList(List<Widget> childrenList) {
		this.childrenList = childrenList;
	}

	public String getWidgetId() {
		return widgetId;
	}

	public void setWidgetId(String widgetId) {
		this.widgetId = widgetId;
	}

	public Map<String, Object> getAttributesMap() {
		return attributesMap;
	}

	public void setAttributesMap(Map<String, Object> attributesMap) {
		this.attributesMap = attributesMap;
	}
	
	public void putAttribute(String key, Object value) {
		attributesMap.put(key, value);
	}
	
	public Map<String, Object> getPermissionsMap() {
		return permissionsMap;
	}

	public void setPermissionsMap(Map<String, Object> permissionsMap) {
		this.permissionsMap = permissionsMap;
	}
	
	public void putPermission(String key, Object value) {
		permissionsMap.put(key, value);
	}
}
