package pl.net.bluesoft.rnd.processtool.bpm.exception;

/**
 * @author tlipski@bluesoft.net.pl
 */
public class ProcessToolException extends RuntimeException {

	private String details;

	public ProcessToolException() {
	}

	public ProcessToolException(String message) {
		super(message);
	}

	public ProcessToolException(String message, Throwable cause) {
		super(message, cause);
	}

	public ProcessToolException(Throwable cause) {
		super(cause);
	}

	public ProcessToolException(String message, String details) {
		super(message);
		this.details = details;
	}

	public ProcessToolException(String message, Throwable cause, String details) {
		super(message, cause);
		this.details = details;
	}

	public ProcessToolException(Throwable cause, String details) {
		super(cause);
		this.details = details;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}
}
