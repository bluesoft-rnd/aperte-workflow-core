package pl.net.bluesoft.rnd.processtool.dict.mapping.metadata.item;

import pl.net.bluesoft.rnd.processtool.dict.mapping.providers.DictEntryProvider;

/**
 * User: POlszewski
 * Date: 2012-01-02
 * Time: 16:11:51
 */
public class PropertyInfo {
	private String property;
	private String dictName;
	private DictEntryProvider dictEntryProvider;

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public String getDictName() {
		return dictName;
	}

	public void setDictName(String dictName) {
		this.dictName = dictName;
	}

	public DictEntryProvider getDictEntryProvider() {
		return dictEntryProvider;
	}

	public void setDictEntryProvider(DictEntryProvider dictEntryProvider) {
		this.dictEntryProvider = dictEntryProvider;
	}
}
