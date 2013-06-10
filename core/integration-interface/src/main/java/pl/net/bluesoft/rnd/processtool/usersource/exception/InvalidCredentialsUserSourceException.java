package pl.net.bluesoft.rnd.processtool.usersource.exception;

import pl.net.bluesoft.rnd.processtool.usersource.IUserSource;


/**
 * {@link IUserSource} exception class
 * Invalid user login or password exception
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class InvalidCredentialsUserSourceException extends UserSourceException {

	public InvalidCredentialsUserSourceException() {
		// TODO Auto-generated constructor stub
	}

	public InvalidCredentialsUserSourceException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public InvalidCredentialsUserSourceException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public InvalidCredentialsUserSourceException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
