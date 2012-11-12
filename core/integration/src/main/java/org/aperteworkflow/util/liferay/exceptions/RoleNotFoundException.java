package org.aperteworkflow.util.liferay.exceptions;

/** 
 * Wyjątek dotyczący nie znalezienia roli o podanej nazwie
 * @author Maciej Pawlak
 *
 */
public class RoleNotFoundException extends RuntimeException {

	public RoleNotFoundException() {
		// TODO Auto-generated constructor stub
	}

	public RoleNotFoundException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public RoleNotFoundException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public RoleNotFoundException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
