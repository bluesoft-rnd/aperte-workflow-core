package pl.net.bluesoft.rnd.processtool.editor.jpdl.object;

public class JPDLStartEvent extends JPDLComponent {
  
	
	protected JPDLStartEvent() {
	  
    }
	
	private static final String START_EVENT_X = "50";
	private static final String START_EVENT_Y = "50";
	
	public String toXML() { 
		StringBuffer sb = new StringBuffer();
		sb.append(String.format("<start name=\"%s\" g=\"%s,%s,%s,%s\">\n", name,x1,y1,START_EVENT_X,START_EVENT_Y));
		//sb.append(String.format("<description>Original ID: '%s'</description>\n", resourceId));
		sb.append(getTransitionsXML());
		sb.append("</start>\n");
		return sb.toString();
    }
}
