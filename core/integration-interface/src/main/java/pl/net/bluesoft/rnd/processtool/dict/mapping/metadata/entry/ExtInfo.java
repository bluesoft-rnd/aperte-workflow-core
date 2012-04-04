package pl.net.bluesoft.rnd.processtool.dict.mapping.metadata.entry;

/**
 * User: POlszewski
 * Date: 2012-01-02
 * Time: 19:09:58
 */
public class ExtInfo {
	private String name;
	private String property;
	private Class type;
	private String defaultValue;
	private Class elementClass;
	private String separator;
	private boolean defaultNull;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public Class getType() {
		return type;
	}

	public void setType(Class type) {
		this.type = type;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public Class getElementClass() {
		return elementClass;
	}

	public void setElementClass(Class elementClass) {
		this.elementClass = elementClass;
	}

	public String getSeparator() {
		return separator;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}

	public boolean isDefaultNull() {
		return defaultNull;
	}

	public void setDefaultNull(boolean defaultNull) {
		this.defaultNull = defaultNull;
	}
}
