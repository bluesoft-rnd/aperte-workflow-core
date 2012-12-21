package org.aperteworkflow.editor.processeditor.tab.dict.wrappers;

import org.aperteworkflow.util.dict.wrappers.DictionaryItemWrapper;
import pl.net.bluesoft.rnd.processtool.dict.xml.DictionaryEntry;
import pl.net.bluesoft.rnd.processtool.dict.xml.DictionaryEntryValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * User: POlszewski
 * Date: 2012-12-03
 * Time: 16:20
 */
public class XmlDictionaryItemWrapper implements DictionaryItemWrapper<DictionaryEntry, XmlDictionaryItemValueWrapper> {
	private final DictionaryEntry entry;

	public XmlDictionaryItemWrapper() {
		this(new DictionaryEntry());
	}

	public XmlDictionaryItemWrapper(DictionaryEntry entry) {
		this.entry = entry;
	}

	@Override
	public DictionaryEntry getWrappedObject() {
		return entry;
	}

	@Override
	public String getDescription() {
		return entry.getDescription();
	}

	@Override
	public void setDescription(String description) {
		entry.setDescription(description);
	}

	@Override
	public String getKey() {
		return entry.getKey();
	}

	@Override
	public void setKey(String key) {
		entry.setKey(key);
	}

	@Override
	public String getValueType() {
		return entry.getValueType();
	}

	@Override
	public void setValueType(String valueType) {
		entry.setValueType(valueType);
	}

	@Override
	public Set<XmlDictionaryItemValueWrapper> getValues() {
		Set<XmlDictionaryItemValueWrapper> wrappedValues = new HashSet<XmlDictionaryItemValueWrapper>();

		for (DictionaryEntryValue value : entry.getValues()) {
			wrappedValues.add(new XmlDictionaryItemValueWrapper(value));
		}
		return wrappedValues;
	}

	@Override
	public void setValues(Set<XmlDictionaryItemValueWrapper> values) {
		List<DictionaryEntryValue> unwrappedValues = new ArrayList<DictionaryEntryValue>();

		for (XmlDictionaryItemValueWrapper value : values) {
			unwrappedValues.add(value.getWrappedObject());
		}
		entry.setValues(unwrappedValues);
	}
}
