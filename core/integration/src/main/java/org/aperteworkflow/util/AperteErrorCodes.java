package org.aperteworkflow.util;
/**
 * 
 * @author kkolodziej@bluesoft.net.pl
 *
 */
public enum AperteErrorCodes {
	USER("Wrong user name, such user does not exists.",101),PROCESS("Wrong internalID, such process does not exists.",102),ACTION("No suche name of Action",103),BPMTASK("No suche name of bpmTask",104),NOTASK("The user is not assaigned to any Task in this process.",105);
	
	String message;
	Integer errorCode;
	
	private AperteErrorCodes(String message, Integer wrongCode) {
		this.message = message;
		this.errorCode = wrongCode;
	}

	public String getMessage() {
		return message;
	}

	public Integer getErrorCode() { 
		return errorCode;
	}
	

}
