package pl.net.bluesoft.rnd.processtool.model.dict;

import java.util.Collection;
import java.util.Date;

public interface ProcessDictionaryItem<K, V> {
    K getKey();
    void setKey(K key);
    String getValueType();
    void setValueType(String valueType);

    String getDescription();
    void setDescription(String description);

    Collection<ProcessDictionaryItemValue<V>> values();
    void addValue(ProcessDictionaryItemValue<V> value);
    void removeValue(ProcessDictionaryItemValue<V> value);

    ProcessDictionaryItemValue<V> getValueForDate(Date date);
    ProcessDictionaryItemValue<V> getValueForCurrentDate();
}
