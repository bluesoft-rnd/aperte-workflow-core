package org.aperteworkflow.util;

import org.aperteworkflow.service.fault.AperteWsWrongArgumentException;

/**
 * 
 * @author kkolodziej@bluesoft.net.pl
 *
 */
public enum AperteWrongArgumentCodes {
	USER("Wrong user name, such user does not exists.",101),
	PROCESS("Wrong internalID, such process does not exists.",102),
	ACTION("No such name of Action",103),
	BPMTASK("No suche name of bpmTask",104),
	NOTASK("The user is not assaigned to any Task in this process.",105),
	SIMPLE_ATTRIBUTE("Wrong key, such key does not exists.",106),
	DEFINITION("Wrong definition name, such definitnion does not exists.",107),
	TASK_ID("No Such name of task!",108)
	;
	
	String message;
	Integer errorCode;
	
	private AperteWrongArgumentCodes(String message, Integer wrongCode) {
		this.message = message;
		this.errorCode = wrongCode;
	}

public void  throwAperteWebServiceException() throws AperteWsWrongArgumentException{
		throw new AperteWsWrongArgumentException(errorCode, message);
	}

	

}
