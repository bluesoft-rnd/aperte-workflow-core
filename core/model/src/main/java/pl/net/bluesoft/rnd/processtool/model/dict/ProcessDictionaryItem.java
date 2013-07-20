package pl.net.bluesoft.rnd.processtool.model.dict;

import java.util.Collection;
import java.util.Date;

public interface ProcessDictionaryItem {
    String getKey();
    String getValueType();

    String getDescription();

    Collection<ProcessDictionaryItemValue> values();

    ProcessDictionaryItemValue getValueForDate(Date date);
    ProcessDictionaryItemValue getValueForCurrentDate();
}
