package pl.net.bluesoft.rnd.processtool.model.dict;

import java.util.Collection;

public interface ProcessDictionary<K, V> {
    String getDictionaryId();
	String getDictionaryName();
    String getLanguageCode();
    Boolean isDefaultDictionary();

    ProcessDictionaryItem<K, V> lookup(K key);
	boolean containsKey(K key);
    Collection<K> itemKeys();
    Collection<ProcessDictionaryItem<K, V>> items();
}
