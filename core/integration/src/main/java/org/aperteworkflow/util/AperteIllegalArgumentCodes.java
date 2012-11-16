package org.aperteworkflow.util;

import org.aperteworkflow.service.fault.AperteWsIllegalArgumentException;


/**
 * 
 * @author kkolodziej@bluesoft.net.pl
 *
 */
public enum AperteIllegalArgumentCodes {
	
	FILTR("Filtr cannot by null or empty!",201),
	DEFINITION("Definition key cannot by null or empty!",202),
	PROCESS("Process name cannot by null or empty!",203), 
	TASK("Task name cannot by null or empty!",204),
	TASK_ID("Task Id cannot by null or empty!",205)
	;
	
	String message;
	Integer errorCode;
	
	private AperteIllegalArgumentCodes(String message, Integer wrongCode) {
		this.message = message;
		this.errorCode = wrongCode;
	}

public void  throwAperteWebServiceException() throws AperteWsIllegalArgumentException{
		throw new AperteWsIllegalArgumentException(errorCode, message);
	}



}
