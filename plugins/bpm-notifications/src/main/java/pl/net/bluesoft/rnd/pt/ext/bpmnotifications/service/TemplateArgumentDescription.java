package pl.net.bluesoft.rnd.pt.ext.bpmnotifications.service;

/**
 * User: POlszewski
 * Date: 2012-10-14
 * Time: 10:44
 */
public class TemplateArgumentDescription {
	private String name;
	private String description;

	public TemplateArgumentDescription() {
	}

	public TemplateArgumentDescription(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
