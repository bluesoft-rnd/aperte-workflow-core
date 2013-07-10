package pl.net.bluesoft.rnd.processtool.web.domain;

/**
 * Representation of error 
 * 
 * @author mpawlak@bluesoft.net.pl
 *
 */
public class ErrorResultBean 
{
	private String source;
	private String message;
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}

}
