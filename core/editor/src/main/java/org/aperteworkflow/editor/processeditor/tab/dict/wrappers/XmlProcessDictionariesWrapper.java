package org.aperteworkflow.editor.processeditor.tab.dict.wrappers;

import pl.net.bluesoft.rnd.processtool.dict.xml.Dictionary;
import pl.net.bluesoft.rnd.processtool.dict.xml.ProcessDictionaries;
import pl.net.bluesoft.util.lang.cquery.func.F;
import pl.net.bluesoft.util.lang.cquery.func.P;

import java.util.Collection;
import java.util.List;

import static pl.net.bluesoft.util.lang.cquery.CQuery.from;

/**
 * User: POlszewski
 * Date: 2012-12-04
 * Time: 10:52
 */
public class XmlProcessDictionariesWrapper {
	public static final String _DEFAULT_LANGUAGE = "defaultLanguage";

	private final ProcessDictionaries processDictionaries;

	public XmlProcessDictionariesWrapper(ProcessDictionaries processDictionaries) {
		this.processDictionaries = processDictionaries;
	}

	public ProcessDictionaries getWrappedObject() {
		return processDictionaries;
	}

	public String getDefaultLanguage() {
		return processDictionaries.getDefaultLanguage();
	}

	public void setDefaultLanguage(String defaultLanguage) {
		processDictionaries.setDefaultLanguage(defaultLanguage);
	}

	public Collection<String> getDictionaryIds() {
		return from(processDictionaries.getDictionaries()).select(new F<Dictionary, String>() {
			@Override
			public String invoke(Dictionary x) {
				return x.getDictionaryId();
			}
		}).distinct().ordered();
	}

	public Collection<String> getLanguageCodes() {
		return from(processDictionaries.getDictionaries()).select(new F<Dictionary, String>() {
			@Override
			public String invoke(Dictionary x) {
				return x.getLanguageCode();
			}
		}).distinct().ordered();
	}

	public List<XmlDictionaryItemWrapper> getItems(final String dictionaryId, final String languageCode) {
		return getDictionary(dictionaryId, languageCode).getItems();
	}

	public XmlDictionaryWrapper getDictionary(final String dictionaryId, final String languageCode) {
		Dictionary dictionary = from(processDictionaries.getDictionaries()).where(new P<Dictionary>() {
			@Override
			public boolean invoke(Dictionary dictionary) {
				return dictionary.getDictionaryId().equals(dictionaryId) && dictionary.getLanguageCode().equals(languageCode);
			}
		}).firstOrDefault();
		if (dictionary == null) {
			dictionary = new Dictionary();
			dictionary.setDictionaryId(dictionaryId);
			dictionary.setLanguageCode(languageCode);
			processDictionaries.getDictionaries().add(dictionary);
		}
		return new XmlDictionaryWrapper(dictionary);
	}
}
