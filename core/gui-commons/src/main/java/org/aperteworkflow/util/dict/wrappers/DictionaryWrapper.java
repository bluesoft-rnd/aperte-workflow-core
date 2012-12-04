package org.aperteworkflow.util.dict.wrappers;

/**
 * User: POlszewski
 * Date: 2012-12-01
 * Time: 14:02
 */
public interface DictionaryWrapper<WrappedDictionaryType> {
	WrappedDictionaryType getWrappedObject();

	String getDictionaryId();
	void setDictionaryId(String dictionaryId);

	String getLanguageCode();
	void setLanguageCode(String languageCode);

	String getDictionaryName();
	void setDictionaryName(String dictionaryName);

	String getDescription();
	void setDescription(String description);
}
