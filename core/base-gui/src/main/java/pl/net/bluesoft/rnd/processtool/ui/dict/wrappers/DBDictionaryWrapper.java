package pl.net.bluesoft.rnd.processtool.ui.dict.wrappers;

import org.aperteworkflow.util.dict.wrappers.DictionaryWrapper;
import pl.net.bluesoft.rnd.processtool.model.dict.db.ProcessDBDictionary;

/**
 * User: POlszewski
 * Date: 2012-12-01
 * Time: 14:02
 */
public class DBDictionaryWrapper implements DictionaryWrapper<ProcessDBDictionary> {
	private final ProcessDBDictionary dictionary;

	public DBDictionaryWrapper(ProcessDBDictionary dictionary) {
		this.dictionary = dictionary;
	}

	@Override
	public ProcessDBDictionary getWrappedObject() {
		return dictionary;
	}

	@Override
	public String getDictionaryId() {
		return dictionary.getDictionaryId();
	}

	@Override
	public void setDictionaryId(String dictionaryId) {
		dictionary.setDictionaryId(dictionaryId);
	}

	@Override
	public String getLanguageCode() {
		return dictionary.getLanguageCode();
	}

	@Override
	public void setLanguageCode(String languageCode) {
		dictionary.setLanguageCode(languageCode);
	}

	@Override
	public String getDictionaryName() {
		return dictionary.getDictionaryName();
	}

	@Override
	public void setDictionaryName(String dictionaryName) {
		dictionary.setDictionaryName(dictionaryName);
	}

	@Override
	public String getDescription() {
		return dictionary.getDescription();
	}

	@Override
	public void setDescription(String description) {
		dictionary.setDescription(description);
	}
}
