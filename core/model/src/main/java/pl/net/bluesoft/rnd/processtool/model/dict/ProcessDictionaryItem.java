package pl.net.bluesoft.rnd.processtool.model.dict;

import java.util.Collection;

public interface ProcessDictionaryItem<K, V> {
    K getKey();
    void setKey(K key);
    V getValue();
    void setValue(V value);
    String getValueType();
    void setValueType(String valueType);

    Collection<ProcessDictionaryItemExtension> extensions();
    Collection<String> getExtensionNames();
    ProcessDictionaryItemExtension getExtensionByName(String extensionName);
}
