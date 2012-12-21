package pl.net.bluesoft.rnd.processtool.dict.mapping.metadata.dict;

import pl.net.bluesoft.rnd.processtool.dict.mapping.providers.CustomValueProvider;

/**
 * User: POlszewski
 * Date: 2012-01-02
 * Time: 22:35:24
 */
public abstract class PTDictDescription extends DictDescription {
	private Class entryClass;
	private CustomValueProvider<?> customValueProvider;

	public PTDictDescription(String name) {
		super(name);
	}

	public Class getEntryClass() {
		return entryClass;
	}

	public PTDictDescription setEntryClass(Class entryClass) {
		this.entryClass = entryClass;
		return this;
	}

	public CustomValueProvider<?> getCustomValueProvider() {
		return customValueProvider;
	}

	public PTDictDescription setCustomValueProvider(CustomValueProvider<?> customValueProvider) {
		this.customValueProvider = customValueProvider;
		return this;
	}
}
