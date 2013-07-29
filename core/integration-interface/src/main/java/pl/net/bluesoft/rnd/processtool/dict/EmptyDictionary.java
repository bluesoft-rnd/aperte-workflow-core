package pl.net.bluesoft.rnd.processtool.dict;

import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionary;
import pl.net.bluesoft.rnd.processtool.model.dict.ProcessDictionaryItem;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class EmptyDictionary implements ProcessDictionary {
    @Override
    public String getDictionaryId() {
        return getClass().getName();
    }

    @Override
    public String getDefaultName() {
        return getClass().getSimpleName();
    }

	@Override
	public String getName(String languageCode) {
		return getDefaultName();
	}

	@Override
	public String getName(Locale locale) {
		return getName(locale.toString());
	}

	@Override
    public ProcessDictionaryItem lookup(String key) {
        return null;
    }

    @Override
    public Collection<String> itemKeys() {
        return Collections.emptyList();
    }

    @Override
    public Collection<ProcessDictionaryItem> items() {
        return Collections.emptyList();
    }

	@Override
	public boolean containsKey(String key) {
		return false;
	}

	@Override
	public List<ProcessDictionaryItem> sortedItems(String languageCode) {
		return Collections.emptyList();
	}
}
