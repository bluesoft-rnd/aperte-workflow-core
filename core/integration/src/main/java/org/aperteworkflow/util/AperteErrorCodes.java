package org.aperteworkflow.util;

import org.aperteworkflow.service.fault.AperteWebServiceError;

/**
 * 
 * @author kkolodziej@bluesoft.net.pl
 *
 */
public enum AperteErrorCodes {
	USER("Wrong user name, such user does not exists.",101),
	PROCESS("Wrong internalID, such process does not exists.",102),
	ACTION("No such name of Action",103),
	BPMTASK("No suche name of bpmTask",104),
	NOTASK("The user is not assaigned to any Task in this process.",105),
	SIMPLE_ATTRIBUTE("Wrong key, such key does not exists.",106),
	DEFINITION("Wrong definition name, such definitnion does not exists.",107),
	TASK_ID("No Such name of task!",108),
	FILTR("Filtr cannot by null or empty!",201),
	DEFINITION_KEY("Definition key cannot by null or empty!",202),
	PROCESS_NAME("Process name cannot by null or empty!",203), 
	TASK_NAME("Task name cannot by null or empty!",204)
	;
	
	String message;
	Integer errorCode;
	
	private AperteErrorCodes(String message, Integer wrongCode) {
		this.message = message;
		this.errorCode = wrongCode;
	}

public void  throwAperteWebServiceError() throws AperteWebServiceError{
		throw new AperteWebServiceError(errorCode, message);
	}

	

}
