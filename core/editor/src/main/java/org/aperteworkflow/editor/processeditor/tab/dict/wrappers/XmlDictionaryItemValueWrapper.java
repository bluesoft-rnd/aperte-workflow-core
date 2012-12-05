package org.aperteworkflow.editor.processeditor.tab.dict.wrappers;

import org.apache.commons.lang3.time.DateUtils;
import org.aperteworkflow.util.dict.wrappers.DictionaryItemValueWrapper;
import pl.net.bluesoft.rnd.processtool.dict.xml.DictionaryEntryExtension;
import pl.net.bluesoft.rnd.processtool.dict.xml.DictionaryEntryValue;

import java.util.*;

/**
 * User: POlszewski
 * Date: 2012-12-03
 * Time: 16:20
 */
public class XmlDictionaryItemValueWrapper implements DictionaryItemValueWrapper<DictionaryEntryValue, XmlDictionaryItemExtensionWrapper> {
	private final DictionaryEntryValue itemValue;

	public XmlDictionaryItemValueWrapper(DictionaryEntryValue itemValue) {
		this.itemValue = itemValue;
	}

	public XmlDictionaryItemValueWrapper() {
		this(new DictionaryEntryValue());
	}

	@Override
	public DictionaryEntryValue getWrappedObject() {
		return itemValue;
	}

	@Override
	public XmlDictionaryItemValueWrapper exactCopy() {
		XmlDictionaryItemValueWrapper copy = new XmlDictionaryItemValueWrapper();

		copy.setValue(getValue());
		copy.setValidStartDate(getValidStartDate());
		copy.setValidEndDate(getValidEndDate());


		Map<String, XmlDictionaryItemExtensionWrapper> extensions = new HashMap<String, XmlDictionaryItemExtensionWrapper>();

		for (Map.Entry<String, XmlDictionaryItemExtensionWrapper> entry : getExtensions().entrySet()) {
			extensions.put(entry.getKey(), entry.getValue().exactCopy());
		}
		copy.setExtensions(extensions);
		return copy;
	}

	@Override
	public XmlDictionaryItemValueWrapper shallowCopy() {
		return exactCopy();
	}

	@Override
	public String getValue() {
		return itemValue.getValue();
	}

	@Override
	public void setValue(String value) {
		itemValue.setValue(value);
	}

	@Override
	public Date getValidStartDate() {
		return itemValue.getValidStartDate();
	}

	@Override
	public void setValidStartDate(Date validStartDate) {
		itemValue.setValidStartDate(validStartDate);
	}

	@Override
	public Date getValidEndDate() {
		return itemValue.getValidEndDate();
	}

	@Override
	public void setValidEndDate(Date validEndDate) {
		itemValue.setValidEndDate(validEndDate);
	}

	@Override
	public boolean isValidForDate(Date date) {
		Date validStartDate = getValidStartDate();
		Date validEndDate = getValidEndDate();

		if (date == null) {
			return validStartDate == null && validEndDate == null;
		}
		if (validStartDate != null && date.before(validStartDate) && !(DateUtils.isSameDay(date, validStartDate))) {
			return false;
		}
		else if (validEndDate != null && date.after(validEndDate) && !(DateUtils.isSameDay(date, validEndDate))) {
			return false;
		}
		return true;
	}

	@Override
	public Map<String, XmlDictionaryItemExtensionWrapper> getExtensions() {
		Map<String, XmlDictionaryItemExtensionWrapper> wrappers = new HashMap<String, XmlDictionaryItemExtensionWrapper>();

		for (DictionaryEntryExtension extension : itemValue.getExtensions()) {
			wrappers.put(extension.getName(), new XmlDictionaryItemExtensionWrapper(extension));
		}
		return wrappers;
	}

	@Override
	public void setExtensions(Map<String, XmlDictionaryItemExtensionWrapper> extensions) {
		List<DictionaryEntryExtension> unwrappedExtensions = new ArrayList<DictionaryEntryExtension>();

		for (XmlDictionaryItemExtensionWrapper wrapper : extensions.values()) {
			unwrappedExtensions.add(wrapper.getWrappedObject());
		}
		itemValue.setExtensions(unwrappedExtensions);
	}

	@Override
	public Collection<String> getExtensionNames() {
		return getExtensions().keySet();
	}

	@Override
	public XmlDictionaryItemExtensionWrapper getExtensionByName(String extensionName) {
		return getExtensions().get(extensionName);
	}

	@Override
	public boolean hasFullDatesRange() {
		return getValidStartDate() == null && getValidEndDate() == null;
	}
}
