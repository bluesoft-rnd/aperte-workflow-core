package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.dao;

/**
 * Created by mpawluczuk on 2014-11-17.
 */
public class BpmSettingsDTO {

	private String key;
	private String value;
	private String description;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
