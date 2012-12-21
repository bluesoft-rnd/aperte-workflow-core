package org.aperteworkflow.util.dict.wrappers;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * User: POlszewski
 * Date: 2012-12-01
 * Time: 12:57
 */
public interface DictionaryItemValueWrapper<WrappedItemValueType, ItemExtensionWrapperType extends DictionaryItemExtensionWrapper> {
	String _VALUE = "value";
	String _VALID_START_DATE = "validStartDate";
	String _VALID_END_DATE = "validEndDate";
	String _EXTENSIONS = "extensions";

	WrappedItemValueType getWrappedObject();

	DictionaryItemValueWrapper<WrappedItemValueType, ItemExtensionWrapperType> exactCopy();
	DictionaryItemValueWrapper<WrappedItemValueType, ItemExtensionWrapperType> shallowCopy();

	String getValue();
	void setValue(String value);

	Date getValidStartDate();
	void setValidStartDate(Date validStartDate);

	Date getValidEndDate();
	void setValidEndDate(Date validEndDate);

	boolean isValidForDate(Date date);

	Map<String, ItemExtensionWrapperType> getExtensions();
	void setExtensions(Map<String, ItemExtensionWrapperType> extensions);

	Collection<String> getExtensionNames();

	ItemExtensionWrapperType getExtensionByName(String extensionName);

	boolean hasFullDatesRange();
}
