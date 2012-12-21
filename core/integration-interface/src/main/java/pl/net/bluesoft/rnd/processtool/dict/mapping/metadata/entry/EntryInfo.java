package pl.net.bluesoft.rnd.processtool.dict.mapping.metadata.entry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * User: POlszewski
 * Date: 2012-01-02
 * Time: 16:00:00
 */
public class EntryInfo {
	private String keyProperty;
	private Class keyType;
	private String valueProperty;
	private Class valueType;
	private String descriptionProperty;
	private Class descriptionType;
	private Map<String, ExtInfo> extInfos = new HashMap<String, ExtInfo>();
	private Class entryClass;

	public String getKeyProperty() {
		return keyProperty;
	}

	public void setKeyProperty(String keyProperty) {
		this.keyProperty = keyProperty;
	}

	public Class getKeyType() {
		return keyType;
	}

	public void setKeyType(Class keyType) {
		this.keyType = keyType;
	}

	public String getValueProperty() {
		return valueProperty;
	}

	public void setValueProperty(String valueProperty) {
		this.valueProperty = valueProperty;
	}

	public Class getValueType() {
		return valueType;
	}

	public void setValueType(Class valueType) {
		this.valueType = valueType;
	}

	public String getDescriptionProperty() {
		return descriptionProperty;
	}

	public void setDescriptionProperty(String descriptionProperty) {
		this.descriptionProperty = descriptionProperty;
	}

	public Class getDescriptionType() {
		return descriptionType;
	}

	public void setDescriptionType(Class descriptionType) {
		this.descriptionType = descriptionType;
	}

	public void addExtInfo(ExtInfo extInfo) {
		extInfos.put(extInfo.getName(), extInfo);
	}

	public ExtInfo getExtInfo(String property) {
		return extInfos.get(property);
	}

	public Collection<ExtInfo> getExtInfos() {
		return extInfos.values();
	}

	public Class getEntryClass() {
		return entryClass;
	}

	public void setEntryClass(Class entryClass) {
		this.entryClass = entryClass;
	}
}
