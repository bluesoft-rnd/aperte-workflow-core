package pl.net.bluesoft.rnd.processtool.model.dict;

import java.util.Collection;
import java.util.Date;
import java.util.Locale;

public interface ProcessDictionaryItemValue {
	@Deprecated
    String getDefaultValue();
	String getValue(String languageCode);
	String getValue(Locale locale);
    Date getValidFrom();
    Date getValidTo();
    boolean isValidForDate(Date date);
    
    Collection<ProcessDictionaryItemExtension> getItemExtensions();
}
