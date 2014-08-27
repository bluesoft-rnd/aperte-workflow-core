package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.model;

/**
 * User: POlszewski
 * Date: 2014-05-19
 */
public class BpmAttachment {
	private String name;
	private String contentType;
	private byte[] body;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public byte[] getBody() {
		return body;
	}

	public void setBody(byte[] body) {
		this.body = body;
	}
}
