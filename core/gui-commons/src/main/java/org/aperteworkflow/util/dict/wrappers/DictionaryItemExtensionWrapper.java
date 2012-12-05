package org.aperteworkflow.util.dict.wrappers;

/**
 * User: POlszewski
 * Date: 2012-12-01
 * Time: 12:48
 */
public interface DictionaryItemExtensionWrapper<WrappedExtensionType> {
	String _NAME = "name";
	String _VALUE = "value";

	WrappedExtensionType getWrappedObject();

	DictionaryItemExtensionWrapper<WrappedExtensionType> exactCopy();

	String getName();
	void setName(String name);

	String getValue();
	void setValue(String value);

	String getDescription();
}
