package pl.net.bluesoft.rnd.processtool.model.dict;

import java.util.Collection;
import java.util.Date;
import java.util.Locale;

public interface ProcessDictionaryItemValue {
	String getValue(String languageCode);
	String getValue(Locale locale);
    Date getValidFrom();
    Date getValidTo();
    boolean isValidForDate(Date date);
    boolean isEmptyValue();
    
    Collection<ProcessDictionaryItemExtension> getItemExtensions();
	String getExtValue(String name);
}
