package pl.net.bluesoft.rnd.processtool.model.report.xml;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * User: POlszewski
 * Date: 2013-08-22
 * Time: 11:47
 */
@XStreamAlias("column")
public class ReportResultColumn {
	@XStreamAsAttribute
	private String description;
	@XStreamAsAttribute
	private int width;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}
}
