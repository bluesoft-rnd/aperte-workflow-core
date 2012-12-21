package org.aperteworkflow.util.dict.wrappers;

import java.util.Set;

/**
 * User: POlszewski
 * Date: 2012-12-01
 * Time: 13:21
 */
public interface DictionaryItemWrapper<WrappedItemType, ItemValueWrapperType extends DictionaryItemValueWrapper> {
	String _KEY = "key";
	String _DESCRIPTION = "description";
	String _VALUES = "values";

	WrappedItemType getWrappedObject();

	String getDescription();
	void setDescription(String description);

	String getKey();
	void setKey(String key);

	String getValueType();
	void setValueType(String valueType);

	Set<ItemValueWrapperType> getValues();
	void setValues(Set<ItemValueWrapperType> values);
}
