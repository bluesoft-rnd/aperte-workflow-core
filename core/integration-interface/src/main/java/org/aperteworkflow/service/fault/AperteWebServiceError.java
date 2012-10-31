package org.aperteworkflow.service.fault;

import javax.xml.ws.WebFault;
/**
 * 
 * @author kkolodziej@bluesoft.net.pl
 *
 */
@WebFault(name = "faultBean", targetNamespace = "http://www.example.com")
public class AperteWebServiceError extends Exception {
 
	private FaultBean faultBean;
	private  String testText ="testText";
    
	public AperteWebServiceError(int code, String message){
		super(message);
		faultBean = new FaultBean(code);
		
	}

	public FaultBean getFaultBean() {
		return faultBean;
	}

	public String getTestText() {
		return testText;
	}
	
	
	
	
	
	
}
