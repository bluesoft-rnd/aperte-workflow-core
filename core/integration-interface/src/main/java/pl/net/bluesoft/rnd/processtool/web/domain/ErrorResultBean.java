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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ErrorResultBean that = (ErrorResultBean) o;

		if (message != null ? !message.equals(that.message) : that.message != null) return false;

		return true;
	}

	@Override
	public int hashCode() {
		return message != null ? message.hashCode() : 0;
	}
}
