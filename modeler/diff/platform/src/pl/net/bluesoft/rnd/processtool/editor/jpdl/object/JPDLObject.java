package pl.net.bluesoft.rnd.processtool.editor.jpdl.object;

import org.json.JSONException;
import org.json.JSONObject;

import pl.net.bluesoft.rnd.processtool.editor.jpdl.exception.UnsupportedJPDLObjectException;

public abstract class JPDLObject {
	
	protected String x1,y1,x2,y2;
	
	protected String resourceId;
	protected String name;
	
	public String getX1() {
		return x1;
	}

	public void setX1(String x1) {
		this.x1 = x1;
	}

	public String getY1() {
		return y1;
	}

	public void setY1(String y1) {
		this.y1 = y1;
	}

	public String getX2() {
		return x2;
	}

	public void setX2(String x2) {
		this.x2 = x2;
	}

	public String getY2() {
		return y2;
	}

	public void setY2(String y2) {
		this.y2 = y2;
	}

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public void fillBasicProperties(JSONObject json) throws JSONException {
		x1 = round(json.getJSONObject("bounds").getJSONObject("upperLeft").getString("x"));
		y1 = round(json.getJSONObject("bounds").getJSONObject("upperLeft").getString("y"));
		x2 = round(json.getJSONObject("bounds").getJSONObject("lowerRight").getString("x"));
		y2 = round(json.getJSONObject("bounds").getJSONObject("lowerRight").getString("y"));
		
		resourceId = json.getString("resourceId");
		name = json.getJSONObject("properties").getString("name");
	}
	
    public static JPDLObject getJPDLObject(JSONObject obj) throws JSONException, UnsupportedJPDLObjectException {
		
		JPDLObject ret = null;

		String stencilId = obj.getJSONObject("stencil").getString("id");
		
		if ("StartNoneEvent".equals(stencilId)) {
			ret = new JPDLStartEvent();
		} else if ("Task".equals(stencilId)) {
			String taskType = obj.getJSONObject("properties").getString("tasktype");
			if ("User".equals(taskType))
			  ret = new JPDLUserTask();
			else if ("None".equals(taskType))
			  throw new UnsupportedJPDLObjectException("Task type 'None' is not supported.");
			else
			  ret = new JPDLJavaTask();
		} else if ("SequenceFlow".equals(stencilId)) {
			ret = new JPDLTransition();
		} else if ("EndNoneEvent".equals(stencilId)) {
			ret = new JPDLEndEvent();
		} else if ("Exclusive_Databased_Gateway".equals(stencilId)) {
			ret = new JPDLDecision();
		} else {
		  throw new UnsupportedJPDLObjectException("Object named '" + stencilId + "' is not supported.");
		}
		return ret;
	}
    
    private static String round(String s) {
    	if (s == null)
    		return "0";
    	Float f = Float.parseFloat(s);
    	return String.valueOf(Math.round(f));
    }
    
}
