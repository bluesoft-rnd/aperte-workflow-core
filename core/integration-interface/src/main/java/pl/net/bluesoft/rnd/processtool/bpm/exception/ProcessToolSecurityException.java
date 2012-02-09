package pl.net.bluesoft.rnd.processtool.bpm.exception;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessToolSecurityException extends ProcessToolException {
	public ProcessToolSecurityException() {
	}

	public ProcessToolSecurityException(String message) {
		super(message);
	}

	public ProcessToolSecurityException(String message, Throwable cause) {
		super(message, cause);
	}

	public ProcessToolSecurityException(Throwable cause) {
		super(cause);
	}

	public ProcessToolSecurityException(String message, String details) {
		super(message, details);
	}

	public ProcessToolSecurityException(String message, Throwable cause, String details) {
		super(message, cause, details);
	}

	public ProcessToolSecurityException(Throwable cause, String details) {
		super(cause, details);
	}
}
