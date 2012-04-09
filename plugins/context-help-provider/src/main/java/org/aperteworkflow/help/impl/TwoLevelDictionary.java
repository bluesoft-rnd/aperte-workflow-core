package org.aperteworkflow.help.impl;

import org.apache.commons.collections.CollectionUtils;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionary;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItem;

import java.util.Collection;
import java.util.HashSet;

public class TwoLevelDictionary implements ProcessDictionary<String, String> {

	private ProcessDictionary<String, String>					globalDictionary;
	private ProcessDictionary<String, String>					processDictionary;
	private Collection<String>									allItemKeys	= new HashSet<String>();
	private Collection<ProcessDictionaryItem<String, String>>	allItems	= new HashSet<ProcessDictionaryItem<String, String>>();

	public TwoLevelDictionary(ProcessDictionary globalDictionary, ProcessDictionary localDictionary) {
		super();
		this.globalDictionary = globalDictionary;
		this.processDictionary = localDictionary;
	}

	@Override
	public String getDictionaryId() {
		return processDictionary.getDictionaryId();
	}

	@Override
	public String getDictionaryName() {
		return processDictionary.getDictionaryName();
	}

	@Override
	public String getLanguageCode() {
		return processDictionary.getLanguageCode();
	}

	@Override
	public Boolean isDefaultDictionary() {
		return false;
	}

	@Override
	public ProcessDictionaryItem<String, String> lookup(String key) {
		if (processDictionary.containsKey(key)) {
			return processDictionary.lookup(key);
		} else {
			return globalDictionary.lookup(key);
		}
	}

	@Override
	public Collection<String> itemKeys() {
		if (allItemKeys.isEmpty()) {
			allItemKeys.addAll(processDictionary.itemKeys());
			allItemKeys.addAll(globalDictionary.itemKeys());
		}
		return allItemKeys;
	}

	@Override
	public Collection<ProcessDictionaryItem<String, String>> items() {
		if (allItems.isEmpty()) {
			allItems.addAll(processDictionary.items());
			for(Object key : CollectionUtils.subtract(globalDictionary.itemKeys(), processDictionary.itemKeys())){
				allItems.add(globalDictionary.lookup((String) key));
			}
		}
		return allItems;
	}

	@Override
	public boolean containsKey(String key) {
		return processDictionary.containsKey(key) || globalDictionary.containsKey(key);
	}

}