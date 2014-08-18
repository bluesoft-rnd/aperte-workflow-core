package pl.net.bluesoft.interactivereports.controller;

/**
* User: POlszewski
* Date: 2014-06-25
*/
public class ReportTemplateDTO {
	private String key;
	private String name;

	public ReportTemplateDTO() {}

	public ReportTemplateDTO(String key, String name) {
		this.key = key;
		this.name = name;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
