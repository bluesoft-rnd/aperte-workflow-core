package pl.net.bluesoft.rnd.processtool.editor.jpdl.object;

import org.json.JSONException;
import org.json.JSONObject;

public class JPDLDecision extends JPDLComponent {

	
    protected JPDLDecision() {
    	
	}
	
    protected static final String DECISION_X = "100";
	protected static final String DECISION_Y = "50";
	
	@Override
	public void fillBasicProperties(JSONObject json) throws JSONException {
		super.fillBasicProperties(json);
	}
    
    
    @Override
	public String toXML() { 
    	StringBuffer sb = new StringBuffer();
    	sb.append(String.format("<decision name=\"%s\" g=\"%s,%s,%s,%s\">\n", name,x1,y1,DECISION_X,DECISION_Y));
		sb.append(getTransitionsXML());
		sb.append("</decision>");
		return sb.toString();
    }
	
}
