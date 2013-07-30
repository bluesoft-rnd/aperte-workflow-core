package pl.net.bluesoft.rnd.processtool.model.dict;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

public interface ProcessDictionary {
    String getDictionaryId();
	@Deprecated
	String getDefaultName();
	String getName(String languageCode);
	String getName(Locale locale);

    ProcessDictionaryItem lookup(String key);
	boolean containsKey(String key);
    Collection<String> itemKeys();
    Collection<ProcessDictionaryItem> items();
	List<ProcessDictionaryItem> sortedItems(String languageCode);
}
