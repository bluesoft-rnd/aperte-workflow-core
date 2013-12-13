package pl.net.bluesoft.rnd.processtool.model.dict;

import java.util.Collection;
import java.util.Date;
import java.util.Locale;

public interface ProcessDictionaryItem {
    String getKey();
    String getValueType();

    @Deprecated
    String getDefaultDescription();
    String getDescription(Locale locale);
    String getDescription(String localeName);


    Collection<ProcessDictionaryItemValue> values();

    ProcessDictionaryItemValue getValueForDate(Date date);
    ProcessDictionaryItemValue getValueForCurrentDate();
}
