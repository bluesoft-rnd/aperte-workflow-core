package pl.net.bluesoft.rnd.processtool.dict.mapping.metadata.dict;

import pl.net.bluesoft.rnd.processtool.dict.mapping.providers.DerivedDictEntryProvider;
import pl.net.bluesoft.rnd.processtool.dict.mapping.providers.DictEntryProvider;

import java.util.Arrays;
import java.util.Collection;

/**
 * User: POlszewski
 * Date: 2012-01-02
 * Time: 23:06:36
 */
public class CustomDictDescription extends DictDescription {
	private String baseDictName;
	private String keyProperty;
	private String valueProperty;

	public CustomDictDescription(String name) {
		super(name);
	}

	public String getBaseDictName() {
		return baseDictName;
	}

	public CustomDictDescription setBaseDictName(String baseDictName) {
		this.baseDictName = baseDictName;
		return this;
	}

	public String getKeyProperty() {
		return keyProperty;
	}

	public CustomDictDescription setKeyProperty(String keyProperty) {
		this.keyProperty = keyProperty;
		return this;
	}

	public String getValueProperty() {
		return valueProperty;
	}

	public CustomDictDescription setValueProperty(String valueProperty) {
		this.valueProperty = valueProperty;
		return this;
	}

	@Override
	public Collection<String> getBaseDictionaries() {
		return Arrays.asList(baseDictName);
	}

	@Override
	public DictEntryProvider createDictEntryProvider() {
		if (baseDictName != null) {
			return new DerivedDictEntryProvider(this);
		}
		return null;
	}
}
