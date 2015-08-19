package pl.net.bluesoft.rnd.processtool.auditlog.model;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * Created with IntelliJ IDEA.
 * User: Rafał Surowiecki
 * Date: 27.05.14
 * Time: 16:18
 */
public class AuditedProperty implements Comparable<AuditedProperty> {
	private String messageKey;
	private String name;
	private String value;
	private String dictKey;
	private String annotation;

	public AuditedProperty() {
	}

	public AuditedProperty(String messageKey, String name, String value, String dictKey, String annotation) {
		this.messageKey = messageKey;
		this.name = name;
		this.value = value;
		this.dictKey = dictKey;
		this.annotation = annotation;
	}

	public String getMessageKey() {
		return messageKey;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public void setMessageKey(String messageKey) {
		this.messageKey = messageKey;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDictKey() {
		return dictKey;
	}

	public void setDictKey(String dictKey) {
		this.dictKey = dictKey;
	}

	@JsonIgnore
	public boolean isUseDict() {
		return dictKey != null;
	}

	public String getAnnotation() {
		return annotation;
	}

	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}

	@Override
	public int compareTo(AuditedProperty property) {
		return name.compareTo(property.name);
	}

	@Override
	public String toString() {
		return "AuditedProperty{" +
				"messageKey='" + messageKey + '\'' +
				", name='" + name + '\'' +
				", value='" + value + '\'' +
				", dictKey='" + dictKey + '\'' +
				", annotation='" + annotation + '\'' +
				'}';
	}
}
