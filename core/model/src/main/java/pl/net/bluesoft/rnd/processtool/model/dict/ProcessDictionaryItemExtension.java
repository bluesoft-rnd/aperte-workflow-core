package pl.net.bluesoft.rnd.processtool.model.dict;

public interface ProcessDictionaryItemExtension<V> {
    String getName();
    void setName(String name);
    V getValue();
    void setValue(V value);
    String getValueType();
    void setValueType(String valueType);
    String getDescription();
    void setDescription(String description);
}
