package pl.net.bluesoft.rnd.processtool.model.dict;

import java.util.Collection;
import java.util.Date;

public interface ProcessDictionaryItemValue<V> {
    V getValue();
    void setValue(V value);

    void setValidStartDate(Date validStartDate);
    Date getValidStartDate();
    void setValidEndDate(Date validEndDate);
    Date getValidEndDate();
    boolean isValidForDate(Date date);

    Collection<ProcessDictionaryItemExtension> extensions();
    Collection<String> getExtensionNames();
    ProcessDictionaryItemExtension getExtensionByName(String extensionName);
}
