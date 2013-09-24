package pl.net.bluesoft.rnd.processtool.model.report.xml;

/**
 * User: POlszewski
 * Date: 2013-08-21
 * Time: 14:38
 */

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("param")
public class ReportParam {
	@XStreamAsAttribute
	private String name;
	@XStreamAsAttribute
	private String description;
	@XStreamAsAttribute
	private ReportParamType type;
	@XStreamAsAttribute
	private String values;

	public ReportParam() {}

	public ReportParam(String name, String description, ReportParamType type) {
		this.name = name;
		this.description = description;
		this.type = type;
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

	public ReportParamType getType() {
		return type;
	}

	public void setType(ReportParamType type) {
		this.type = type;
	}

	public String getValues() {
		return values;
	}

	public void setValues(String values) {
		this.values = values;
	}
}
