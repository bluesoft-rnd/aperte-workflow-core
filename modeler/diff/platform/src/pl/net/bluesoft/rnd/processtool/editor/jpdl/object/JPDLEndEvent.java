package pl.net.bluesoft.rnd.processtool.editor.jpdl.object;

public class JPDLEndEvent extends JPDLComponent {
  
	
	protected JPDLEndEvent() {
	  
    }
	
	private static final String END_EVENT_X = "50";
	private static final String END_EVENT_Y = "50";
	
	public String toXML() { 
		StringBuffer sb = new StringBuffer();
		sb.append(String.format("<end name=\"%s\" g=\"%s,%s,%s,%s\">\n", name,x1,y1,END_EVENT_X,END_EVENT_Y));
		//sb.append(String.format("<description>Original ID: '%s'</description>\n", resourceId));
		sb.append(getTransitionsXML());
		sb.append("</end>\n");
		return sb.toString();
    }
}
